package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.R
import com.example.milista.data.Lista
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListasScreen(
    viewModel: MiListaViewModel,
    onNavigateToListDetails: (Int) -> Unit // Mantenemos para compatibilidad aunque no lo usemos ahora
) {
    val allListas by viewModel.listas.collectAsState()
    val quickNotesId by viewModel.quickNotesId.collectAsState()
    
    val listas = remember(allListas, quickNotesId) {
        allListas.filter { it.id != quickNotesId }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Fondo Noctra Premium
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SamsungGreen.copy(alpha = 0.03f), Color.Transparent),
                            center = Offset(size.width * 0.1f, size.height * 0.1f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ListasHeaderPremium()
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Nueva Lista Superior
                item {
                    NewListButton(onClick = { showAddDialog = true })
                }

                // Listas Activas
                val activeListas = listas.filter { !it.esCompletada }
                if (activeListas.isNotEmpty()) {
                    items(activeListas) { lista ->
                        ShoppingListCard(
                            lista = lista,
                            viewModel = viewModel,
                            onDelete = { viewModel.borrarLista(lista) },
                            onFinish = { viewModel.finalizarCompra(lista, 0.0) }
                        )
                    }
                } else {
                    item {
                        EmptyListState()
                    }
                }

                // Sección Completadas
                val completedListas = listas.filter { it.esCompletada }
                if (completedListas.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completadas",
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(completedListas) { lista ->
                        CompletedListCard(
                            lista = lista,
                            onDelete = { viewModel.borrarLista(lista) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = CardDark,
                shape = RoundedCornerShape(28.dp),
                title = { Text("Nueva Lista de Compra", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text("Nombre de la lista (ej. Feria)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SamsungGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newListName.isNotBlank()) {
                                viewModel.agregarLista(newListName)
                                newListName = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Crear", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar", color = GrayText)
                    }
                }
            )
        }
    }
}

@Composable
fun ListasHeaderPremium() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
        Text(
            text = "Compras",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Text(
            text = "Gestiona tus listas de forma independiente",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun NewListButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SamsungGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, SamsungGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.Add, null, tint = SamsungGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Nueva lista de compra", color = SamsungGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShoppingListCard(
    lista: Lista,
    viewModel: MiListaViewModel,
    onDelete: () -> Unit,
    onFinish: () -> Unit
) {
    val itemsByList by viewModel.repository.obtenerTareasPorLista(lista.id).collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(true) }
    var newItemName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header de la Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(SamsungGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ShoppingCart, null, tint = SamsungGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(lista.nombre, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        Text("${itemsByList.count { it.estaCompletada }}/${itemsByList.size} productos", color = GrayText, fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, null, tint = Color.White.copy(alpha = 0.2f))
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Lista de Productos
                itemsByList.forEach { product ->
                    ProductRow(
                        product = product,
                        onToggle = { viewModel.toggleTarea(product) },
                        onDelete = { viewModel.borrarTarea(product) }
                    )
                }

                // Input para nuevo producto
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (newItemName.isEmpty()) {
                                    Text("Agregar producto...", color = GrayText, fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                viewModel.agregarTarea(newItemName, lista.id)
                                newItemName = ""
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = SamsungGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Botón Finalizar
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text("Terminar lista", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProductRow(
    product: Tarea,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = if (product.estaCompletada) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (product.estaCompletada) SamsungGreen else GrayText
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = product.titulo,
            modifier = Modifier.weight(1f).clickable { onToggle() },
            color = if (product.estaCompletada) Color.White.copy(alpha = 0.3f) else Color.White,
            textDecoration = if (product.estaCompletada) TextDecoration.LineThrough else null,
            fontSize = 15.sp
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Rounded.Close, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CompletedListCard(lista: Lista, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().alpha(0.5f),
        shape = RoundedCornerShape(22.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, null, tint = GrayText)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(lista.nombre, fontWeight = FontWeight.Bold, color = Color.White, textDecoration = TextDecoration.LineThrough)
                    Text("Completada el ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(lista.fechaFinalizacion ?: 0))}", color = GrayText, fontSize = 11.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null, tint = GrayText)
            }
        }
    }
}

@Composable
fun EmptyListState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.ShoppingCart, null, tint = Color.White.copy(alpha = 0.05f), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay listas de compra", color = GrayText, fontSize = 14.sp)
    }
}
