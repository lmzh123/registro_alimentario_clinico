package com.registro.alimentario.ui.professional

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.registro.alimentario.viewmodel.PeriodStats
import com.registro.alimentario.viewmodel.StatsGranularity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientStatsScreen(
    periodStats: List<PeriodStats>,
    statsGranularity: StatsGranularity,
    onGranularityChange: (StatsGranularity) -> Unit
) {
    if (periodStats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos suficientes", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = statsGranularity == StatsGranularity.WEEKLY,
                    onClick = { onGranularityChange(StatsGranularity.WEEKLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Semanal") }
                SegmentedButton(
                    selected = statsGranularity == StatsGranularity.MONTHLY,
                    onClick = { onGranularityChange(StatsGranularity.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Mensual") }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Total de registros", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            SingleSeriesChart(
                values = periodStats.map { it.totalRegistros.toFloat() },
                labels = periodStats.map { it.periodLabel }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Purga: deseos vs actuó", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            PurgaChart(periodStats = periodStats)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Episodios de atracón", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            SingleSeriesChart(
                values = periodStats.map { it.atracones.toFloat() },
                labels = periodStats.map { it.periodLabel }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Restricción previa", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            RestriccionChart(periodStats = periodStats)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SingleSeriesChart(values: List<Float>, labels: List<String>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val cachedLabels = remember(labels) { labels }

    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    cachedLabels.getOrNull(value.toInt()) ?: ""
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Composable
private fun PurgaChart(periodStats: List<PeriodStats>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(periodStats) { periodStats.map { it.periodLabel } }

    LaunchedEffect(periodStats) {
        modelProducer.runTransaction {
            columnSeries {
                series(periodStats.map { it.deseosPurgar.toFloat() })
                series(periodStats.map { it.actuoSobrePurga.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    labels.getOrNull(value.toInt()) ?: ""
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Composable
private fun RestriccionChart(periodStats: List<PeriodStats>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(periodStats) { periodStats.map { it.periodLabel } }

    LaunchedEffect(periodStats) {
        modelProducer.runTransaction {
            columnSeries {
                series(periodStats.map { it.restriccionSalteComida.toFloat() })
                series(periodStats.map { it.restriccionComiMenos.toFloat() })
                series(periodStats.map { it.restriccionRetrase.toFloat() })
                series(periodStats.map { it.restriccionNoHubo.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                mergeMode = { ColumnCartesianLayer.MergeMode.Stacked }
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    labels.getOrNull(value.toInt()) ?: ""
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}
