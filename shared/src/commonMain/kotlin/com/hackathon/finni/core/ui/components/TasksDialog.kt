package com.hackathon.finni.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.task_badge_check
import com.hackathon.finni.resources.task_badge_shop
import com.hackathon.finni.resources.task_badge_star
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Модель элемента сюжетного финансового задания.
 */
data class GameTaskItem(
    val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val badgeRes: DrawableResource,
    val isCompleted: Boolean = false,
    val isAvailable: Boolean = true
)

/**
 * Список сюжетных заданий Главы 1 в соответствии с дизайн-референсом и ТЗ.
 */
val DefaultChapter1Tasks = listOf(
    GameTaskItem(
        id = "task_1",
        number = 1,
        title = "Первые конверты",
        description = "Распредели доступные монеты по трем категориям бюджета — оставь запас на питание и карманный резерв в кошельке, а остальное отправь в копилку мечты.",
        badgeRes = Res.drawable.task_badge_check,
        isCompleted = true
    ),
    GameTaskItem(
        id = "task_2",
        number = 2,
        title = "Честный труд",
        description = "Разложи карточки с предложениями заработка: нажимай «Честно» для полезного труда и «Обман» для сомнительных схем или воровства.",
        badgeRes = Res.drawable.task_badge_star,
        isCompleted = false
    ),
    GameTaskItem(
        id = "task_3",
        number = 3,
        title = "Умный покупатель",
        description = "Купи в буфете только то, что указано в списке покупок (воду), и не поддавайся соблазну потратить лишние монеты на леденец у кассы.",
        badgeRes = Res.drawable.task_badge_shop,
        isCompleted = false
    )
)

/**
 * Диалог списка заданий текущей главы [TasksDialog], созданный на базе [GameDialog]
 * в точном визуальном соответствии с дизайн-референсом:
 *
 * - Заголовок главы в глянцевом баннере («Глава 1: Новая комната»)
 * - 3 глянцевые золотисто-карамельные карточки с круглыми 3D-медалями (галочка, звезда, тележка)
 * - Кнопка-крестик закрытия
 */
@Composable
fun TasksDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    chapterTitle: String = "Глава 1: Новая комната",
    tasks: List<GameTaskItem> = DefaultChapter1Tasks,
    onTaskClick: (GameTaskItem) -> Unit = {}
) {
    GameDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = chapterTitle
    ) {
        tasks.forEach { task ->
            TaskCardItem(
                task = task,
                onClick = { onTaskClick(task) }
            )
        }
    }
}

/**
 * Глянцевая карточка задания с 3D-медалью и описанием.
 */
@Composable
fun TaskCardItem(
    task: GameTaskItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 450f),
        label = "task_card_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x60000000),
                spotColor = Color(0x80000000)
            )
            .clip(RoundedCornerShape(18.dp))
            // Золотистая окантовка карточки
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFEEBA),
                        Color(0xFFECA348),
                        Color(0xFFC7781C)
                    )
                )
            )
            .padding(2.5.dp)
            .clip(RoundedCornerShape(16.dp))
            // Основной карамельно-золотистый градиент карточки
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF5D6),
                        Color(0xFFFEDBA0),
                        Color(0xFFF7AE52),
                        Color(0xFFEB9433)
                    )
                )
            )
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                enabled = task.isAvailable,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Верхний глянцевый блик карточки (белый овал)
        Box(
            modifier = Modifier
                .padding(start = 6.dp, top = 2.dp)
                .size(width = 18.dp, height = 9.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x99FFFFFF))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглая 3D-медаль задания слева
            Image(
                painter = painterResource(task.badgeRes),
                contentDescription = task.title,
                modifier = Modifier
                    .size(54.dp)
                    .offset(x = (-2).dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Текстовый блок (Номер + Название жирным, Описание обычным)
            val annotatedText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = comfortaaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF381A08)
                    )
                ) {
                    append("${task.number}. ${task.title}. ")
                }
                withStyle(
                    style = SpanStyle(
                        fontFamily = comfortaaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Color(0xFF381A08)
                    )
                ) {
                    append(task.description)
                }
            }

            Text(
                text = annotatedText,
                lineHeight = 14.5.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )
        }
    }
}
