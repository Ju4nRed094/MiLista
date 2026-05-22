package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmScreen(
    title: String,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "alarmGlow")
    val glowSize by infiniteTransition.animateFloat(
        initialValue = 200.dp.value,
        targetValue = 400.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowSize"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        contentAlignment = Alignment.Center
    ) {
        // Fondo animado glow
        Box(
            modifier = Modifier
                .size(glowSize.dp)
                .blur(100.dp)
                .background(NeonGreen.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = currentTime,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-4).sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title.ifEmpty { "Alarma" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
            )

            Spacer(modifier = Modifier.height(100.dp))

            // Icono de alarma pulsante
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "iconScale"
            )
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(NeonGreen.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text("Posponer", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Descartar", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
