package com.example.milista.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.milista.R
import com.example.milista.ui.theme.*

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val steps = listOf(
        OnboardingStep(
            title = "Bienvenido a Noctra",
            description = "Tu ecosistema de productividad premium. Necesitamos algunos permisos para brindarte la mejor experiencia.",
            icon = Icons.Rounded.RocketLaunch,
            isWelcome = true
        ),
        OnboardingStep(
            title = "Notificaciones",
            description = "Para avisarte sobre tus recordatorios, eventos y alarmas importantes.",
            icon = Icons.Rounded.Notifications,
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
        ),
        OnboardingStep(
            title = "Multimedia",
            description = "Para que puedas adjuntar imágenes a tus notas y usar tus sonidos personalizados.",
            icon = Icons.Rounded.Image,
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        ),
        OnboardingStep(
            title = "Ubicación",
            description = "Para el reloj mundial automático y funciones inteligentes basadas en tu zona.",
            icon = Icons.Rounded.LocationOn,
            permission = Manifest.permission.ACCESS_FINE_LOCATION
        ),
        OnboardingStep(
            title = "Precisión y Vibración",
            description = "Noctra usará alarmas exactas y vibración para que nunca pierdas un compromiso importante.",
            icon = Icons.Rounded.AvTimer,
            isWelcome = false // Just informative, but could request if needed
        )
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Proceed to next step regardless of grant (handled in UI)
        if (currentStep < steps.size - 1) {
            currentStep++
        } else {
            onFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        // Fondo con glow
        Box(modifier = Modifier.size(300.dp).align(Alignment.TopEnd).offset(x = 100.dp, y = (-100).dp).blur(80.dp).background(NeonGreen.copy(alpha = 0.1f)))
        Box(modifier = Modifier.size(300.dp).align(Alignment.BottomStart).offset(x = (-100).dp, y = 100.dp).blur(80.dp).background(NeonGreen.copy(alpha = 0.05f)))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
            }
        ) { stepIndex ->
            val step = steps[stepIndex]
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = NeonGreen
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = step.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = step.description,
                    fontSize = 16.sp,
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Indicador de pasos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentStep) NeonGreen else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (step.permission != null) {
                            launcher.launch(step.permission)
                        } else {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onFinished()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(
                        text = when {
                            step.isWelcome -> "Comenzar"
                            currentStep == steps.size - 1 -> "Finalizar"
                            else -> "Permitir"
                        },
                        color = AmoledBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                if (!step.isWelcome) {
                    TextButton(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onFinished()
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Omitir", color = GrayText)
                    }
                }
            }
        }
    }
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val permission: String? = null,
    val isWelcome: Boolean = false
)
