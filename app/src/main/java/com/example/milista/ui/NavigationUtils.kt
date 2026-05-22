package com.example.milista.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.R
import com.example.milista.ui.theme.*

@Composable
fun PremiumBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToClock: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToLists: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentRoute: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(32.dp, RoundedCornerShape(40.dp), spotColor = NeonGreen.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF0D0D0D).copy(alpha = 0.96f),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendario
                BottomNavItem(
                    icon = Icons.Default.CalendarMonth,
                    label = stringResource(R.string.calendar),
                    isSelected = currentRoute == "calendar",
                    onClick = onNavigateToCalendar
                )
                
                // Reloj
                BottomNavItem(
                    icon = Icons.Default.AccessTime,
                    label = stringResource(R.string.clock),
                    isSelected = currentRoute == "clock",
                    onClick = onNavigateToClock
                )
                
                // Notas
                BottomNavItem(
                    icon = Icons.Default.Description,
                    label = stringResource(R.string.notes),
                    isSelected = currentRoute == "notes",
                    onClick = onNavigateToNotes
                )
                
                // INICIO (CENTRO)
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = stringResource(R.string.home),
                    isSelected = currentRoute == "home",
                    isMain = true,
                    onClick = onNavigateToHome
                )
                
                // Listas
                BottomNavItem(
                    icon = Icons.Default.Checklist,
                    label = stringResource(R.string.lists),
                    isSelected = currentRoute == "listas",
                    onClick = onNavigateToLists
                )
                
                // Recordatorio
                BottomNavItem(
                    icon = Icons.Default.Notifications,
                    label = stringResource(R.string.reminders),
                    isSelected = currentRoute == "reminders",
                    onClick = onNavigateToReminders
                )
                
                // Ajustes
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    isSelected = currentRoute == "settings",
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector, 
    label: String, 
    isSelected: Boolean, 
    isMain: Boolean = false,
    onClick: () -> Unit
) {
    val color = if (isSelected) NeonGreen else GrayText.copy(alpha = 0.5f)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) (if (isMain) 1.25f else 1.15f) else (if (isMain) 1.1f else 1.0f),
        label = "navScale"
    )
    
    val iconSize = if (isMain) 28.dp else 22.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .then(if (isMain && isSelected) Modifier.shadow(12.dp, CircleShape, spotColor = NeonGreen) else Modifier)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (isMain && isSelected) 
                Modifier
                    .size(44.dp)
                    .background(NeonGreen.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, NeonGreen.copy(alpha = 0.3f), CircleShape)
            else Modifier
        ) {
            Icon(
                icon, 
                null, 
                tint = color, 
                modifier = Modifier.size(iconSize)
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = label,
            fontSize = 7.5.sp,
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
