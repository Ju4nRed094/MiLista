package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.milista.R
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: MiListaViewModel,
    listaId: Int,
    onBack: () -> Unit,
    onNavigateToEditor: (Int, Int) -> Unit
) {
    val tareas by viewModel.repository.obtenerTareasPorLista(listaId).collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to Icons.Default.FilterList,
        "work" to Icons.Default.Work,
        "personal" to Icons.Default.Person,
        "ideas" to Icons.Default.Lightbulb,
        "study" to Icons.AutoMirrored.Filled.MenuBook,
        "favorites" to Icons.Default.Star
    )

    // Ordenación automática: Fijadas primero, luego por fecha de creación descendente
    val filteredTareas = remember(tareas, selectedCategory) {
        tareas.filter { 
            (selectedCategory == "all" || (selectedCategory == "favorites" && it.esFavorita) || it.contenido.contains("#$selectedCategory"))
        }.sortedWith(compareByDescending<Tarea> { it.esFavorita }.thenByDescending { it.fechaCreacion })
    }

    val pinnedTareas = remember(filteredTareas) { filteredTareas.filter { it.esFavorita } }
    val recentTareas = remember(filteredTareas) { filteredTareas.filter { !it.esFavorita } }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Fondo Noctra Premium
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SamsungGreen.copy(alpha = 0.03f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NoteTopBarPremium(
                    onAddClick = { onNavigateToEditor(listaId, -1) }
                )
            }
        ) { padding ->
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp
            ) {
                // Título y Subtítulo integrado en la lista para mejor scroll
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)) {
                        Text(
                            text = stringResource(R.string.notes),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Text(
                            text = "Organiza tus ideas con Noctra",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayText.copy(alpha = 0.6f)
                        )
                    }
                }

                // Chips de categorías
                item(span = StaggeredGridItemSpan.FullLine) {
                    NoteCategoryChipsLocal(categories, selectedCategory, { selectedCategory = it })
                }

                // Sección Fijadas
                if (pinnedTareas.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Icon(Icons.Rounded.PushPin, null, tint = SamsungGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.pinned), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    items(pinnedTareas, key = { it.id }) { tarea ->
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        
                        NoteGridCardPremium(
                            tarea = tarea, 
                            onClick = { onNavigateToEditor(listaId, tarea.id) },
                            onLongClick = { showDeleteConfirm = true }
                        )

                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                containerColor = CardDark,
                                shape = RoundedCornerShape(28.dp),
                                title = { Text("¿Eliminar nota?", color = Color.White, fontWeight = FontWeight.Bold) },
                                text = { Text("Esta acción no se puede deshacer.", color = GrayText) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.borrarTarea(tarea)
                                            showDeleteConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SamsungRed),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) {
                                        Text("Cancelar", color = GrayText)
                                    }
                                }
                            )
                        }
                    }
                    item(span = StaggeredGridItemSpan.FullLine) { 
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Título Recientes si hay notas
                if (recentTareas.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            stringResource(R.string.recent_notes), 
                            color = Color.White.copy(alpha = 0.5f), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                if (recentTareas.isEmpty() && pinnedTareas.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        NoteEmptyStatePremium()
                    }
                } else {
                    items(recentTareas, key = { it.id }) { tarea ->
                        var showDeleteConfirm by remember { mutableStateOf(false) }

                        NoteGridCardPremium(
                            tarea = tarea, 
                            onClick = { onNavigateToEditor(listaId, tarea.id) },
                            onLongClick = { showDeleteConfirm = true }
                        )

                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                containerColor = CardDark,
                                shape = RoundedCornerShape(28.dp),
                                title = { Text("¿Eliminar nota?", color = Color.White, fontWeight = FontWeight.Bold) },
                                text = { Text("Esta acción no se puede deshacer.", color = GrayText) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.borrarTarea(tarea)
                                            showDeleteConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SamsungRed),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) {
                                        Text("Cancelar", color = GrayText)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTopBarPremium(onAddClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { /* Título manejado en el scroll para estilo Samsung */ },
        navigationIcon = {
            // Sin botón de retroceso por ser pantalla principal de la barra inferior
        },
        actions = {
            Surface(
                onClick = onAddClick,
                shape = CircleShape,
                color = SamsungGreen.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, SamsungGreen.copy(alpha = 0.2f)),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, null, tint = SamsungGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva", color = SamsungGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteGridCardPremium(tarea: Tarea, onClick: () -> Unit, onLongClick: () -> Unit) {
    val cardColor = if (tarea.color != null) Color(tarea.color) else Color(0xFF1A1A1A)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        color = cardColor.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            if (tarea.imagenPath != null) {
                AsyncImage(
                    model = tarea.imagenPath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (tarea.esFavorita) {
                    Icon(Icons.Rounded.PushPin, null, tint = SamsungGreen, modifier = Modifier.size(14.dp).align(Alignment.End))
                }

                if (tarea.titulo.isNotBlank()) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (tarea.contenido.isNotBlank()) {
                    Text(
                        text = tarea.contenido,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Fecha de creación
                val dateFormat = remember { java.text.SimpleDateFormat("dd MMM", Locale.getDefault()) }
                Text(
                    text = dateFormat.format(Date(tarea.fechaCreacion)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun NoteCategoryChipsLocal(
    categories: List<Pair<String, ImageVector>>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(categories) { (name, _) ->
            val isSelected = selectedCategory == name
            val stringId = when(name) {
                "all" -> R.string.all
                "work" -> R.string.work
                "personal" -> R.string.personal
                "ideas" -> R.string.ideas
                "study" -> R.string.study
                "favorites" -> R.string.favorites
                else -> R.string.all
            }
            Surface(
                onClick = { onCategorySelect(name) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) SamsungGreen else Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f)),
            ) {
                Text(
                    text = stringResource(stringId),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NoteEmptyStatePremium() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(120.dp).background(SamsungGreen.copy(alpha = 0.05f), CircleShape).blur(40.dp))
            Icon(Icons.Default.EditNote, null, modifier = Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.05f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.ideas_begin_here), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.create_first_smart_note), color = GrayText.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
    }
}
