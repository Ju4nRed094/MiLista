package com.example.milista.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText

@Composable
fun PremiumBottomBar(
    selectedLanguage: String,
    onNavigateToCalendar: () -> Unit,
    onNavigateToClock: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToLists: () -> Unit,
    currentRoute: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Glassmorphism Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .shadow(32.dp, RoundedCornerShape(38.dp), spotColor = NeonGreen.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(38.dp),
            color = Color(0xFF0D0D0D).copy(alpha = 0.92f), // AMOLED Dark Glass
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Rounded.CalendarMonth,
                    label = getTranslatedText("Calendario", selectedLanguage),
                    isSelected = currentRoute == "calendar",
                    onClick = onNavigateToCalendar
                )
                BottomNavItem(
                    icon = Icons.Rounded.Schedule,
                    label = getTranslatedText("Reloj", selectedLanguage),
                    isSelected = currentRoute == "clock",
                    onClick = onNavigateToClock
                )
                BottomNavItem(
                    icon = Icons.Rounded.Description,
                    label = getTranslatedText("Notas", selectedLanguage),
                    isSelected = currentRoute == "notes",
                    onClick = onNavigateToNotes
                )
                BottomNavItem(
                    icon = Icons.Rounded.Notifications,
                    label = getTranslatedText("Recordatorios", selectedLanguage),
                    isSelected = currentRoute == "reminders",
                    onClick = onNavigateToReminders
                )
                BottomNavItem(
                    icon = Icons.Rounded.Checklist,
                    label = getTranslatedText("Listas", selectedLanguage),
                    isSelected = currentRoute == "listas",
                    onClick = onNavigateToLists
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) NeonGreen else GrayText.copy(alpha = 0.4f)
    val scale by animateFloatAsState(if (isSelected) 1.15f else 1.0f)
    val iconSize by animateFloatAsState(if (isSelected) 26f else 24f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Icon(
            icon, 
            null, 
            tint = color, 
            modifier = Modifier.size(iconSize.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(3.dp)
                    .background(NeonGreen, CircleShape)
                    .shadow(4.dp, CircleShape, spotColor = NeonGreen)
            )
        }
    }
}
