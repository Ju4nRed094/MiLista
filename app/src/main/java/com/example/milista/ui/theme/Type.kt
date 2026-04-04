package com.example.milista.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.milista.R

data class AppFont(val name: String, val fontFamily: FontFamily)

val AppFonts = listOf(
    AppFont("Default", FontFamily.Default),
    AppFont("Agnia Misel", FontFamily(Font(R.font.agnia_misel))),
    AppFont("Bold Move", FontFamily(Font(R.font.bold_move))),
    AppFont("DIY", FontFamily(Font(R.font.diy))),
    AppFont("Futurist Black", FontFamily(Font(R.font.futurist_black))),
    AppFont("Hislife", FontFamily(Font(R.font.hislife))),
    AppFont("Icemaster", FontFamily(Font(R.font.icemaster))),
    AppFont("Le Cercle", FontFamily(Font(R.font.le_cercle_des_artisans))),
    AppFont("Make It Click", FontFamily(Font(R.font.makeitclick))),
    AppFont("Matrix Pulse", FontFamily(Font(R.font.matrix_pulse))),
    AppFont("Milk Yogurt", FontFamily(Font(R.font.milk_yogurt))),
    AppFont("Quantum Futuristic", FontFamily(Font(R.font.quantum_futuristic))),
    AppFont("Romano", FontFamily(Font(R.font.romano))),
    AppFont("Saigon Outline", FontFamily(Font(R.font.saigon_melon_outline))),
    AppFont("Senara", FontFamily(Font(R.font.senara))),
    AppFont("Shelter Coffee", FontFamily(Font(R.font.shelter_coffee))),
    AppFont("Spidol Clash", FontFamily(Font(R.font.spidol_clash))),
    AppFont("Steak Break", FontFamily(Font(R.font.steak_break))),
    AppFont("Strange Neighbor", FontFamily(Font(R.font.strange_neighbor))),
    AppFont("Super Youth", FontFamily(Font(R.font.super_youth))),
    AppFont("Uniwars HV", FontFamily(Font(R.font.uniwars_hv)))
)

fun getTypography(fontFamily: FontFamily = FontFamily.Default) = Typography(
    displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

val Typography = getTypography()
