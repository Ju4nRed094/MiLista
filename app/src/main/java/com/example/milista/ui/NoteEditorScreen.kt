package com.example.milista.ui

import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.milista.R
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.DrawingUtils
import com.example.milista.ui.utils.PointData
import kotlinx.coroutines.delay
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: MiListaViewModel,
    listaId: Int,
    tareaId: Int = -1,
    onBack: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var color by remember { mutableStateOf<Int?>(null) }
    var esFavorita by remember { mutableStateOf(false) }
    var isDrawingMode by remember { mutableStateOf(false) }
    var currentDrawingColor by remember { mutableStateOf(Color.White) }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    
    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    val pathsPoints = remember { mutableStateListOf<List<PointData>>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints by remember { mutableStateOf<List<PointData>?>(null) }

    val tareasByLista by viewModel.repository.obtenerTareasPorLista(listaId).collectAsState(initial = emptyList())
    val tareaEditar = remember(tareasByLista, tareaId) { tareasByLista.find { it.id == tareaId } }

    var isInitializing by remember { mutableStateOf(true) }

    LaunchedEffect(tareaEditar) {
        tareaEditar?.let {
            titulo = it.titulo
            contenido = it.contenido
            color = it.color
            esFavorita = it.esFavorita
            imageUri = it.imagenPath?.let { path -> Uri.parse(path) }
            audioPath = it.audioPath
            
            if (it.dibujoData != null && it.dibujoData != "HAS_DRAWING" && it.dibujoData.isNotEmpty()) {
                val decoded = DrawingUtils.deserializePaths(it.dibujoData)
                paths.clear()
                paths.addAll(decoded.map { (p, c) -> p to Color(c) })
                
                val points = DrawingUtils.deserializeToPointData(it.dibujoData)
                pathsPoints.clear()
                pathsPoints.addAll(points)
            }
        }
        isInitializing = false
    }
    
    // Auto-save logic
    LaunchedEffect(titulo, contenido, esFavorita, color, imageUri, audioPath) {
        if (isInitializing) return@LaunchedEffect
        
        // Esperar un momento antes de guardar para no saturar la BD
        delay(1000)
        
        val drawingSerialized = DrawingUtils.serializePaths(
            paths.map { it.first to it.second.toArgb() },
            pathsPoints
        )
        
        val t = Tarea(
            id = if (tareaId == -1) 0 else tareaId,
            listaId = listaId,
            titulo = titulo,
            contenido = contenido,
            color = color,
            esFavorita = esFavorita,
            esNotaEnriquecida = true,
            dibujoData = if (paths.isEmpty()) null else drawingSerialized,
            imagenPath = imageUri?.toString(),
            audioPath = audioPath
        )
        
        if (tareaId != -1 || t.titulo.isNotBlank() || t.contenido.isNotBlank()) {
            if (tareaId == -1) {
                // Para nuevas notas, el ViewModel debería retornar el ID para evitar crear duplicados en el auto-save
                // Por ahora, asumimos que el usuario guardará manualmente la primera vez o implementamos una lógica de "draft"
                // Pero el requerimiento dice "guardado automático", así que para una nueva nota:
                // viewModel.agregarTarea(t.titulo, t.listaId, t.contenido, t.color, t.dibujoData, t.imagenPath, t.audioPath)
                // Nota: viewModel.agregarTarea actualmente no retorna el ID, esto podría causar duplicados si el usuario sigue escribiendo.
            } else {
                viewModel.actualizarTarea(t)
            }
        }
    }

    val saveAndBack = {
        val drawingSerialized = DrawingUtils.serializePaths(
            paths.map { it.first to it.second.toArgb() },
            pathsPoints
        )
        if (tareaId == -1) {
            if (titulo.isNotBlank() || contenido.isNotBlank() || paths.isNotEmpty() || imageUri != null || audioPath != null) {
                viewModel.agregarTarea(
                    titulo = titulo, 
                    listaId = listaId, 
                    contenido = contenido, 
                    color = color, 
                    dibujoData = if (paths.isEmpty()) null else drawingSerialized,
                    imagenPath = imageUri?.toString(),
                    audioPath = audioPath
                )
            }
        } else {
            val t = Tarea(
                id = tareaId,
                listaId = listaId,
                titulo = titulo,
                contenido = contenido,
                color = color,
                esFavorita = esFavorita,
                esNotaEnriquecida = true,
                dibujoData = if (paths.isEmpty()) null else drawingSerialized,
                imagenPath = imageUri?.toString(),
                audioPath = audioPath
            )
            viewModel.actualizarTarea(t)
        }
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { saveAndBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { esFavorita = !esFavorita }) {
                            Icon(
                                if (esFavorita) Icons.Rounded.PushPin else Icons.Rounded.PushPin, 
                                null, 
                                tint = if (esFavorita) SamsungGreen else Color.White.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(onClick = { saveAndBack() }) {
                            Icon(Icons.Rounded.Check, null, tint = SamsungGreen)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                if (!isDrawingMode) {
                    NoteEditorToolbarPremium(
                        onImageClick = { imagePickerLauncher.launch("image/*") },
                        onDrawingToggle = { isDrawingMode = true },
                        isRecording = isRecording,
                        onRecordingToggle = {
                            if (isRecording) {
                                recorder?.stop()
                                recorder?.release()
                                recorder = null
                                isRecording = false
                            } else {
                                try {
                                    val file = File(context.cacheDir, "record_${System.currentTimeMillis()}.mp3")
                                    audioPath = file.absolutePath
                                    recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
                                    recorder?.apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                        setOutputFile(audioPath)
                                        prepare()
                                        start()
                                    }
                                    isRecording = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Título con estilo Samsung Notes
                BasicTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(SamsungGreen),
                    decorationBox = { innerTextField ->
                        if (titulo.isEmpty()) Text(stringResource(R.string.title), color = Color.White.copy(alpha = 0.2f), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        innerTextField()
                    }
                )

                // Contenido Multimedia
                if (imageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (audioPath != null) {
                    AudioAttachmentPreviewLocal(audioPath!!, onRemove = { audioPath = null })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Área de texto principal
                BasicTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 300.dp),
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.White.copy(0.8f), lineHeight = 28.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(SamsungGreen),
                    decorationBox = { innerTextField ->
                        if (contenido.isEmpty()) Text(stringResource(R.string.write_something_smart), color = Color.White.copy(alpha = 0.2f), fontSize = 18.sp)
                        innerTextField()
                    }
                )
            }
        }

        // Overlay de dibujo
        if (isDrawingMode) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.9f))) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPoints = listOf(PointData(offset.x, offset.y, 0))
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    currentPoints = currentPoints?.plus(PointData(change.position.x, change.position.y, 1))
                                    val p = currentPath
                                    currentPath = null
                                    currentPath = p
                                },
                                onDragEnd = {
                                    currentPath?.let { paths.add(it to currentDrawingColor) }
                                    currentPoints?.let { pathsPoints.add(it) }
                                    currentPath = null
                                    currentPoints = null
                                }
                            )
                        }
                ) {
                    paths.forEach { (path, color) ->
                        drawPath(path = path, color = color, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    currentPath?.let { path ->
                        drawPath(path = path, color = currentDrawingColor, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
                
                // Toolbar de dibujo
                Row(
                    modifier = Modifier.align(Alignment.TopCenter).padding(24.dp).background(Color.White.copy(0.1f), CircleShape).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isDrawingMode = false }) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { paths.clear(); pathsPoints.clear() }) {
                        Icon(Icons.Rounded.Delete, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { isDrawingMode = false }) {
                        Icon(Icons.Rounded.Check, null, tint = SamsungGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun NoteEditorToolbarPremium(
    onImageClick: () -> Unit,
    onDrawingToggle: () -> Unit,
    isRecording: Boolean,
    onRecordingToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Checkbox list */ }) { Icon(Icons.Default.CheckBox, null, tint = Color.White.copy(0.6f)) }
            IconButton(onClick = onImageClick) { Icon(Icons.Default.Image, null, tint = Color.White.copy(0.6f)) }
            IconButton(onClick = onDrawingToggle) { Icon(Icons.Default.Brush, null, tint = Color.White.copy(0.6f)) }
            IconButton(onClick = onRecordingToggle) { 
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic, 
                    null, 
                    tint = if (isRecording) SamsungRed else Color.White.copy(0.6f)
                ) 
            }
        }
    }
}

@Composable
fun AudioAttachmentPreviewLocal(path: String, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(SamsungGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = SamsungGreen)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Audio inteligente", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, null, tint = Color.Gray)
            }
        }
    }
}
