package com.example.milista

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.milista.receiver.AlarmReceiver
import com.example.milista.ui.*
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getLocaleCode
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: MiListaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val alarmTitle = intent.getStringExtra("title") ?: ""
        
        enableEdgeToEdge()
        setContent {
            // Estado local para evitar navegación prematura y loops
            var showSplash by remember { mutableStateOf(true) }
            var isRefreshing by remember { mutableStateOf(false) }
            
            val selectedFont by viewModel.selectedFont.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            val selectedFontSize by viewModel.selectedFontSize.collectAsState()
            val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
            val isApplyingChanges by viewModel.isApplyingChanges.collectAsState()
            
            // Sincronización de Locale mejorada para evitar loops infinitos
            LaunchedEffect(selectedLanguage) {
                val targetCode = getLocaleCode(selectedLanguage)
                val currentLocale = Locale.getDefault().language
                
                if (currentLocale != targetCode) {
                    val locale = Locale(targetCode)
                    Locale.setDefault(locale)
                    val resources = baseContext.resources
                    val configuration = resources.configuration
                    configuration.setLocale(locale)
                    resources.updateConfiguration(configuration, resources.displayMetrics)
                    
                    // Efecto de parpadeo premium AMOLED
                    isRefreshing = true
                    kotlinx.coroutines.delay(200)
                    isRefreshing = false
                }
            }
            
            NoctraTheme(
                selectedFontFamily = selectedFont.fontFamily,
                selectedTheme = selectedTheme,
                selectedFontSize = selectedFontSize
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundDark) {
                    if (showSplash) {
                        SplashScreen(onStart = { showSplash = false })
                    } else if (!onboardingCompleted) {
                        OnboardingScreen(onFinished = { viewModel.completeOnboarding() })
                    } else if (alarmId != -1) {
                        AlarmScreen(
                            title = alarmTitle,
                            onSnooze = {
                                sendBroadcast(Intent(this@MainActivity, AlarmReceiver::class.java).apply {
                                    action = AlarmReceiver.ACTION_SNOOZE
                                    putExtra("alarm_id", alarmId)
                                    putExtra("title", alarmTitle)
                                })
                                finish()
                            },
                            onDismiss = {
                                sendBroadcast(Intent(this@MainActivity, AlarmReceiver::class.java).apply {
                                    action = AlarmReceiver.ACTION_DISMISS
                                    putExtra("alarm_id", alarmId)
                                })
                                finish()
                            }
                        )
                    } else {
                        val layoutDirection = if (getLocaleCode(selectedLanguage) == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
                        
                        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                            // KEY ÚNICA: Recrea la UI solo cuando el idioma cambia REALMENTE
                            key(selectedLanguage) {
                                val navController = rememberNavController()
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                                val refreshAlpha by animateFloatAsState(
                                    targetValue = if (isRefreshing) 0f else 1f,
                                    animationSpec = tween(300),
                                    label = "refreshAlpha"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(alpha = refreshAlpha)
                                ) {
                                    // Fondo Atmosférico Noctra
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.mapuche), 
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().blur(60.dp).graphicsLayer(alpha = 0.2f)
                                        )
                                        Box(
                                            modifier = Modifier.fillMaxSize().drawBehind {
                                                drawRect(Brush.radialGradient(
                                                    colors = listOf(NeonGreen.copy(alpha = 0.08f), Color.Transparent),
                                                    center = Offset(size.width / 2, -size.height * 0.1f),
                                                    radius = size.width * 1.5f
                                                ))
                                            }
                                        )
                                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.8f), BackgroundDark.copy(0.98f)))))
                                    }

                                    Scaffold(
                                        containerColor = Color.Transparent,
                                        modifier = Modifier.blur(if (isApplyingChanges) 5.dp else 0.dp),
                                        bottomBar = {
                                            val hideBarRoutes = listOf("note_editor", "add_reminder", "task_list")
                                            if (hideBarRoutes.none { currentRoute.startsWith(it) }) {
                                                PremiumBottomBar(
                                                    onNavigateToHome = { navController.navigate("home") { launchSingleTop = true } },
                                                    onNavigateToCalendar = { navController.navigate("calendar") { launchSingleTop = true } },
                                                    onNavigateToClock = { navController.navigate("clock") { launchSingleTop = true } },
                                                    onNavigateToNotes = { navController.navigate("notes") { launchSingleTop = true } },
                                                    onNavigateToLists = { navController.navigate("listas") { launchSingleTop = true } },
                                                    onNavigateToReminders = { navController.navigate("reminders") { launchSingleTop = true } },
                                                    onNavigateToSettings = { navController.navigate("settings") { launchSingleTop = true } },
                                                    currentRoute = currentRoute
                                                )
                                            }
                                        }
                                    ) { padding ->
                                        NavHost(
                                            navController = navController, 
                                            startDestination = "home",
                                            modifier = Modifier.padding(padding),
                                            enterTransition = { fadeIn(tween(400)) },
                                            exitTransition = { fadeOut(tween(400)) }
                                        ) {
                                            composable("home") {
                                                HomeScreen(
                                                    viewModel = viewModel,
                                                    onAddReminder = { tipo -> navController.navigate("add_reminder/$tipo") },
                                                    onNavigateToClock = { navController.navigate("clock") },
                                                    onNavigateToCalendar = { navController.navigate("calendar") },
                                                    onNavigateToListas = { navController.navigate("listas") },
                                                    onNavigateToNotes = { navController.navigate("notes") }
                                                )
                                            }
                                            composable("clock") { ClockScreen(viewModel = viewModel) }
                                            composable("listas") { 
                                                ListasScreen(
                                                    viewModel = viewModel, 
                                                    onNavigateToListDetails = { listaId -> navController.navigate("task_list/$listaId") }
                                                ) 
                                            }
                                            composable(
                                                "task_list/{listaId}",
                                                arguments = listOf(navArgument("listaId") { type = NavType.IntType })
                                            ) { backStackEntry ->
                                                val listaId = backStackEntry.arguments?.getInt("listaId") ?: 0
                                                TaskListScreen(viewModel = viewModel, listaId = listaId, onBack = { navController.popBackStack() })
                                            }
                                            composable("notes") {
                                                val id by viewModel.quickNotesId.collectAsState()
                                                id?.let {
                                                    NotesScreen(
                                                        viewModel = viewModel,
                                                        listaId = it,
                                                        onBack = { navController.popBackStack() },
                                                        onNavigateToEditor = { lId, tId -> navController.navigate("note_editor/$lId/$tId") }
                                                    )
                                                }
                                            }
                                            composable(
                                                "note_editor/{listaId}/{tareaId}",
                                                arguments = listOf(navArgument("listaId") { type = NavType.IntType }, navArgument("tareaId") { type = NavType.IntType })
                                            ) { backStackEntry ->
                                                val lId = backStackEntry.arguments?.getInt("listaId") ?: 0
                                                val tId = backStackEntry.arguments?.getInt("tareaId") ?: -1
                                                NoteEditorScreen(viewModel = viewModel, listaId = lId, tareaId = tId, onBack = { navController.popBackStack() })
                                            }
                                            composable(
                                                "add_reminder/{tipo}?reminderId={reminderId}",
                                                arguments = listOf(
                                                    navArgument("tipo") { type = NavType.StringType },
                                                    navArgument("reminderId") { type = NavType.IntType; defaultValue = -1 }
                                                )
                                            ) { backStackEntry ->
                                                val tipo = backStackEntry.arguments?.getString("tipo") ?: "Otro"
                                                val rId = backStackEntry.arguments?.getInt("reminderId") ?: -1
                                                AddReminderScreen(viewModel = viewModel, tipo = tipo, reminderId = rId, onBack = { navController.popBackStack() })
                                            }
                                            composable("settings") { SettingsScreen(viewModel = viewModel) }
                                            composable("calendar") { CalendarScreen(viewModel = viewModel) }
                                            composable("reminders") {
                                                ReminderScreen(
                                                    viewModel = viewModel,
                                                    onBack = { navController.popBackStack() },
                                                    onNavigateToAdd = { tipo -> navController.navigate("add_reminder/$tipo") }
                                                )
                                            }
                                        }
                                    }

                                    // Overlay de carga Premium (Barra Linear)
                                    AnimatedVisibility(
                                        visible = isApplyingChanges,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.applying_changes),
                                                    color = Color.White,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 20.dp)
                                                )
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.1f))
                                                ) {
                                                    val progress by animateFloatAsState(
                                                        targetValue = if (isApplyingChanges) 1f else 0f,
                                                        animationSpec = tween(1500),
                                                        label = "languageProgress"
                                                    )
                                                    Box(
                                                        modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                                                            .background(Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.7f), NeonGreen)))
                                                            .shadow(12.dp, RoundedCornerShape(2.dp), spotColor = NeonGreen)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
