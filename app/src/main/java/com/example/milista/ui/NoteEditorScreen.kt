package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.media.MediaRecorder
import android.media.MediaPlayer
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.DrawingUtils
import com.example.milista.ui.utils.PointData
import com.example.milista.ui.utils.getTranslatedText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: MiListaViewModel,
    listaId: Int,
    tareaId: Int = -1,
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    
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

    LaunchedEffect(tareaEditar) {
        tareaEditar?.let {
            titulo = it.titulo
            contenido = it.contenido
            color = it.color
            esFavorita = it.esFavorita
            imageUri = it.imagenPath?.let { path -> Uri.parse(path) }
            audioPath = it.audioPath
            
            if (it.dibujoData != null && it.dibujoData != "HAS_DRAWING") {
                val decoded = DrawingUtils.deserializePaths(it.dibujoData)
                paths.clear()
                paths.addAll(decoded.map { (p, c) -> p to Color(c) })
                
                val points = DrawingUtils.deserializeToPointData(it.dibujoData)
                pathsPoints.clear()
                pathsPoints.addAll(points)
            }
        }
    }
    
    var strokeWidth by remember { mutableFloatStateOf(8f) }

    val saveNote = {
        if (titulo.isNotBlank() || contenido.isNotBlank() || paths.isNotEmpty() || imageUri != null || audioPath != null) {
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
                dibujoData = drawingSerialized,
                imagenPath = imageUri?.toString(),
                audioPath = audioPath
            )
            if (tareaId == -1) viewModel.agregarTarea(
                titulo = t.titulo, 
                listaId = t.listaId, 
                contenido = t.contenido, 
                color = t.color, 
                dibujoData = t.dibujoData,
                imagenPath = t.imagenPath,
                audioPath = t.audioPath
            )
            else viewModel.actualizarTarea(t)
            onBack()
        } else {
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        BasicTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (titulo.isEmpty()) Text(getTranslatedText("Título", selectedLanguage), color = GrayText.copy(alpha = 0.5f), fontSize = 20.sp)
                                innerTextField()
                            }
                        )
                    },
                    navigationIcon = {
                        HeaderCircleButtonEditor(Icons.AutoMirrored.Filled.ArrowBack, onClick = { saveNote() })
                    },
                    actions = {
                        HeaderCircleButtonEditor(if (esFavorita) Icons.Default.Star else Icons.Default.StarBorder, onClick = { esFavorita = !esFavorita })
                        Spacer(modifier = Modifier.width(8.dp))
                        HeaderCircleButtonEditor(Icons.Default.Check, onClick = { saveNote() })
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                NoteEditorToolbar(
                    isDrawingMode = isDrawingMode,
                    onDrawingToggle = { isDrawingMode = !isDrawingMode },
                    onImageClick = { imagePickerLauncher.launch("image/*") },
                    isRecording = isRecording,
                    onRecordingToggle = {
                        if (isRecording) {
                            recorder?.stop()
                            recorder?.release()
                            recorder = null
                            isRecording = false
                        } else {
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
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    imageUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    audioPath?.let { path ->
                        AudioAttachmentPreviewLocal(path, onRemove = { audioPath = null })
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    BasicTextField(
                        value = contenido,
                        onValueChange = { contenido = it },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 400.dp),
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.White.copy(0.9f), lineHeight = 24.sp),
                        decorationBox = { innerTextField ->
                            if (contenido.isEmpty()) Text(getTranslatedText("Escribe algo inteligente...", selectedLanguage), color = GrayText.copy(alpha = 0.4f))
                            innerTextField()
                        }
                    )
                }

                if (isDrawingMode) {
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
                            drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                        currentPath?.let { path ->
                            drawPath(path = path, color = currentDrawingColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCircleButtonEditor(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun NoteEditorToolbar(
    isDrawingMode: Boolean,
    onDrawingToggle: () -> Unit,
    onImageClick: () -> Unit,
    isRecording: Boolean,
    onRecordingToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
            .height(64.dp),
        color = CardDark.copy(alpha = 0.9f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButtonLocal(Icons.Default.TextFields, onClick = { /* Text Style */ })
            ToolbarButtonLocal(Icons.Default.Checklist, onClick = { /* Checklist */ })
            ToolbarButtonLocal(Icons.Default.Brush, active = isDrawingMode, onClick = onDrawingToggle)
            ToolbarButtonLocal(Icons.Default.Image, onClick = onImageClick)
            ToolbarButtonLocal(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, active = isRecording, activeColor = SamsungRed, onClick = onRecordingToggle)
            ToolbarButtonLocal(Icons.Default.AttachFile, onClick = { /* Attachments */ })
        }
    }
}

@Composable
fun ToolbarButtonLocal(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    active: Boolean = false, 
    activeColor: Color = SamsungGreen,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            icon, 
            null, 
            tint = if (active) activeColor else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AudioAttachmentPreviewLocal(path: String, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
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
                Icon(Icons.Default.Close, null, tint = GrayText)
            }
        }
    }
}
