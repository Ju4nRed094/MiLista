package com.example.milista.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText

@Composable
fun PremiumBottomBar(
    selectedLanguage: String,
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPlus: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentRoute: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Bar Background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = NeonGreen.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(32.dp),
            color = CardDark.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.Default.Home, getTranslatedText("Inicio", selectedLanguage), currentRoute == "home", onNavigateToHome)
                BottomNavItem(Icons.Default.CalendarToday, getTranslatedText("Calendario", selectedLanguage), currentRoute == "calendar", onNavigateToCalendar)
                
                // Espacio para el FAB central
                Spacer(modifier = Modifier.width(64.dp))

                BottomNavItem(Icons.Default.TrackChanges, getTranslatedText("Enfoque", selectedLanguage), currentRoute == "productivity", onNavigateToFocus)
                BottomNavItem(Icons.Default.Settings, getTranslatedText("Ajustes", selectedLanguage), currentRoute == "settings", onNavigateToSettings)
            }
        }

        // Prominent Central FAB
        FloatingActionButton(
            onClick = onNavigateToPlus,
            shape = CircleShape,
            containerColor = NeonGreen,
            contentColor = AmoledBlack,
            modifier = Modifier
                .offset(y = (-32).dp)
                .size(64.dp)
                .shadow(16.dp, CircleShape, spotColor = NeonGreen),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) NeonGreen else GrayText.copy(alpha = 0.5f)
    val scale by animateFloatAsState(if (isSelected) 1.15f else 1.0f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(4.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(3.dp)
                    .background(NeonGreen, CircleShape)
            )
        }
    }
}
