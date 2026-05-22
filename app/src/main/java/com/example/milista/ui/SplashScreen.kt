package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.R
import com.example.milista.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onStart: () -> Unit) {
    val messages = listOf(
        "Mejorando y organizando tu vida...",
        "Preparando tu productividad...",
        "Organizando tu tiempo inteligentemente...",
        "Cargando tu espacio personal...",
        "Diseñando tu futuro hoy...",
        "Sincronizando tus metas...",
        "Optimizando tu flujo de trabajo..."
    )
    
    var currentMessage by remember { mutableStateOf(messages.random()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            var newMessage = messages.random()
            while (newMessage == currentMessage) {
                newMessage = messages.random()
            }
            currentMessage = newMessage
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        // Fondo con glow ambiental
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .blur(100.dp)
                .background(NeonGreen.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = "Noctra Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Texto Noctra
            Text(
                text = "Noctra",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-2).sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Mensajes Dinámicos
            AnimatedContent(
                targetState = currentMessage,
                transitionSpec = {
                    fadeIn(tween(800)) + slideInVertically { it / 2 } togetherWith
                    fadeOut(tween(800)) + slideOutVertically { -it / 2 }
                },
                label = "splashMessage"
            ) { message ->
                Text(
                    text = message,
                    color = GrayText,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Botón Iniciar
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Iniciar",
                    color = AmoledBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}
