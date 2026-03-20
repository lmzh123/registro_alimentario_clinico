package com.registro.alimentario.ui.professional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.Registro
import com.registro.alimentario.ui.shared.components.NewItemBadge
import com.registro.alimentario.viewmodel.PeriodStats
import com.registro.alimentario.viewmodel.RegistroFilter
import com.registro.alimentario.viewmodel.StatsGranularity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegistroListScreen(
    patientName: String,
    registros: List<Registro>,
    currentFilter: RegistroFilter,
    registroBadges: Map<String, Boolean> = emptyMap(),
    periodStats: List<PeriodStats> = emptyList(),
    statsGranularity: StatsGranularity = StatsGranularity.WEEKLY,
    onFilterChanged: (RegistroFilter) -> Unit,
    onGranularityChange: (StatsGranularity) -> Unit = {},
    onRegistroTapped: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patientName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.filter_button))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Registros") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Estadísticas") }
                )
            }

            when (selectedTab) {
                0 -> {
                    if (registros.isEmpty()) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Este paciente no tiene registros compartidos con vos aún.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            items(registros, key = { it.id }) { registro ->
                                val registroDateFormat = SimpleDateFormat("EEE dd/MM, HH:mm", Locale("es"))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clickable { onRegistroTapped(registro.id) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = registro.tipoComida.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (registroBadges[registro.id] == true) {
                                                NewItemBadge(modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                        Text(
                                            text = registroDateFormat.format(registro.fechaHora.toDate()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = registro.descripcion,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    PatientStatsScreen(
                        periodStats = periodStats,
                        statsGranularity = statsGranularity,
                        onGranularityChange = onGranularityChange
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            FilterSheetContent(
                filter = currentFilter,
                onFilterChanged = onFilterChanged,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
private fun FilterSheetContent(
    filter: RegistroFilter,
    onFilterChanged: (RegistroFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var local by remember { mutableStateOf(filter) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.filter_button), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        FilterCheckRow(
            label = stringResource(R.string.filter_atracon),
            checked = local.fueAtracon,
            onCheckedChange = { local = local.copy(fueAtracon = it) }
        )
        FilterCheckRow(
            label = stringResource(R.string.filter_purga),
            checked = local.deseosPurgar,
            onCheckedChange = { local = local.copy(deseosPurgar = it) }
        )
        FilterCheckRow(
            label = stringResource(R.string.filter_actuo_purga),
            checked = local.actuoSobrePurga,
            onCheckedChange = { local = local.copy(actuoSobrePurga = it) }
        )
        FilterCheckRow(
            label = stringResource(R.string.filter_chequeo),
            checked = local.checqueoCuerpo,
            onCheckedChange = { local = local.copy(checqueoCuerpo = it) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            TextButton(onClick = {
                onFilterChanged(RegistroFilter())
                onDismiss()
            }) { Text(stringResource(R.string.clear_filter)) }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = {
                onFilterChanged(local)
                onDismiss()
            }) { Text(stringResource(R.string.apply_filter)) }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FilterCheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
