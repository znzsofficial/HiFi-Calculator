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
import com.nekolaska.hificalculator.ui.theme.HiFiCalculatorTheme
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

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

// <-- 4. 创建一个全新的 Composable 函数作为"关于"页面 -->
@Composable
fun AboutScreen(navController: NavController) {
    CalculatorScreen(navController = navController, title = "关于") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "HiFi Calculator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "一款为高保真音频爱好者、DIY玩家和专业人士设计的计算工具。旨在提供一系列精确、便捷的计算功能，覆盖从基础电学到数字音频的多个方面。",
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider()
            Text(
                text = "开源与致谢",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "本项目为开源软件，您可以在GitHub上找到源代码。核心UI基于Google的Jetpack Compose构建，使用了Material 3设计语言。",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "GitHub: github.com/NekoLaska/HiFiCalculator (示例链接)",
                style = MaterialTheme.typography.bodyMedium
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


// --- EXISTING CALCULATOR SCREENS ---
@Composable
fun PowerImpedanceVoltageScreen(navController: NavController) {
    var vStr by remember { mutableStateOf("") }
    var rStr by remember { mutableStateOf("") }
    var pStr by remember { mutableStateOf("") }
    var lastEdited by remember { mutableStateOf<String?>(null) }
    var voltageUnit by remember { mutableStateOf(VoltageUnit.Vrms) }
    var powerUnit by remember { mutableStateOf(PowerUnit.W) }

    LaunchedEffect(vStr, rStr, pStr, voltageUnit, powerUnit) {
        val vInput = vStr.toDoubleOrNull()
        val rInput = rStr.toDoubleOrNull()
        val pInput = pStr.toDoubleOrNull()

        if (lastEdited != "P" && vInput != null && rInput != null && rInput > 0) {
            val vrms = if (voltageUnit == VoltageUnit.Vpp) vInput / (2 * sqrt(2.0)) else vInput
            val powerInW = vrms.pow(2) / rInput
            val powerResult = if (powerUnit == PowerUnit.mW) powerInW * 1000 else powerInW
            pStr = String.format("%.4f", powerResult)
        } else if (lastEdited != "V" && pInput != null && rInput != null && rInput > 0) {
            val powerInW = if (powerUnit == PowerUnit.mW) pInput / 1000 else pInput
            val vrms = sqrt(powerInW * rInput)
            val voltageResult = if (voltageUnit == VoltageUnit.Vpp) vrms * 2 * sqrt(2.0) else vrms
            vStr = String.format("%.4f", voltageResult)
        } else if (lastEdited != "R" && vInput != null && pInput != null && pInput > 0) {
            val vrms = if (voltageUnit == VoltageUnit.Vpp) vInput / (2 * sqrt(2.0)) else vInput
            val powerInW = if (powerUnit == PowerUnit.mW) pInput / 1000 else pInput
            if (powerInW > 0) {
                val resistance = vrms.pow(2) / powerInW
                rStr = String.format("%.2f", resistance)
            }
        }
    }

    CalculatorScreen(navController, "功率/电压/阻抗") {
        OutlinedTextField(
            value = vStr,
            onValueChange = { vStr = it; lastEdited = "V" },
            label = { Text("电压") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        UnitSelector(voltageUnit.label, VoltageUnit.entries.toList()) { voltageUnit = it }
        OutlinedTextField(
            value = rStr,
            onValueChange = {
                rStr = it; lastEdited =
                if (lastEdited == "P") "V" else "P"
            },
            label = { Text("阻抗 (Ohms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = pStr,
            onValueChange = { pStr = it; lastEdited = "P" },
            label = { Text("功率") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        UnitSelector(powerUnit.label, PowerUnit.entries.toList()) { powerUnit = it }
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