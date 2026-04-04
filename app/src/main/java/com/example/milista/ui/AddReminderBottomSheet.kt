package com.example.milista.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import androidx.compose.runtime.collectAsState

@Composable
fun AddReminderBottomSheet(
    viewModel: MiListaViewModel,
    onDismiss: () -> Unit,
    onTypeSelected: (String) -> Unit
) {
    val reminderTypes = ReminderConstants.types
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f),
        color = CardDark,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getTranslatedText("Configurar Aviso", selectedLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                
                Surface(
                    onClick = onDismiss,
                    color = SamsungRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        getTranslatedText("Cerrar", selectedLanguage), 
                        color = SamsungRed, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 28.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 24.dp)
            ) {
                items(reminderTypes) { type ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypeSelected(type.name) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(type.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type.emoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = getTranslatedText(type.name, selectedLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Espaciador inferior para barra de navegación
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
