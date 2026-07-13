package com.example.glucoserecordbook.ui


import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glucoserecordbook.data.Reading
import com.example.glucoserecordbook.data.ReadingStatus
import com.example.glucoserecordbook.data.ReadingType
import com.example.glucoserecordbook.data.nextUnresolved
import com.example.glucoserecordbook.export.GlucosePdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val fullDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())
private val compactDateFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu").withLocale(Locale.getDefault())

@Composable
fun RecordApp(viewModel: RecordViewModel) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.messages.collectLatest { snackbar.showSnackbar(it) } }
    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val current = screen) {
            Screen.Home -> HomeScreen(viewModel, Modifier.padding(padding))
            Screen.History -> HistoryScreen(viewModel, Modifier.padding(padding))
            is Screen.Entry -> EntryScreen(current.state, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeScreen(viewModel: RecordViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.home.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val today = LocalDate.now()
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            Text("Glucose Records Tracker", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (state.date == today) "TODAY" else "SELECTED DAY", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                Text(state.date.format(fullDateFormatter), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            }
            val nextUnfilled = nextUnresolved(state.readings)
            ReadingType.entries.forEach { type ->
                ReadingCard(
                    type = type,
                    reading = state.readings[type],
                    isNextToEnter = type == nextUnfilled,
                    onClick = { viewModel.beginEntry(type) }
                )
            }
            OutlinedButton(onClick = viewModel::showHistory, modifier = Modifier.fillMaxWidth().height(72.dp)) {
                Text("VIEW RECORDS")
            }
            DecorativeLandscape(
    modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
)
            DateNavigationBar(
                date = state.date,
                today = today,
                onPrevious = viewModel::previousDate,
                onNext = viewModel::nextDate,
                onToday = viewModel::today,
                onChooseDate = {
                    DatePickerDialog(context, { _, year, month, day -> viewModel.showDate(LocalDate.of(year, month + 1, day)) },
                        state.date.year, state.date.monthValue - 1, state.date.dayOfMonth).apply {
                        datePicker.maxDate = System.currentTimeMillis()
                    }.show()
                }
            )
            Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ReadingCard(type: ReadingType, reading: Reading?, isNextToEnter: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 148.dp)
            .semantics { role = Role.Button }
            .clickable(onClick = onClick),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isNextToEnter) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            else androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextToEnter) 4.dp else 1.dp),
        border = if (isNextToEnter) androidx.compose.foundation.BorderStroke(2.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            if (isNextToEnter) {
                Text("NEXT TO ENTER", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            }
            Text(type.label, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            if (reading == null) {
                Text("+ ADD READING", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            } else if (reading.status == ReadingStatus.SKIPPED) {
                Text("\u2014", style = androidx.compose.material3.MaterialTheme.typography.displayLarge, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                Text("Tap to change", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(reading.value!!.toString(), style = androidx.compose.material3.MaterialTheme.typography.displayLarge, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(10.dp))
                    Text("mg/dL", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                }
                Text("Tap to change", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun DateNavigationBar(
    date: LocalDate,
    today: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onChooseDate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactDateButton(
                    label = "â€¹ Previous",
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                )

                CompactDateButton(
                    label = "Today",
                    onClick = onToday,
                    modifier = Modifier.weight(1f),
                    emphasized = date == today
                )

                if (date.isBefore(today)) {
                    CompactDateButton(
                        label = "Next â€º",
                        onClick = onNext,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedButton(
                onClick = onChooseDate,
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 56.dp)
            ) {
                Text("Choose date")
            }
        }
    }
}

@Composable
private fun CompactDateButton(label: String, onClick: () -> Unit, modifier: Modifier, emphasized: Boolean = false) {
    if (emphasized) {
        Button(onClick = onClick, modifier = modifier.sizeIn(minHeight = 62.dp)) { Text(label, textAlign = TextAlign.Center) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier.sizeIn(minHeight = 62.dp)) { Text(label, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun DecorativeLandscape(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
    brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFFFAF8F2),
            0.18f to Color(0xFFF7F8F1),
            0.40f to Color(0xFFEDF8F4),
            0.62f to Color(0xFFE4F5F2),
            1.00f to Color(0xFFEAF3E5)
        ),
        startY = 0f,
        endY = size.height
    )
)

        val farLeft = Path().apply {
            moveTo(0f, h * 0.58f)
            cubicTo(
                w * 0.10f, h * 0.35f,
                w * 0.24f, h * 0.34f,
                w * 0.38f, h * 0.56f
            )
            cubicTo(
                w * 0.48f, h * 0.70f,
                w * 0.55f, h * 0.70f,
                w * 0.62f, h
            )
            lineTo(0f, h)
            close()
        }

        val farRight = Path().apply {
            moveTo(w * 0.34f, h)
            cubicTo(
                w * 0.43f, h * 0.62f,
                w * 0.52f, h * 0.40f,
                w * 0.67f, h * 0.36f
            )
            cubicTo(
                w * 0.83f, h * 0.32f,
                w * 0.94f, h * 0.43f,
                w, h * 0.56f
            )
            lineTo(w, h)
            close()
        }

        drawPath(farLeft, Color(0xFFDDEEDC))
        drawPath(farRight, Color(0xFFD5E9D5))

        val middleLeft = Path().apply {
            moveTo(0f, h * 0.72f)
            cubicTo(
                w * 0.12f, h * 0.53f,
                w * 0.27f, h * 0.51f,
                w * 0.42f, h * 0.67f
            )
            cubicTo(
                w * 0.51f, h * 0.77f,
                w * 0.57f, h * 0.86f,
                w * 0.62f, h
            )
            lineTo(0f, h)
            close()
        }

        val middleRight = Path().apply {
            moveTo(w * 0.40f, h)
            cubicTo(
                w * 0.50f, h * 0.73f,
                w * 0.64f, h * 0.54f,
                w * 0.78f, h * 0.55f
            )
            cubicTo(
                w * 0.90f, h * 0.56f,
                w * 0.97f, h * 0.66f,
                w, h * 0.72f
            )
            lineTo(w, h)
            close()
        }

        drawPath(middleLeft, Color(0xFFAED5B8))
        drawPath(middleRight, Color(0xFFA4CFB0))

        val foreground = Path().apply {
            moveTo(0f, h * 0.84f)
            cubicTo(
                w * 0.14f, h * 0.70f,
                w * 0.29f, h * 0.72f,
                w * 0.43f, h * 0.81f
            )
            cubicTo(
                w * 0.55f, h * 0.89f,
                w * 0.65f, h * 0.78f,
                w * 0.76f, h * 0.73f
            )
            cubicTo(
                w * 0.87f, h * 0.68f,
                w * 0.95f, h * 0.76f,
                w, h * 0.80f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(foreground, Color(0xFF78B68F))

        val windingPath = Path().apply {
            moveTo(w * 0.40f, h)

            cubicTo(
                w * 0.48f, h * 0.89f,
                w * 0.56f, h * 0.84f,
                w * 0.58f, h * 0.75f
            )
            cubicTo(
                w * 0.60f, h * 0.67f,
                w * 0.53f, h * 0.61f,
                w * 0.51f, h * 0.55f
            )


            cubicTo(
                w * 0.50f, h * 0.51f,
                w * 0.51f, h * 0.48f,
                w * 0.53f, h * 0.46f
            )

            cubicTo(
                w * 0.57f, h * 0.53f,
                w * 0.66f, h * 0.60f,
                w * 0.65f, h * 0.70f
            )
            cubicTo(
                w * 0.64f, h * 0.82f,
                w * 0.55f, h * 0.91f,
                w * 0.50f, h
            )

            close()
        }

        drawPath(windingPath, Color(0xFFF9F6EA))

        fun drawTree(x: Float, groundY: Float, scale: Float) {
            drawRoundRect(
                color = Color(0xFF6F8E70),
                topLeft = Offset(
                    x - 1.5.dp.toPx() * scale,
                    groundY - 15.dp.toPx() * scale
                ),
                size = Size(
                    3.dp.toPx() * scale,
                    15.dp.toPx() * scale
                ),
                cornerRadius = CornerRadius(
                    1.5.dp.toPx() * scale,
                    1.5.dp.toPx() * scale
                )
            )

            // Canopy
            drawCircle(
                color = Color(0xFF7FAF83),
                radius = 8.dp.toPx() * scale,
                center = Offset(
                    x,
                    groundY - 18.dp.toPx() * scale
                )
            )
        }

        drawTree(
            x = w * 0.16f,
            groundY = h * 0.68f,
            scale = 0.65f
        )

        drawTree(
            x = w * 0.84f,
            groundY = h * 0.62f,
            scale = 0.55f
        )

        drawTree(
            x = w * 0.91f,
            groundY = h * 0.68f,
            scale = 0.75f
        )
    }
}

@Composable
private fun EntryScreen(state: EntryState, viewModel: RecordViewModel, modifier: Modifier = Modifier) {
    var showDeleteConfirmation by remember(state.reading?.id) { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(if (state.reading == null) "Add Reading" else "Change Reading", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text(state.type.label, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Text(state.date.format(fullDateFormatter), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = when (state.input) {
                        EntryInput.EMPTY -> "Enter reading"
                        EntryInput.NUMBER -> state.number
                        EntryInput.SKIPPED -> "\u2014"
                    },
                    style = if (state.input == EntryInput.EMPTY) androidx.compose.material3.MaterialTheme.typography.titleLarge
                    else androidx.compose.material3.MaterialTheme.typography.displayLarge,
                    color = if (state.input == EntryInput.EMPTY) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    else androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        state.error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge) }
        NumericKeypad(
            onDigit = viewModel::appendDigit,
            onBackspace = viewModel::backspace,
            onDash = viewModel::markSkipped,
            enabled = !state.saving
        )
        Button(onClick = viewModel::saveEntry, enabled = !state.saving, modifier = Modifier.fillMaxWidth().height(80.dp)) {
            Text(if (state.reading == null) "SAVE" else "SAVE CHANGES")
        }
        OutlinedButton(onClick = viewModel::cancelEntry, enabled = !state.saving, modifier = Modifier.fillMaxWidth().height(68.dp)) { Text("CANCEL") }
        if (state.reading != null) {
            OutlinedButton(onClick = { showDeleteConfirmation = true }, enabled = !state.saving, modifier = Modifier.fillMaxWidth().height(68.dp)) { Text("REMOVE READING") }
        }
    }
    if (showDeleteConfirmation && state.reading != null) {
        DeleteConfirmation(state.reading, onKeep = { showDeleteConfirmation = false }, onRemove = viewModel::deleteEntry)
    }
}

@Composable
private fun NumericKeypad(onDigit: (Int) -> Unit, onBackspace: () -> Unit, onDash: () -> Unit, enabled: Boolean) {
    val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row -> KeyRow(row.map { it.toString() }, onDigit, enabled) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onBackspace,
                enabled = enabled,
                modifier = Modifier.weight(1f).height(72.dp).semantics { contentDescription = "Delete last digit" }
            ) { Text("âŒ«", fontSize = 34.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
            Button(onClick = { onDigit(0) }, enabled = enabled, modifier = Modifier.weight(1f).height(72.dp)) { Text("0") }
            OutlinedButton(
                onClick = onDash,
                enabled = enabled,
                modifier = Modifier.weight(1f).height(72.dp).semantics { contentDescription = "Mark reading as not taken" }
            ) { Text("\u2014", fontSize = 34.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun KeyRow(keys: List<String>, onDigit: (Int) -> Unit, enabled: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        keys.forEach { key -> Button(onClick = { onDigit(key.toInt()) }, enabled = enabled, modifier = Modifier.weight(1f).height(72.dp)) { Text(key) } }
    }
}

@Composable
private fun DeleteConfirmation(reading: Reading, onKeep: () -> Unit, onRemove: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeep,
        title = { Text("Remove this reading?", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (reading.status == ReadingStatus.SKIPPED) "\u2014" else "${reading.value} mg/dL",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                Text(reading.type.label, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                Text(reading.date.format(fullDateFormatter), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = { Button(onClick = onRemove, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("REMOVE READING") } },
        dismissButton = { OutlinedButton(onClick = onKeep, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("KEEP READING") } }
    )
}

@Composable
private fun HistoryScreen(viewModel: RecordViewModel, modifier: Modifier = Modifier) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExport by remember { mutableStateOf<Map<LocalDate, List<Reading>>?>(null) }
    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val export = pendingExport
        pendingExport = null
        if (uri == null || export == null) return@rememberLauncherForActivityResult
        scope.launch {
            val message = try {
                withContext(Dispatchers.IO) { GlucosePdfExporter.write(context.contentResolver, uri, export) }
                "PDF saved"
            } catch (_: Exception) {
                "Could not save the PDF"
            }
            viewModel.showMessage(message)
        }
    }
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Records", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text("Newest dates first", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        if (history.isEmpty()) {
            Text("No readings recorded yet.", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                items(history.entries.toList(), key = { it.key.toEpochDay() }) { (date, readings) -> HistoryDateCard(date, readings) }
            }
        }
        OutlinedButton(
            onClick = {
                pendingExport = history.mapValues { (_, readings) -> readings.toList() }
                savePdf.launch("glucose-records-${LocalDate.now()}.pdf")
            },
            enabled = history.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(68.dp)
        ) { Text("EXPORT PDF") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = viewModel::showHome, modifier = Modifier.fillMaxWidth().height(76.dp)) { Text("BACK TO LOG") }
    }
}

@Composable
private fun HistoryDateCard(date: LocalDate, readings: List<Reading>) {
    val cells = readings.associateBy { it.type }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(date.format(compactDateFormatter).uppercase(), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            ReadingType.entries.forEachIndexed { index, type ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(type.shortLabel, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                    Text(
                        cells[type]?.let { if (it.status == ReadingStatus.SKIPPED) "\u2014" else it.value.toString() } ?: "\u2014",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                }
                if (index < ReadingType.entries.lastIndex) Divider()
            }
        }
    }
}

