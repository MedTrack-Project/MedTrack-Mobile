package com.example.piec_1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.piec_1.R

val MontserratFont = FontFamily(
    Font(R.font.montserratbold, FontWeight.Bold),
    Font(R.font.montserratthin, FontWeight.Thin),
)

val RobotoFont = FontFamily(
    Font(R.font.robotobold, FontWeight.Bold),
    Font(R.font.robotoblack, FontWeight.Black),
    Font(R.font.robotoregular, FontWeight.Normal),
)

val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Thin,
        fontSize = 12.sp,
    ),
)
