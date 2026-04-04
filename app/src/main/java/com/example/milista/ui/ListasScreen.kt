package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.data.Lista
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListasScreen(
    viewModel: MiListaViewModel,
    onNavigateToListDetails: (Int) -> Unit
) {
    val listas by viewModel.listas.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf("Mis listas") }
    var selectedListId by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(listas) {
        if (selectedListId == null && listas.isNotEmpty()) {
            selectedListId = listas.find { !it.esCompletada }?.id ?: listas.first().id
        }
    }

    val selectedList = remember(listas, selectedListId) {
        listas.find { it.id == selectedListId }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SamsungGreen.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width * 0.1f, size.height * 0.1f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.background(Color.Transparent)) {
                    ListasHeaderPremiumLocal(selectedLanguage)
                    ListasTabsPremiumLocal(selectedTab, { selectedTab = it }, selectedLanguage)
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                item {
                    ListasCarouselPremiumLocal(
                        listas = listas.filter { !it.esCompletada },
                        selectedListId = selectedListId,
                        onListSelect = { selectedListId = it },
                        onNewList = { showAddDialog = true },
                        language = selectedLanguage
                    )
                }

                if (selectedTab == "Mis listas" && selectedList != null && !selectedList.esCompletada) {
                    item {
                        ActiveListHeaderPremiumLocal(selectedList, selectedLanguage)
                    }

                    item {
                        val itemsByList by viewModel.repository.obtenerTareasPorLista(selectedList.id).collectAsState(initial = emptyList())
                        Column {
                            ListProgressBarPremiumLocal(itemsByList)
                            
                            val categories = listOf("Productos frescos", "Lácteos", "Despensa", "Bebidas", "Limpieza", "Otros")
                            categories.forEach { cat ->
                                val tareasCat = itemsByList.filter { it.categoriaProducto == cat || (cat == "Otros" && (it.categoriaProducto !in categories)) }
                                if (tareasCat.isNotEmpty()) {
                                    CategorySectionHeaderLocal(cat, selectedLanguage)
                                    tareasCat.forEach { tarea ->
                                        ShoppingItemRowLocal(
                                            tarea = tarea,
                                            onToggle = { viewModel.toggleTarea(tarea) },
                                            onUpdateCantidad = { viewModel.actualizarCantidadTarea(tarea, it) },
                                            language = selectedLanguage
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == "Historial") {
                    val historial = listas.filter { it.esCompletada }.sortedByDescending { it.fechaFinalizacion }
                    items(historial) { list ->
                        HistorialListCardLocal(list, selectedLanguage)
                    }
                } else {
                    item {
                        EmptyListStateLocalPremium(selectedLanguage)
                    }
                }
            }
        }

        if (selectedTab == "Mis listas" && selectedList != null && !selectedList.esCompletada) {
            val currentTareas by viewModel.repository.obtenerTareasPorLista(selectedList.id).collectAsState(initial = emptyList())
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                FinalizePurchaseCardLocal(
                    total = currentTareas.sumOf { it.precio * it.cantidad },
                    boughtCount = currentTareas.count { it.estaCompletada },
                    totalCount = currentTareas.size,
                    onFinalize = { viewModel.finalizarCompra(selectedList, currentTareas.sumOf { it.precio * it.cantidad }) },
                    language = selectedLanguage
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = CardDark,
                title = { Text(getTranslatedText("Nueva lista", selectedLanguage), color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text(getTranslatedText("Nombre", selectedLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newListName.isNotBlank()) {
                            viewModel.agregarLista(newListName) { id ->
                                selectedListId = id.toInt()
                            }
                            newListName = ""
                            showAddDialog = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen)) {
                        Text(getTranslatedText("Crear", selectedLanguage), color = Color.Black)
                    }
                }
            )
        }
    }
}

@Composable
fun ListasHeaderPremiumLocal(language: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getTranslatedText("Listas", language),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.ShoppingCart, null, tint = SamsungGreen, modifier = Modifier.size(28.dp).offset(y = 4.dp))
            }
            Text(getTranslatedText("Organiza tus compras fácilmente.", language), style = MaterialTheme.typography.bodyMedium, color = GrayText.copy(alpha = 0.7f))
        }
        Row {
            HeaderIconButtonListasLocal(Icons.Default.Search)
            Spacer(modifier = Modifier.width(12.dp))
            HeaderIconButtonListasLocal(Icons.Default.FilterList)
            Spacer(modifier = Modifier.width(12.dp))
            HeaderIconButtonListasLocal(Icons.Default.MoreVert)
        }
    }
}

@Composable
fun HeaderIconButtonListasLocal(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun ListasTabsPremiumLocal(selectedTab: String, onTabSelect: (String) -> Unit, language: String) {
    val tabs = listOf("Mis listas", "Plantillas", "Compartidas", "Historial")
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = Color.Transparent,
        contentColor = SamsungGreen,
        edgePadding = 24.dp,
        divider = {},
        indicator = { tabPositions ->
            if (tabs.indexOf(selectedTab) != -1) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)])
                        .height(3.dp)
                        .padding(horizontal = 20.dp)
                        .background(SamsungGreen, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                )
            }
        }
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                text = {
                    Text(text = getTranslatedText(tab, language), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) SamsungGreen else GrayText)
                }
            )
        }
    }
}

@Composable
fun ListasCarouselPremiumLocal(listas: List<Lista>, selectedListId: Int?, onListSelect: (Int) -> Unit, onNewList: () -> Unit, language: String) {
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        item { NewListCardPremiumLocal(onNewList, language) }
        items(listas) { lista ->
            ListCardCompactPremiumLocal(lista = lista, isSelected = selectedListId == lista.id, onClick = { onListSelect(lista.id) }, language = language)
        }
    }
}

@Composable
fun NewListCardPremiumLocal(onClick: () -> Unit, language: String) {
    Surface(onClick = onClick, modifier = Modifier.size(width = 140.dp, height = 160.dp), shape = RoundedCornerShape(28.dp), color = SamsungGreen.copy(alpha = 0.05f), border = BorderStroke(2.dp, SamsungGreen.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(SamsungGreen.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = SamsungGreen, modifier = Modifier.size(28.dp)) }
            Spacer(modifier = Modifier.height(16.dp))
            Text(getTranslatedText("Nueva lista", language), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(getTranslatedText("Crear desde cero", language), color = GrayText, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ListCardCompactPremiumLocal(lista: Lista, isSelected: Boolean, onClick: () -> Unit, language: String) {
    Surface(onClick = onClick, modifier = Modifier.size(width = 140.dp, height = 160.dp), shape = RoundedCornerShape(28.dp), color = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f), border = BorderStroke(1.dp, if (isSelected) SamsungGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f))) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(40.dp).background(if (isSelected) SamsungGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.ShoppingCart, null, tint = if (isSelected) SamsungGreen else Color.White, modifier = Modifier.size(20.dp)) }
            Column {
                Text(lista.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Artículos", color = GrayText, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ActiveListHeaderPremiumLocal(lista: Lista, language: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShoppingCart, null, tint = SamsungGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(lista.nombre, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Edit, null, tint = GrayText, modifier = Modifier.size(16.dp))
                }
                Text("Artículos • Hoy 10:30 AM", fontSize = 12.sp, color = GrayText)
            }
        }
        Row {
            HeaderIconButtonListasLocal(Icons.Default.Share)
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButtonListasLocal(Icons.Default.Tune)
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButtonListasLocal(Icons.Default.MoreVert)
        }
    }
}

@Composable
fun ListProgressBarPremiumLocal(items: List<Tarea>) {
    val total = items.size
    val bought = items.count { it.estaCompletada }
    val progress = if (total > 0) bought.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progreso de la lista", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
            Text("$bought / $total", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))) {
            Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(CircleShape).background(Brush.horizontalGradient(listOf(SamsungGreen, SamsungGreen.copy(alpha = 0.7f)))))
        }
        Text(text = "${(progress * 100).toInt()}%", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 10.sp, color = SamsungGreen, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategorySectionHeaderLocal(title: String, language: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        val icon = when(title) {
            "Productos frescos" -> Icons.Default.Agriculture
            "Lácteos" -> Icons.Default.Kitchen
            "Despensa" -> Icons.Default.Inventory
            "Bebidas" -> Icons.Default.LocalBar
            "Limpieza" -> Icons.Default.CleanHands
            else -> Icons.Default.Category
        }
        Icon(icon, null, tint = SamsungGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(getTranslatedText(title, language), color = SamsungGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShoppingItemRowLocal(tarea: Tarea, onToggle: () -> Unit, onUpdateCantidad: (Double) -> Unit, language: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), color = if (tarea.estaCompletada) Color.White.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.04f), border = BorderStroke(1.dp, if (tarea.estaCompletada) Color.Transparent else Color.White.copy(alpha = 0.06f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(if (tarea.estaCompletada) SamsungGreen else Color.White.copy(alpha = 0.1f)).clickable { onToggle() }, contentAlignment = Alignment.Center) { if (tarea.estaCompletada) Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.titulo, color = if (tarea.estaCompletada) GrayText.copy(alpha = 0.5f) else Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textDecoration = if (tarea.estaCompletada) TextDecoration.LineThrough else null)
                Text(text = "${tarea.cantidad.toInt()} ${tarea.unidad}", color = GrayText.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            if (!tarea.estaCompletada) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(horizontal = 4.dp)) {
                    IconButton(onClick = { if(tarea.cantidad > 1) onUpdateCantidad(tarea.cantidad - 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = GrayText, modifier = Modifier.size(14.dp)) }
                    Text(tarea.cantidad.toInt().toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { onUpdateCantidad(tarea.cantidad + 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = SamsungGreen, modifier = Modifier.size(14.dp)) }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.MoreVert, null, tint = GrayText.copy(alpha = 0.4f), modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun FinalizePurchaseCardLocal(total: Double, boughtCount: Int, totalCount: Int, onFinalize: () -> Unit, language: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(24.dp).height(100.dp).shadow(24.dp, RoundedCornerShape(32.dp), spotColor = SamsungGreen), shape = RoundedCornerShape(32.dp), color = CardDark.copy(alpha = 0.95f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(52.dp).background(SamsungGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.ShoppingBag, null, tint = SamsungGreen, modifier = Modifier.size(24.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(getTranslatedText("Total estimado", language), fontSize = 11.sp, color = GrayText)
                    Text("$${String.format(Locale.US, "%.2f", total)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SamsungGreen)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(getTranslatedText("Artículos", language), fontSize = 11.sp, color = GrayText)
                Text("$boughtCount de $totalCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = onFinalize, colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), modifier = Modifier.height(36.dp)) {
                    Text(getTranslatedText("Finalizar", language), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun HistorialListCardLocal(lista: Lista, language: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = 0.04f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.History, null, tint = GrayText) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lista.nombre, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${lista.totalEstimado} • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(lista.fechaFinalizacion ?: 0))}", fontSize = 12.sp, color = GrayText)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = GrayText.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun EmptyListStateLocalPremium(language: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(getTranslatedText("No hay nada aquí", language), color = GrayText.copy(alpha = 0.5f))
    }
}
