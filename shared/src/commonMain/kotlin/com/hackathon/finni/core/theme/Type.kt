package com.hackathon.finni.core.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.inter_bold
import com.hackathon.finni.resources.inter_light
import com.hackathon.finni.resources.inter_medium
import com.hackathon.finni.resources.inter_regular
import com.hackathon.finni.resources.inter_semibold
import com.hackathon.finni.resources.roboto_bold
import com.hackathon.finni.resources.roboto_medium
import com.hackathon.finni.resources.roboto_regular
import org.jetbrains.compose.resources.Font

@Composable
fun getAppTypography(): Typography {
    val interFontFamily = FontFamily(
        Font(Res.font.inter_light, FontWeight.Light),
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_bold, FontWeight.Bold)
    )

    val robotoFontFamily = FontFamily(
        Font(Res.font.roboto_regular, FontWeight.Normal),
        Font(Res.font.roboto_medium, FontWeight.Medium),
        Font(Res.font.roboto_bold, FontWeight.Bold)
    )

    val base = Typography()

    return remember(interFontFamily, robotoFontFamily) {
        base.copy(
            displayLarge = base.displayLarge.copy(fontFamily = interFontFamily),
            displayMedium = base.displayMedium.copy(fontFamily = interFontFamily),
            displaySmall = base.displaySmall.copy(fontFamily = interFontFamily),

            headlineLarge = base.headlineLarge.copy(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp
            ),
            headlineMedium = base.headlineMedium.copy(fontFamily = interFontFamily),
            headlineSmall = base.headlineSmall.copy(fontFamily = interFontFamily),

            titleLarge = base.titleLarge.copy(
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            ),
            titleMedium = base.titleMedium.copy(fontFamily = interFontFamily),
            titleSmall = base.titleSmall.copy(fontFamily = interFontFamily),

            bodyLarge = base.bodyLarge.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = base.bodyMedium.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
            bodySmall = base.bodySmall.copy(fontFamily = robotoFontFamily),

            labelLarge = base.labelLarge.copy(fontFamily = interFontFamily),
            labelMedium = base.labelMedium.copy(fontFamily = interFontFamily),
            labelSmall = base.labelSmall.copy(
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}
