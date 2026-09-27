package com.hackathon.finni.features.levels.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hackathon.finni.features.levels.model.LevelItem
import com.hackathon.finni.features.levels.model.LevelStatus
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.bg_levels_tile
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/**
 * Цвета дороги, соответствующие референсу:
 * - Внешний контур: темно-сливовый / шоколадный оттенок
 * - Тело дороги: светлый мощеный камень / песочный оттенок
 * - Осевая линия: пунктирная темно-сливовая полоса
 */
private val RoadBorderColor = Color(0xFF4A3549)
private val RoadSurfaceColor = Color(0xFFDDD2C1)
private val RoadCobbleLineColor = Color(0x334A3549)
private val RoadCenterLineColor = Color(0xFF4A3549)

/**
 * Интерактивная карта уровней с зигзагообразной мощеной дорогой:
 * - Динамически рассчитывает плавные кубические кривые Безье между узлами
 * - Отрисовывает мощеную дорогу с темной каймой, текстурой камня и пунктирной разметкой
 * - Точно позиционирует каждый узел уровня на изгибах трассы
 */
@Composable
fun ZigzagRoadMap(
    levels: List<LevelItem>,
    onLevelClick: (LevelItem) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 115.dp,
    paddingTop: Dp = 140.dp,
    paddingBottom: Dp = 150.dp
) {
    if (levels.isEmpty()) return

    val density = LocalDensity.current
    val tileBitmap = imageResource(Res.drawable.bg_levels_tile)

    // Общая высота полотна карты
    val totalHeight = paddingTop + paddingBottom + (rowHeight * (levels.size - 1).coerceAtLeast(0))

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = with(density) { totalHeight.toPx() }
        val paddingTopPx = with(density) { paddingTop.toPx() }
        val paddingBottomPx = with(density) { paddingBottom.toPx() }
        val rowHeightPx = with(density) { rowHeight.toPx() }
        val roadWidthPx = with(density) { 56.dp.toPx() }
        val borderWidthPx = with(density) { 5.dp.toPx() }
        val dashWidthPx = with(density) { 3.5.dp.toPx() }

        // Расчет координат центров уровней (снизу вверх, как в референсе)
        // Ритм зигзага: Слева (0.24) -> Центр (0.50) -> Справа (0.76) -> Центр (0.50)
        val xFractions = remember { listOf(0.24f, 0.50f, 0.76f, 0.50f) }

        val nodePoints = remember(levels.size, widthPx, heightPx) {
            levels.mapIndexed { index, _ ->
                val x = widthPx * xFractions[index % xFractions.size]
                val y = heightPx - paddingBottomPx - (index * rowHeightPx)
                Offset(x, y)
            }
        }

        // Построение пути дороги
        val roadPath = remember(nodePoints) {
            buildSmoothZigzagPath(nodePoints, rowHeightPx)
        }

        // 1. Отрисовка фона и извилистой дороги
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Фоновый льняной паттерн с желудями и листьями (увеличен/приближен для лучшей читаемости деталей)
            val bgScale = 1.8f
            val shader = ImageShader(tileBitmap, TileMode.Repeated, TileMode.Repeated)
            scale(scale = bgScale, pivot = Offset.Zero) {
                drawRect(
                    brush = ShaderBrush(shader),
                    size = Size(
                        width = size.width / bgScale,
                        height = size.height / bgScale
                    )
                )
            }

            // Внешняя темная обводка дороги
            drawPath(
                path = roadPath,
                color = RoadBorderColor,
                style = Stroke(
                    width = roadWidthPx + (borderWidthPx * 2),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Внутреннее каменное полотно дороги
            drawPath(
                path = roadPath,
                color = RoadSurfaceColor,
                style = Stroke(
                    width = roadWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Промежуточные стыки брусчатки (cobblestones)
            drawCobblestoneSeams(
                points = nodePoints,
                roadWidthPx = roadWidthPx,
                seamColor = RoadCobbleLineColor
            )

            // Центральная пунктирная разделительная полоса
            drawPath(
                path = roadPath,
                color = RoadCenterLineColor,
                style = Stroke(
                    width = dashWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f)
                )
            )
        }

        // 2. Отрисовка интерактивных узлов уровней поверх полотна дороги
        levels.forEachIndexed { index, level ->
            val point = nodePoints.getOrNull(index) ?: return@forEachIndexed

            // Смещение элемента так, чтобы база кнопки располагалась строго по центру дороги
            val xOffsetDp = with(density) { point.x.toDp() }
            val yOffsetDp = with(density) { point.y.toDp() }

            Box(
                modifier = Modifier
                    .offset {
                        when (level.status) {
                            LevelStatus.CURRENT -> {
                                // Для текущего узла база кнопки находится внизу (yOffset), а пин парит над ней
                                IntOffset(
                                    x = (point.x - with(density) { 48.dp.toPx() }).roundToInt(),
                                    y = (point.y - with(density) { 98.dp.toPx() }).roundToInt()
                                )
                            }

                            LevelStatus.COMPLETED -> {
                                // Для пройденного узла кнопка центрируется на точке
                                IntOffset(
                                    x = (point.x - with(density) { 43.dp.toPx() }).roundToInt(),
                                    y = (point.y - with(density) { 44.dp.toPx() }).roundToInt()
                                )
                            }

                            LevelStatus.LOCKED -> {
                                // Для заблокированного круглого диска
                                IntOffset(
                                    x = (point.x - with(density) { 38.dp.toPx() }).roundToInt(),
                                    y = (point.y - with(density) { 38.dp.toPx() }).roundToInt()
                                )
                            }
                        }
                    }
            ) {
                LevelNodeItem(
                    level = level,
                    onClick = onLevelClick
                )
            }
        }
    }
}

/**
 * Построение гладкой кубической кривой Безье с естественными закруглениями на шпильках (hairpin turns).
 */
private fun buildSmoothZigzagPath(points: List<Offset>, rowHeightPx: Float): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().x, points.first().y)
    val n = points.size

    for (i in 0 until n - 1) {
        val p0 = points[i]
        val p3 = points[i + 1]

        val t0 = calculateTangent(i, points, rowHeightPx)
        val t1 = calculateTangent(i + 1, points, rowHeightPx)

        // Контрольные точки кубической кривой
        val cp1 = Offset(
            x = p0.x + t0.x * 0.36f,
            y = p0.y + t0.y * 0.36f
        )
        val cp2 = Offset(
            x = p3.x - t1.x * 0.36f,
            y = p3.y - t1.y * 0.36f
        )

        path.cubicTo(
            x1 = cp1.x, y1 = cp1.y,
            x2 = cp2.x, y2 = cp2.y,
            x3 = p3.x, y3 = p3.y
        )
    }

    return path
}

/**
 * Расчет вектора касательной для точки i:
 * - В точках поворота (крайние левые и правые узлы) касательная направлена строго вверх,
 *   что формирует естественное огибание узла дорогой.
 * - В центральных узлах касательная ориентирована по диагонали перехода.
 */
private fun calculateTangent(index: Int, points: List<Offset>, rowHeightPx: Float): Offset {
    val n = points.size
    return when {
        index == 0 -> {
            Offset(
                x = (points[1].x - points[0].x) * 0.8f,
                y = (points[1].y - points[0].y) * 0.8f
            )
        }

        index == n - 1 -> {
            Offset(
                x = (points[n - 1].x - points[n - 2].x) * 0.8f,
                y = (points[n - 1].y - points[n - 2].y) * 0.8f
            )
        }
        // Четные индексы (0, 2, 4...) — это крайние узлы (Слева или Справа)
        index % 2 == 0 -> {
            Offset(0f, -rowHeightPx * 1.25f)
        }
        // Нечетные индексы (1, 3, 5...) — узлы по центру
        else -> {
            Offset(
                x = (points[index + 1].x - points[index - 1].x) * 0.55f,
                y = (points[index + 1].y - points[index - 1].y) * 0.55f
            )
        }
    }
}

/**
 * Отрисовка декоративных поперечных швов брусчатки вдоль дороги
 */
private fun DrawScope.drawCobblestoneSeams(
    points: List<Offset>,
    roadWidthPx: Float,
    seamColor: Color
) {
    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]

        // 2-3 поперечных шва на каждом отрезке между уровнями
        for (step in 1..2) {
            val t = step / 3f
            val mx = p0.x + (p1.x - p0.x) * t
            val my = p0.y + (p1.y - p0.y) * t

            // Перпендикуляр к направлению
            val dx = p1.x - p0.x
            val dy = p1.y - p0.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val nx = -dy / len
            val ny = dx / len

            val half = roadWidthPx * 0.42f
            drawLine(
                color = seamColor,
                start = Offset(mx - nx * half, my - ny * half),
                end = Offset(mx + nx * half, my + ny * half),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }
}
