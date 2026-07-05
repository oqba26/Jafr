package com.oqba26.jafr.util

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.oqba26.jafr.R

class PersianNumberVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val input = text.text
        val sb = StringBuilder()
        
        for (char in input) {
            if (char.isDigit()) {
                sb.append(persianDigits[char - '0'])
            } else {
                sb.append(char)
            }
        }
        
        return TransformedText(
            androidx.compose.ui.text.AnnotatedString(sb.toString()),
            OffsetMapping.Identity
        )
    }
}

fun createTypography(fontFamily: FontFamily): Typography {
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
    )
}

fun getFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "vazirmatn" -> FontFamily(
            Font(R.font.vazirmatn_regular, FontWeight.Normal),
            Font(R.font.vazirmatn_bold, FontWeight.Bold),
            Font(R.font.vazirmatn_light, FontWeight.Light),
            Font(R.font.vazirmatn_medium, FontWeight.Medium),
            Font(R.font.vazirmatn_thin, FontWeight.Thin),
            Font(R.font.vazirmatn_black, FontWeight.Black)
        )
        "estedad" -> FontFamily(
            Font(R.font.estedad_regular, FontWeight.Normal),
            Font(R.font.estedad_bold, FontWeight.Bold),
            Font(R.font.estedad_light, FontWeight.Light),
            Font(R.font.estedad_medium, FontWeight.Medium),
            Font(R.font.estedad_black, FontWeight.Black)
        )
        "byekan" -> FontFamily(
            Font(R.font.byekan, FontWeight.Normal),
            Font(R.font.byekan_bold, FontWeight.Bold)
        )
        "iraniansans" -> FontFamily(
            Font(R.font.iraniansans, FontWeight.Normal)
        )
        "sahel" -> FontFamily(
            Font(R.font.sahel_bold, FontWeight.Bold),
            Font(R.font.sahel_black, FontWeight.Black)
        )
        else -> FontFamily(Font(R.font.vazirmatn_regular))
    }
}
