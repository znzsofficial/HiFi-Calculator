package com.nekolaska.hificalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.NoiseAware
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.nekolaska.hificalculator.ui.theme.HiFiCalculatorTheme
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.core.net.toUri

// --- INTERFACES & ENUMS ---
interface Labeled {
    val label: String
}

enum class PowerUnit(override val label: String) : Labeled {
    W("W"),
    mW("mW")
}

enum class VoltageUnit(override val label: String) : Labeled {
    Vrms("Vrms"),
    Vpp("Vp-p")
}

// --- DATA STRUCTURES ---
data class CalculatorItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

data class CalculatorCategory(
    val title: String,
    val items: List<CalculatorItem>
)

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiFiCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HiFiCalculatorApp()
                }
            }
        }
    }
}

// --- APP NAVIGATION ---
@Composable
fun HiFiCalculatorApp() {
    val navController = rememberNavController()
    val categorizedCalculators = listOf(
        CalculatorCategory(
            title = "基础电学与换算",
            items = listOf(
                CalculatorItem("power_impedance_voltage", "功率/电压/阻抗", Icons.Outlined.Bolt),
                CalculatorItem("sensitivity_conversion", "灵敏度转换", Icons.Outlined.SwapHoriz),
                CalculatorItem("wavelength", "声波波长计算", Icons.Outlined.Waves)
            )
        ),
        CalculatorCategory(
            title = "耳机与功放匹配",
            items = listOf(
                CalculatorItem(
                    "required_power_for_spl",
                    "指定响度需求",
                    Icons.AutoMirrored.Outlined.VolumeUp
                ),
                CalculatorItem(
                    "input_sensitivity",
                    "功放输入灵敏度",
                    Icons.AutoMirrored.Outlined.Input
                ),
                CalculatorItem(
                    "amplifier_gain",
                    "功放增益",
                    Icons.AutoMirrored.Outlined.TrendingUp
                ),
                CalculatorItem("damping_factor", "阻尼系数", Icons.Outlined.GraphicEq),
                CalculatorItem("btl_power", "BTL桥接功率", Icons.Outlined.Link)
            )
        ),
        CalculatorCategory(
            title = "声学与数字音频",
            items = listOf(
                CalculatorItem("snr_conversion", "信噪比转换", Icons.Outlined.NoiseAware),
                CalculatorItem("dynamic_range", "动态范围/比特深度", Icons.Outlined.Equalizer),
                CalculatorItem("db_combination", "分贝合成", Icons.Outlined.AddCircleOutline),
                CalculatorItem("jitter_converter", "Jitter (抖动) 转换", Icons.Outlined.Timer),
                CalculatorItem("audio_file_size", "音频文件大小/码率", Icons.Outlined.SdStorage),
                CalculatorItem("buffer_latency", "音频缓冲延迟", Icons.Outlined.Schedule)
            )
        ),
        CalculatorCategory(
            title = "时钟与数字信号",
            items = listOf(
                CalculatorItem("ppm_calculator", "晶振精度 (PPM) 偏差", Icons.Outlined.Timer),
                CalculatorItem("dsd_calculator", "DSD 频率换算", Icons.Outlined.GraphicEq),
                CalculatorItem("sample_period", "采样周期 (时间域)", Icons.Outlined.WatchLater)
            )
        )
    )

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController, categorizedCalculators) }
        // Existing Routes
        composable("power_impedance_voltage") { PowerImpedanceVoltageScreen(navController) }
        composable("sensitivity_conversion") { SensitivityConversionScreen(navController) }
        composable("required_power_for_spl") { RequiredPowerForSplScreen(navController) }
        composable("snr_conversion") { SnrConversionScreen(navController) }
        composable("input_sensitivity") { InputSensitivityScreen(navController) }
        composable("amplifier_gain") { AmplifierGainScreen(navController) }
        composable("damping_factor") { DampingFactorScreen(navController) }
        composable("dynamic_range") { DynamicRangeScreen(navController) }
        composable("db_combination") { DbCombinationScreen(navController) }
        composable("jitter_converter") { JitterConverterScreen(navController) }
        composable("audio_file_size") { AudioFileSizeScreen(navController) }
        composable("buffer_latency") { BufferLatencyScreen(navController) }
        composable("wavelength") { WavelengthCalculatorScreen(navController) }
        composable("btl_power") { BtlPowerCalculatorScreen(navController) }

        // --- 新增时钟相关路由 ---
        composable("ppm_calculator") { PpmCalculatorScreen(navController) }
        composable("dsd_calculator") { DsdCalculatorScreen(navController) }
        composable("sample_period") { SamplePeriodScreen(navController) }

        // <-- 2. 新增"关于"页面的导航路由 -->
        composable("about") { AboutScreen(navController) }
    }
}


// --- UI SCREENS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, categories: List<CalculatorCategory>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HiFi Calculator", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEach { category ->
                item {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )
                }
                items(category.items) { item ->
                    Card(
                        onClick = { navController.navigate(item.route) },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Navigate",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // <-- 3. 在主屏幕底部新增一个"关于"按钮 -->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("about") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = "About")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("关于 & 开源")
                }
            }
        }
    }
}


// 这是一个自定义的 GitHub 图标 ImageVector
val GithubIcon: ImageVector
    get() {
        if (_github != null) {
            return _github!!
        }
        _github = ImageVector.Builder(
            name = "Github", defaultWidth = 24.dp, defaultHeight =
                24.dp, viewportWidth = 98f, viewportHeight = 96f
        ).path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
        ) {
            moveTo(48.854f, 0.0f)
            curveTo(21.839f, 0.0f, 0.0f, 22.0f, 0.0f, 49.217f)
            curveTo(0.0f, 70.884f, 13.923f, 89.55f, 33.254f, 95.845f)
            curveTo(35.694f, 96.286f, 36.53f, 94.837f, 36.53f, 93.613f)
            verticalLineTo(86.34f)
            curveTo(23.27f, 89.18f, 20.35f, 80.41f, 20.35f, 80.41f)
            curveTo(18.22f, 75.03f, 14.82f, 72.82f, 14.82f, 72.82f)
            curveTo(10.58f, 69.83f, 15.18f, 69.83f, 15.18f, 69.83f)
            curveTo(19.82f, 70.15f, 22.55f, 74.83f, 22.55f, 74.83f)
            curveTo(26.6f, 81.88f, 32.94f, 79.83f, 35.62f, 78.54f)
            curveTo(36.0f, 75.83f, 37.13f, 73.85f, 38.27f, 72.9f)
            curveTo(27.91f, 71.76f, 16.96f, 67.9f, 16.96f, 51.53f)
            curveTo(16.96f, 45.9f, 18.84f, 41.5f, 22.0f, 38.09f)
            curveTo(21.54f, 36.95f, 19.88f, 32.1f, 22.45f, 25.44f)
            curveTo(22.45f, 25.44f, 26.2f, 24.28f, 36.33f, 31.03f)
            curveTo(39.88f, 30.07f, 43.66f, 29.59f, 47.43f, 29.59f)
            curveTo(51.2f, 29.59f, 54.98f, 30.07f, 58.53f, 31.03f)
            curveTo(68.66f, 24.28f, 72.41f, 25.44f, 72.41f, 25.44f)
            curveTo(74.98f, 32.1f, 73.32f, 36.95f, 72.86f, 38.09f)
            curveTo(76.02f, 41.5f, 77.9f, 45.9f, 77.9f, 51.53f)
            curveTo(77.9f, 67.93f, 66.95f, 71.75f, 56.57f, 72.88f)
            curveTo(57.94f, 74.0f, 59.22f, 76.3f, 59.22f, 79.94f)
            verticalLineTo(93.61f)
            curveTo(59.22f, 94.83f, 60.06f, 96.28f, 62.51f, 95.84f)
            curveTo(81.91f, 89.54f, 95.82f, 70.87f, 95.82f, 49.21f)
            curveTo(95.82f, 22.0f, 73.98f, 0.0f, 48.85f, 0.0f)
            close()
        }.build()
        return _github!!
    }
private var _github: ImageVector? = null

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/znzsofficial/HiFi-Calculator"

    CalculatorScreen(navController = navController, title = "关于") {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // App Info Section
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "App Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "HiFi Calculator",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.1.1",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Description Section
            item {
                SelectionContainer {
                    Text(
                        text = "一款为高保真音频爱好者、DIY玩家和专业人士设计的计算工具。",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // GitHub Link Card
            item {
                Card(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = GithubIcon,
                            contentDescription = "GitHub",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "在GitHub上查看源码",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Acknowledgements Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SelectionContainer {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "技术栈与致谢",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Kotlin
                            AcknowledgementItem(
                                icon = Icons.Outlined.Code,
                                title = "Kotlin",
                                description = "由JetBrains开发的现代、强大且简洁的编程语言。"
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Jetpack Compose
                            AcknowledgementItem(
                                icon = Icons.Outlined.Palette,
                                title = "Jetpack Compose",
                                description = "由Google打造的声明式UI工具包，用于构建原生Android界面。本项目使用了Compose Foundation、UI、Material 3以及Material Icons。"
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // AndroidX Navigation
                            AcknowledgementItem(
                                icon = Icons.Outlined.Route,
                                title = "AndroidX Navigation",
                                description = "用于实现应用内各计算器页面之间类型安全的导航。"
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "感谢Google和JetBrains团队开发并维护了这些优秀的开源工具。",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 一个用于在"关于"页面中显示技术栈项目的可复用组件。
 */
@Composable
private fun AcknowledgementItem(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    navController: NavController,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

// --- NEW CALCULATOR SCREENS ---
@Composable
fun JitterConverterScreen(navController: NavController) {
    var sampleRateStr by remember { mutableStateOf("44100") }
    var psStr by remember { mutableStateOf("") }
    var uiStr by remember { mutableStateOf("") }
    var isInputPs by remember { mutableStateOf(true) }

    CalculatorScreen(navController, "Jitter (抖动) 转换") {
        OutlinedTextField(
            value = sampleRateStr,
            onValueChange = { sampleRateStr = it },
            label = { Text("采样率 (Hz)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            Text("输入单位:")
            RadioButton(selected = isInputPs, onClick = { isInputPs = true })
            Text("皮秒 (ps)")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !isInputPs, onClick = { isInputPs = false })
            Text("单位间隔 (UI)")
        }
        val sampleRate = sampleRateStr.toDoubleOrNull() ?: 0.0
        OutlinedTextField(
            value = psStr,
            onValueChange = {
                psStr = it
                if (isInputPs) {
                    val ps = it.toDoubleOrNull()
                    uiStr = if (ps != null && sampleRate > 0) {
                        String.format("%.6f", ps * sampleRate / 1_000_000_000_000.0)
                    } else ""
                }
            },
            label = { Text("抖动 (ps)") },
            enabled = isInputPs,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = uiStr,
            onValueChange = {
                uiStr = it
                if (!isInputPs) {
                    val ui = it.toDoubleOrNull()
                    psStr = if (ui != null && sampleRate > 0) {
                        String.format("%.6f", (ui / sampleRate) * 1_000_000_000_000.0)
                    } else ""
                }
            },
            label = { Text("抖动 (UI)") },
            enabled = !isInputPs,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun AudioFileSizeScreen(navController: NavController) {
    var minutesStr by remember { mutableStateOf("3") }
    var secondsStr by remember { mutableStateOf("30") }
    var sampleRateStr by remember { mutableStateOf("44100") }
    var bitDepthStr by remember { mutableStateOf("16") }
    var channelsStr by remember { mutableStateOf("2") }

    val results by remember(minutesStr, secondsStr, sampleRateStr, bitDepthStr, channelsStr) {
        derivedStateOf {
            val minutes = minutesStr.toLongOrNull() ?: 0L
            val seconds = secondsStr.toLongOrNull() ?: 0L
            val sampleRate = sampleRateStr.toLongOrNull() ?: 0L
            val bitDepth = bitDepthStr.toLongOrNull() ?: 0L
            val channels = channelsStr.toLongOrNull() ?: 0L

            if (sampleRate > 0 && bitDepth > 0 && channels > 0) {
                val totalSeconds = (minutes * 60) + seconds
                val bitrateBps = sampleRate * bitDepth * channels
                val totalBytes = (bitrateBps / 8.0) * totalSeconds

                val bitrateKbps = bitrateBps / 1000.0
                val sizeMB = totalBytes / (1024.0 * 1024.0)

                Pair(bitrateKbps, sizeMB)
            } else {
                Pair(0.0, 0.0)
            }
        }
    }

    CalculatorScreen(navController, "音频文件大小/码率") {
        Row {
            OutlinedTextField(
                value = minutesStr,
                onValueChange = { minutesStr = it },
                label = { Text("分钟") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = secondsStr,
                onValueChange = { secondsStr = it },
                label = { Text("秒") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        OutlinedTextField(
            value = sampleRateStr,
            onValueChange = { sampleRateStr = it },
            label = { Text("采样率 (Hz)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = bitDepthStr,
            onValueChange = { bitDepthStr = it },
            label = { Text("位深度 (bit)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = channelsStr,
            onValueChange = { channelsStr = it },
            label = { Text("声道数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(
            "码率: ${String.format("%.2f", results.first)} kbps",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            "文件大小: ${String.format("%.2f", results.second)} MB",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun BufferLatencyScreen(navController: NavController) {
    var bufferSizeStr by remember { mutableStateOf("256") }
    var sampleRateStr by remember { mutableStateOf("44100") }

    val latencyMs by remember(bufferSizeStr, sampleRateStr) {
        derivedStateOf {
            val bufferSize = bufferSizeStr.toDoubleOrNull() ?: 0.0
            val sampleRate = sampleRateStr.toDoubleOrNull() ?: 0.0
            if (sampleRate > 0) (bufferSize / sampleRate) * 1000.0 else 0.0
        }
    }

    CalculatorScreen(navController, "音频缓冲延迟") {
        OutlinedTextField(
            value = bufferSizeStr,
            onValueChange = { bufferSizeStr = it },
            label = { Text("缓冲区大小 (samples)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = sampleRateStr,
            onValueChange = { sampleRateStr = it },
            label = { Text("采样率 (Hz)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "延迟: ${String.format("%.3f", latencyMs)} ms",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun WavelengthCalculatorScreen(navController: NavController) {
    var frequencyStr by remember { mutableStateOf("") }
    val speedOfSound = 343.0 // m/s in air at 20°C

    val wavelength by remember(frequencyStr) {
        derivedStateOf {
            val frequency = frequencyStr.toDoubleOrNull() ?: 0.0
            if (frequency > 0) speedOfSound / frequency else 0.0
        }
    }
    CalculatorScreen(navController, "声波波长计算") {
        OutlinedTextField(
            value = frequencyStr,
            onValueChange = { frequencyStr = it },
            label = { Text("频率 (Hz)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "波长: ${String.format("%.4f", wavelength)} 米",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("基于空气中声速 (20°C): $speedOfSound m/s", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun BtlPowerCalculatorScreen(navController: NavController) {
    var stereoPowerStr by remember { mutableStateOf("") }
    var impedanceStr by remember { mutableStateOf("") }

    val results by remember(stereoPowerStr, impedanceStr) {
        derivedStateOf {
            val stereoPower = stereoPowerStr.toDoubleOrNull() ?: 0.0
            val impedance = impedanceStr.toDoubleOrNull() ?: 0.0
            if (stereoPower > 0 && impedance > 0) {
                val btlPowerSameLoad = stereoPower * 4
                val btlPowerDoubleLoad = stereoPower * 2
                Triple(btlPowerSameLoad, btlPowerDoubleLoad, impedance)
            } else {
                Triple(0.0, 0.0, 0.0)
            }
        }
    }

    CalculatorScreen(navController, "BTL桥接功率") {
        OutlinedTextField(
            value = stereoPowerStr,
            onValueChange = { stereoPowerStr = it },
            label = { Text("立体声单通道功率 (Watts)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = impedanceStr,
            onValueChange = { impedanceStr = it },
            label = { Text("立体声额定负载阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(
            "理论最大功率 (${results.third.toInt()}Ω): ${
                String.format(
                    "%.1f",
                    results.first
                )
            } Watts",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            "驱动双倍阻抗 (${(results.third * 2).toInt()}Ω): ${
                String.format(
                    "%.1f",
                    results.second
                )
            } Watts",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            "注意: 计算结果为理论值，实际输出受限于功放电源、散热和保护电路。",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PowerImpedanceVoltageScreen(navController: NavController) {
    // 状态管理
    var vStr by remember { mutableStateOf("") }
    var rStr by remember { mutableStateOf("") }
    var pStr by remember { mutableStateOf("") }

    var voltageUnit by remember { mutableStateOf(VoltageUnit.Vrms) }
    var powerUnit by remember { mutableStateOf(PowerUnit.W) }

    // 用于显示错误或提示信息
    val context = androidx.compose.ui.platform.LocalContext.current
    fun showToast(msg: String) {
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    // 核心计算逻辑
    fun calculate() {
        // 1. 获取输入并转换为基础单位 (Vrms, Ohms, Watts)
        val vInput = vStr.toDoubleOrNull()
        val rInput = rStr.toDoubleOrNull()
        val pInput = pStr.toDoubleOrNull()

        // 基础单位转换逻辑
        val vBase = if (vInput != null) {
            if (voltageUnit == VoltageUnit.Vpp) vInput / (2 * sqrt(2.0)) else vInput
        } else null

        val pBase = if (pInput != null) {
            if (powerUnit == PowerUnit.mW) pInput / 1000.0 else pInput
        } else null

        // 2. 判断计算路径 (输入了哪两个值？)
        if (vBase != null && rInput != null) {
            // 已知 电压 & 阻抗 -> 计算 功率
            val pResultBase = vBase.pow(2) / rInput
            // 转换回显示单位
            val pResult = if (powerUnit == PowerUnit.mW) pResultBase * 1000.0 else pResultBase
            pStr = String.format("%.4f", pResult)

        } else if (pBase != null && rInput != null) {
            // 已知 功率 & 阻抗 -> 计算 电压
            val vResultBase = sqrt(pBase * rInput)
            // 转换回显示单位
            val vResult =
                if (voltageUnit == VoltageUnit.Vpp) vResultBase * 2 * sqrt(2.0) else vResultBase
            vStr = String.format("%.4f", vResult)

        } else if (vBase != null && pBase != null) {
            // 已知 电压 & 功率 -> 计算 阻抗
            if (pBase > 0) {
                val rResult = vBase.pow(2) / pBase
                rStr = String.format("%.2f", rResult)
            } else {
                showToast("功率不能为0")
            }
        } else {
            showToast("请输入任意两个数值以计算第三个")
        }
    }

    fun clearAll() {
        vStr = ""
        rStr = ""
        pStr = ""
    }

    CalculatorScreen(navController, "功率/电压/阻抗") {
        // 顶部提示卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "输入任意两项，点击计算按钮得出第三项。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // 输入区域容器
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- 电压输入 ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "电压 (Voltage)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        // 紧凑型单位选择器
                        CompactUnitSwitch(
                            options = VoltageUnit.entries,
                            selected = voltageUnit,
                            onSelected = { voltageUnit = it; calculate() } // 切换单位时尝试重新计算
                        )
                    }
                    OutlinedTextField(
                        value = vStr,
                        onValueChange = { vStr = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入电压...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                voltageUnit.label,
                                modifier = Modifier.padding(end = 12.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                }

                // --- 阻抗输入 ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "阻抗 (Impedance)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = rStr,
                        onValueChange = { rStr = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入阻抗...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                "Ohms",
                                modifier = Modifier.padding(end = 12.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                }

                // --- 功率输入 ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "功率 (Power)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        // 紧凑型单位选择器
                        CompactUnitSwitch(
                            options = PowerUnit.entries,
                            selected = powerUnit,
                            onSelected = { powerUnit = it; calculate() } // 切换单位时尝试重新计算
                        )
                    }
                    OutlinedTextField(
                        value = pStr,
                        onValueChange = { pStr = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入功率...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                powerUnit.label,
                                modifier = Modifier.padding(end = 12.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                }
            }
        }

        // 按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { clearAll() },
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text("清空", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { calculate() },
                modifier = Modifier.weight(2f)
            ) {
                Icon(Icons.Outlined.Calculate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("计算")
            }
        }
    }
}

// 辅助组件：紧凑的单位切换器
@Composable
fun <T : Labeled> CompactUnitSwitch(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            val containerColor =
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            val contentColor =
                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            val border = if (isSelected) null else BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )

            Surface(
                onClick = { onSelected(option) },
                shape = when (index) {
                    0 -> androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp
                    )

                    options.size - 1 -> androidx.compose.foundation.shape.RoundedCornerShape(
                        topEnd = 8.dp,
                        bottomEnd = 8.dp
                    )

                    else -> androidx.compose.ui.graphics.RectangleShape
                },
                color = containerColor,
                border = border,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun <T> UnitSelector(
    selectedLabel: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit
) where T : Enum<T>, T : Labeled {
    Row(Modifier.selectableGroup()) {
        options.forEach { option ->
            Row(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .selectable(
                        selected = (option.label == selectedLabel),
                        onClick = { onOptionSelected(option) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (option.label == selectedLabel), onClick = null)
                Text(text = option.label, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun SensitivityConversionScreen(navController: NavController) {
    var impedanceStr by remember { mutableStateOf("") }
    var dbVStr by remember { mutableStateOf("") }
    var dbMwStr by remember { mutableStateOf("") }
    var isInputDbV by remember { mutableStateOf(true) }

    val impedance = impedanceStr.toDoubleOrNull() ?: 0.0

    CalculatorScreen(navController, "灵敏度转换") {
        OutlinedTextField(
            value = impedanceStr,
            onValueChange = {
                impedanceStr = it
                val newImpedance = it.toDoubleOrNull() ?: 0.0
                if (isInputDbV) {
                    val dbV = dbVStr.toDoubleOrNull()
                    if (dbV != null && newImpedance > 0) {
                        dbMwStr = String.format("%.2f", dbV - 10 * log10(1000 / newImpedance))
                    }
                } else {
                    val dbMw = dbMwStr.toDoubleOrNull()
                    if (dbMw != null && newImpedance > 0) {
                        dbVStr = String.format("%.2f", dbMw + 10 * log10(1000 / newImpedance))
                    }
                }
            },
            label = { Text("阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            Text("输入单位:")
            RadioButton(selected = isInputDbV, onClick = { isInputDbV = true })
            Text("dB/V")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !isInputDbV, onClick = { isInputDbV = false })
            Text("dB/mW")
        }
        OutlinedTextField(
            value = dbVStr,
            onValueChange = {
                dbVStr = it
                if (isInputDbV) {
                    val dbV = it.toDoubleOrNull()
                    if (dbV != null && impedance > 0) {
                        dbMwStr = String.format("%.2f", dbV - 10 * log10(1000 / impedance))
                    } else if (it.isEmpty()) {
                        dbMwStr = ""
                    }
                }
            },
            label = { Text("灵敏度 (dB/V)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isInputDbV
        )
        OutlinedTextField(
            value = dbMwStr,
            onValueChange = {
                dbMwStr = it
                if (!isInputDbV) {
                    val dbMw = it.toDoubleOrNull()
                    if (dbMw != null && impedance > 0) {
                        dbVStr = String.format("%.2f", dbMw + 10 * log10(1000 / impedance))
                    } else if (it.isEmpty()) {
                        dbVStr = ""
                    }
                }
            },
            label = { Text("灵敏度 (dB/mW)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isInputDbV
        )
    }
}

@Composable
fun SnrConversionScreen(navController: NavController) {
    var dbSnr by remember { mutableStateOf("") }
    var percentSnr by remember { mutableStateOf("") }

    CalculatorScreen(navController, "信噪比转换") {
        OutlinedTextField(
            value = dbSnr,
            onValueChange = {
                dbSnr = it
                val newDbValue = it.toDoubleOrNull()
                percentSnr = if (newDbValue != null) {
                    val ratio = 10.0.pow(newDbValue / 10.0)
                    String.format("%.12f", (1 / ratio) * 100)
                } else {
                    ""
                }
            },
            label = { Text("信噪比 (dB)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = percentSnr,
            onValueChange = {
                percentSnr = it
                val newPercentValue = it.toDoubleOrNull()
                dbSnr = if (newPercentValue != null && newPercentValue > 0) {
                    val ratio = 100.0 / newPercentValue
                    String.format("%.2f", 10 * log10(ratio))
                } else {
                    ""
                }
            },
            label = { Text("噪声占信号百分比 (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun RequiredPowerForSplScreen(navController: NavController) {
    var sensitivity by remember { mutableStateOf("") }
    var impedance by remember { mutableStateOf("") }
    var targetSpl by remember { mutableStateOf("") }
    var isInputDbV by remember { mutableStateOf(true) }

    val requiredValues by remember(sensitivity, impedance, targetSpl, isInputDbV) {
        derivedStateOf {
            val sensitivityValue = sensitivity.toDoubleOrNull() ?: 0.0
            val impedanceValue = impedance.toDoubleOrNull() ?: 0.0
            val targetSplValue = targetSpl.toDoubleOrNull() ?: 0.0
            val sensitivityDbV = if (isInputDbV) sensitivityValue else {
                if (impedanceValue > 0) sensitivityValue + 10 * log10(1000 / impedanceValue) else 0.0
            }
            if (sensitivityDbV != 0.0 && impedanceValue > 0.0) {
                val voltage = 10.0.pow((targetSplValue - sensitivityDbV) / 20.0)
                val power = voltage.pow(2) / impedanceValue
                val current = voltage / impedanceValue
                Triple(voltage, power, current)
            } else {
                Triple(0.0, 0.0, 0.0)
            }
        }
    }
    CalculatorScreen(navController, "计算指定响度下的需求") {
        OutlinedTextField(
            value = sensitivity,
            onValueChange = { sensitivity = it },
            label = { Text("灵敏度") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isInputDbV, onClick = { isInputDbV = true })
            Text("dB/V")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !isInputDbV, onClick = { isInputDbV = false })
            Text("dB/mW")
        }
        OutlinedTextField(
            value = impedance,
            onValueChange = { impedance = it },
            label = { Text("阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = targetSpl,
            onValueChange = { targetSpl = it },
            label = { Text("目标响度 (dB SPL)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("所需电压: ${String.format("%.4f", requiredValues.first)} Vrms")
        Text("所需功率: ${String.format("%.4f", requiredValues.second * 1000)} mW")
        Text("所需电流: ${String.format("%.4f", requiredValues.third * 1000)} mA")
    }
}

@Composable
fun InputSensitivityScreen(navController: NavController) {
    var power by remember { mutableStateOf("") }
    var impedance by remember { mutableStateOf("") }
    var gain by remember { mutableStateOf("") }

    val inputSensitivity = remember(power, impedance, gain) {
        val powerValue = power.toDoubleOrNull() ?: 0.0
        val impedanceValue = impedance.toDoubleOrNull() ?: 0.0
        val gainDbValue = gain.toDoubleOrNull() ?: 0.0
        if (powerValue > 0 && impedanceValue > 0 && gainDbValue != 0.0) {
            val vOut = sqrt(powerValue * impedanceValue)
            val voltageGain = 10.0.pow(gainDbValue / 20.0)
            if (voltageGain > 0) vOut / voltageGain else 0.0
        } else 0.0
    }
    CalculatorScreen(navController, "计算功放输入灵敏度") {
        OutlinedTextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("额定功率 (Watts)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = impedance,
            onValueChange = { impedance = it },
            label = { Text("额定阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = gain,
            onValueChange = { gain = it },
            label = { Text("增益 (dB)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "输入灵敏度: ${String.format("%.3f", inputSensitivity)} Vrms",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun AmplifierGainScreen(navController: NavController) {
    var power by remember { mutableStateOf("") }
    var impedance by remember { mutableStateOf("") }
    var sensitivity by remember { mutableStateOf("") }

    val gain = remember(power, impedance, sensitivity) {
        val powerValue = power.toDoubleOrNull() ?: 0.0
        val impedanceValue = impedance.toDoubleOrNull() ?: 0.0
        val sensitivityValue = sensitivity.toDoubleOrNull() ?: 0.0
        if (powerValue > 0 && impedanceValue > 0 && sensitivityValue > 0) {
            val vOut = sqrt(powerValue * impedanceValue)
            val voltageGainRatio = vOut / sensitivityValue
            20 * log10(voltageGainRatio)
        } else 0.0
    }
    CalculatorScreen(navController, "计算功放增益") {
        OutlinedTextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("额定功率 (Watts)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = impedance,
            onValueChange = { impedance = it },
            label = { Text("额定阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = sensitivity,
            onValueChange = { sensitivity = it },
            label = { Text("输入灵敏度 (Vrms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "增益: ${String.format("%.2f", gain)} dB",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun DampingFactorScreen(navController: NavController) {
    var ampImpedance by remember { mutableStateOf("") }
    var speakerImpedance by remember { mutableStateOf("") }

    val dampingFactor = remember(ampImpedance, speakerImpedance) {
        val ampImpedanceValue = ampImpedance.toDoubleOrNull() ?: 0.0
        val speakerImpedanceValue = speakerImpedance.toDoubleOrNull() ?: 0.0
        if (ampImpedanceValue > 0) speakerImpedanceValue / ampImpedanceValue else 0.0
    }
    CalculatorScreen(navController, "计算阻尼系数") {
        OutlinedTextField(
            value = speakerImpedance,
            onValueChange = { speakerImpedance = it },
            label = { Text("耳机/音箱阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = ampImpedance,
            onValueChange = { ampImpedance = it },
            label = { Text("功放输出阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "阻尼系数: ${String.format("%.1f", dampingFactor)}",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun DynamicRangeScreen(navController: NavController) {
    var bitDepth by remember { mutableStateOf("") }
    val dynamicRange = remember(bitDepth) {
        val bitDepthValue = bitDepth.toIntOrNull() ?: 0
        if (bitDepthValue > 0) bitDepthValue * 6.02 else 0.0
    }
    CalculatorScreen(navController, "动态范围/比特深度转换") {
        OutlinedTextField(
            value = bitDepth,
            onValueChange = { bitDepth = it },
            label = { Text("比特深度 (e.g., 16, 24)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "理论动态范围: ${String.format("%.2f", dynamicRange)} dB",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun DbCombinationScreen(navController: NavController) {
    var db1 by remember { mutableStateOf("") }
    var db2 by remember { mutableStateOf("") }

    val totalDb = remember(db1, db2) {
        val db1Value = db1.toDoubleOrNull()
        val db2Value = db2.toDoubleOrNull()
        if (db1Value != null && db2Value != null) {
            10 * log10(10.0.pow(db1Value / 10.0) + 10.0.pow(db2Value / 10.0))
        } else 0.0
    }
    CalculatorScreen(navController, "分贝合成") {
        OutlinedTextField(
            value = db1,
            onValueChange = { db1 = it },
            label = { Text("声压级 1 (dB SPL)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = db2,
            onValueChange = { db2 = it },
            label = { Text("声压级 2 (dB SPL)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            "合成后总声压级: ${String.format("%.2f", totalDb)} dB",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

// --- NEW CLOCK CALCULATORS ---

@Composable
fun PpmCalculatorScreen(navController: NavController) {
    var frequencyStr by remember { mutableStateOf("10000000") } // 默认 10MHz (常见外部时钟)
    var ppmStr by remember { mutableStateOf("1.0") }

    val results by remember(frequencyStr, ppmStr) {
        derivedStateOf {
            val freq = frequencyStr.toDoubleOrNull() ?: 0.0
            val ppm = ppmStr.toDoubleOrNull() ?: 0.0

            if (freq > 0) {
                val deviationHz = freq * (ppm / 1_000_000.0)
                // 一天有 86400 秒
                val secondsPerDayError = 86400.0 * (ppm / 1_000_000.0)
                Pair(deviationHz, secondsPerDayError)
            } else {
                Pair(0.0, 0.0)
            }
        }
    }

    CalculatorScreen(navController, "晶振精度 (PPM) 偏差") {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PPM (百万分率) 是衡量晶振精度的单位。HiFi设备中常见的 TCXO 为 1-10ppm，OCXO 可达 0.1ppm 甚至更低。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        OutlinedTextField(
            value = frequencyStr,
            onValueChange = { frequencyStr = it },
            label = { Text("基准频率 (Hz)") },
            placeholder = { Text("例如 44100 或 10000000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ppmStr,
            onValueChange = { ppmStr = it },
            label = { Text("精度 (± PPM)") },
            placeholder = { Text("例如 0.5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("频率最大偏差:", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "± ${String.format("%.4f", results.first)} Hz",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("时间累积误差 (每天):", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "± ${String.format("%.4f", results.second)} 秒",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DsdCalculatorScreen(navController: NavController) {
    // 基础倍率
    val multipliers = listOf("64", "128", "256", "512", "1024")
    var selectedMultiplier by remember { mutableStateOf(multipliers[0]) }

    // 基准频率 (44.1k vs 48k)
    var isBase48k by remember { mutableStateOf(false) }

    val results by remember(selectedMultiplier, isBase48k) {
        derivedStateOf {
            val base = if (isBase48k) 48000.0 else 44100.0
            val mult = selectedMultiplier.toIntOrNull() ?: 64

            val dsdRateHz = base * 64 * (mult / 64.0) // DSD64 = 64 * 44.1k
            val mhz = dsdRateHz / 1_000_000.0
            val relativeToCd = (dsdRateHz / 44100.0) / 16.0 * 16.0 // 简化为总数据量对比
            // CD = 44.1k * 16bit = 705.6 kbps (mono)
            // DSD64 = 2.8224 MHz * 1bit = 2822.4 kbps (mono)
            // Ratio = 4
            val ratio = dsdRateHz / (44100.0 * 16.0) // 对比 CD 16bit PCM 的数据流

            Triple(dsdRateHz, mhz, ratio)
        }
    }

    CalculatorScreen(navController, "DSD 频率换算") {
        Text("DSD 规格", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            multipliers.forEach { m ->
                FilterChip(
                    selected = selectedMultiplier == m,
                    onClick = { selectedMultiplier = m },
                    label = { Text("DSD$m") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("基准采样率:")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = isBase48k, onCheckedChange = { isBase48k = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isBase48k) "48 kHz (DVD/TV)" else "44.1 kHz (CD)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("时钟频率", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "${String.format("%.4f", results.second)} MHz",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "= ${String.format("%,.0f", results.first)} Hz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SamplePeriodScreen(navController: NavController) {
    var sampleRateStr by remember { mutableStateOf("44100") }

    val results by remember(sampleRateStr) {
        derivedStateOf {
            val rate = sampleRateStr.toDoubleOrNull() ?: 0.0
            if (rate > 0) {
                val periodSec = 1.0 / rate
                val us = periodSec * 1_000_000.0 // 微秒
                val ns = periodSec * 1_000_000_000.0 // 纳秒
                // 光在这么短时间内走的距离 (光速 ~30cm/ns)
                val lightDistM = periodSec * 299_792_458.0
                Triple(us, ns, lightDistM)
            } else {
                Triple(0.0, 0.0, 0.0)
            }
        }
    }

    CalculatorScreen(navController, "采样周期 (时间域)") {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "此工具将频率转换为时间间隔。在高采样率下，留给DAC处理和时钟对齐的时间窗口极短。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        OutlinedTextField(
            value = sampleRateStr,
            onValueChange = { sampleRateStr = it },
            label = { Text("采样率 (Hz)") },
            placeholder = { Text("例如 44100, 96000, 192000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // 常用预设快捷按钮
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("44100", "48000", "96000", "192000").forEach { rate ->
                TextButton(onClick = { sampleRateStr = rate }) {
                    Text("${rate.toInt() / 1000}k")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("微秒 (μs):")
                    Text(
                        String.format("%.4f", results.first),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("纳秒 (ns):")
                    Text(
                        String.format("%.2f", results.second),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider()
                Column {
                    Text("在此周期内，光/电仅能传播:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${String.format("%.2f", results.third)} 米",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}