package com.example.milista.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.FondoCard
import com.example.milista.ui.theme.TextoPrincipal

@Composable
fun AddReminderBottomSheet(
    onDismiss: () -> Unit,
    onTypeSelected: (String) -> Unit
) {
    val reminderTypes = ReminderConstants.types

    // Panel fijo de 3/4 de pantalla
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f),
        color = FondoCard,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con botón rojo de cerrar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tipo de aviso",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextoPrincipal,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
            
            // Lista de opciones con scroll que llega hasta la base
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)
            ) {
                items(reminderTypes) { type ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypeSelected(type.name) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(type.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type.emoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(18.dp))
                            Text(
                                text = type.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrincipal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // Rectángulo oscuro de la base donde se posiciona la barra de navegación
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // Altura exacta de la nueva barra de navegación pequeña
                    .background(Color(0xFF07111D))
            )
        }
    }
}
