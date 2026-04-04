package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.milista.R
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: MiListaViewModel,
    listaId: Int,
    onBack: () -> Unit,
    onNavigateToEditor: (Int, Int) -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val tareas by viewModel.repository.obtenerTareasPorLista(listaId).collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("Todas") }

    val categories = listOf(
        "Todas" to Icons.Default.FilterList,
        "Trabajo" to Icons.Default.Work,
        "Personal" to Icons.Default.Person,
        "Ideas" to Icons.Default.Lightbulb,
        "Estudio" to Icons.AutoMirrored.Filled.MenuBook,
        "Favoritas" to Icons.Default.Star
    )

    val filteredTareas = remember(tareas, selectedCategory) {
        tareas.filter { 
            (selectedCategory == "Todas" || (selectedCategory == "Favoritas" && it.esFavorita) || it.contenido.contains("#${selectedCategory.lowercase()}"))
        }
    }

    val pinnedTareas = remember(filteredTareas) { filteredTareas.filter { it.esFavorita } }
    val recentTareas = remember(filteredTareas) { filteredTareas.filter { !it.esFavorita } }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Fondo con iluminación ambiental Noctra
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SamsungGreen.copy(alpha = 0.04f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.1f),
                            radius = size.width * 1.6f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigateToEditor(listaId, -1) },
                    containerColor = SamsungGreen,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 90.dp, end = 12.dp)
                        .size(72.dp)
                        .shadow(24.dp, CircleShape, spotColor = SamsungGreen)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(36.dp))
                }
            }
        ) { padding ->
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 180.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp
            ) {
                // Header span
                item(span = StaggeredGridItemSpan.FullLine) {
                    NoteHeaderLocal(selectedLanguage, onBack)
                }

                // Chips span
                item(span = StaggeredGridItemSpan.FullLine) {
                    NoteCategoryChipsLocal(categories, selectedCategory, { selectedCategory = it }, selectedLanguage)
                }

                // Fijadas Section
                if (pinnedTareas.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SectionHeaderLocal(getTranslatedText("Fijadas", selectedLanguage), "Ver todas", {})
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(pinnedTareas) { tarea ->
                                PinnedNoteCardLocal(tarea, onClick = { onNavigateToEditor(listaId, tarea.id) })
                            }
                        }
                    }
                    item(span = StaggeredGridItemSpan.FullLine) { Spacer(modifier = Modifier.height(32.dp)) }
                }

                // Recientes Title
                item(span = StaggeredGridItemSpan.FullLine) {
                    SectionHeaderLocal(getTranslatedText("Recientes", selectedLanguage), "Ver todo", {}, icon = Icons.Default.AccessTime)
                }

                if (recentTareas.isEmpty() && pinnedTareas.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        NoteEmptyStateLocal(selectedLanguage)
                    }
                } else {
                    items(recentTareas, key = { it.id }) { tarea ->
                        NoteGridCardLocal(tarea, onClick = { onNavigateToEditor(listaId, tarea.id) })
                    }
                }

                // Banner Inferior
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(modifier = Modifier.height(40.dp))
                    EscribeSinLimitesBannerLocal(selectedLanguage, { onNavigateToEditor(listaId, -1) })
                }
            }
        }
    }
}

@Composable
fun NoteHeaderLocal(language: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getTranslatedText("Notas", language),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        letterSpacing = (-1.5).sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder for pencil icon
                    contentDescription = null,
                    tint = SamsungGreen,
                    modifier = Modifier.size(28.dp).offset(y = 6.dp)
                )
            }
            Text(
                text = getTranslatedText("Tus ideas, siempre contigo.", language),
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText.copy(alpha = 0.7f)
            )
        }
        Row(modifier = Modifier.padding(top = 8.dp)) {
            HeaderCircleButtonLocal(Icons.Default.Search)
            Spacer(modifier = Modifier.width(12.dp))
            HeaderCircleButtonLocal(Icons.Default.FilterList)
            Spacer(modifier = Modifier.width(12.dp))
            HeaderCircleButtonLocal(Icons.Default.MoreVert)
        }
    }
}

@Composable
fun HeaderCircleButtonLocal(icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun NoteCategoryChipsLocal(
    categories: List<Pair<String, ImageVector>>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    language: String
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        items(categories) { (name, icon) ->
            val isSelected = selectedCategory == name
            Surface(
                onClick = { onCategorySelect(name) },
                shape = RoundedCornerShape(26.dp),
                color = if (isSelected) SamsungGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (isSelected) SamsungGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.animateContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) SamsungGreen else GrayText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = getTranslatedText(name, language),
                        color = if (isSelected) Color.White else GrayText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeaderLocal(title: String, actionText: String, onActionClick: () -> Unit, icon: ImageVector = Icons.Default.StarBorder) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = SamsungGreen, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Text(
            actionText + " >",
            color = SamsungGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun PinnedNoteCardLocal(tarea: Tarea, onClick: () -> Unit) {
    val color = when {
        tarea.titulo.contains("Plan", true) -> SamsungGreen
        tarea.titulo.contains("Ideas", true) -> SamsungPurple
        else -> SamsungGreen
    }

    Surface(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .clickable { onClick() }
            .shadow(20.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Línea de color lateral
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            tarea.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.PushPin, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    tarea.contenido,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = GrayText.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            if (color == SamsungGreen) "Trabajo" else "Ideas",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            color = color,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text("Hoy", fontSize = 11.sp, color = GrayText.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun NoteGridCardLocal(tarea: Tarea, onClick: () -> Unit) {
    val color = when {
        tarea.titulo.contains("Trabajo", true) || tarea.contenido.contains("#trabajo", true) -> SamsungGreen
        tarea.titulo.contains("Personal", true) || tarea.contenido.contains("#personal", true) -> SamsungBlue
        tarea.titulo.contains("Ideas", true) || tarea.contenido.contains("#ideas", true) -> SamsungPurple
        tarea.titulo.contains("Estudio", true) || tarea.contenido.contains("#estudio", true) -> SamsungOrange
        else -> SamsungGreen
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column {
            // Case for different note types (IMAGE)
            if (tarea.imagenPath != null) {
                AsyncImage(
                    model = tarea.imagenPath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Header Row (Icon + Extra Info like Audio duration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val typeIcon = when {
                        tarea.audioPath != null -> Icons.Default.Mic
                        tarea.imagenPath != null -> Icons.Default.Image
                        tarea.contenido.contains("\n-") -> Icons.Default.CheckBox
                        tarea.contenido.contains("http") -> Icons.Default.Link
                        else -> Icons.Default.Description
                    }
                    Icon(typeIcon, null, tint = color, modifier = Modifier.size(18.dp))
                    
                    if (tarea.audioPath != null) {
                        Text("02:45", style = MaterialTheme.typography.labelSmall, color = GrayText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (tarea.titulo.isNotBlank()) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content (Text or Checklist simulation)
                if (tarea.contenido.isNotBlank()) {
                    if (tarea.contenido.contains("\n-")) { 
                        tarea.contenido.split("\n").filter { it.isNotBlank() }.take(4).forEach { line ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Default.CheckBoxOutlineBlank, null, tint = GrayText, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(line.trim('-',' '), style = MaterialTheme.typography.bodySmall, color = GrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    } else if (tarea.contenido.contains("http")) {
                        Text(
                            text = tarea.contenido,
                            style = MaterialTheme.typography.bodySmall.copy(color = color, textDecoration = TextDecoration.Underline),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = tarea.contenido,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = GrayText.copy(alpha = 0.85f),
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            if (color == SamsungGreen) "Trabajo" else if (color == SamsungBlue) "Personal" else if (color == SamsungPurple) "Ideas" else "Estudio",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 9.sp,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("13 mayo", fontSize = 9.sp, color = GrayText.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun EscribeSinLimitesBannerLocal(language: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(12.dp, RoundedCornerShape(36.dp), spotColor = SamsungGreen),
        shape = RoundedCornerShape(36.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(SamsungGreen.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.NoteAdd,
                    null,
                    tint = SamsungGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getTranslatedText("Escribe sin límites", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = getTranslatedText("Añade texto, imágenes, grabaciones, enlaces y mucho más.", language),
                    color = GrayText.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        getTranslatedText("Crear nueva nota", language),
                        color = SamsungGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = SamsungGreen, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun NoteEmptyStateLocal(language: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(SamsungGreen.copy(alpha = 0.06f), CircleShape)
                    .blur(50.dp)
            )
            Icon(
                Icons.Default.EditNote, 
                null, 
                modifier = Modifier.size(110.dp), 
                tint = Color.White.copy(alpha = 0.08f)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            getTranslatedText("Tus ideas comienzan aquí ✨", language), 
            color = Color.White, 
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            getTranslatedText("Crea tu primera nota inteligente.", language), 
            color = GrayText.copy(alpha = 0.6f), 
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
