package com.registro.alimentario.ui.professional

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.registro.alimentario.viewmodel.PeriodStats
import com.registro.alimentario.viewmodel.StatsGranularity
import kotlin.math.max

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

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = statsGranularity == StatsGranularity.LAST_7_DAYS,
                    onClick = { onGranularityChange(StatsGranularity.LAST_7_DAYS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                ) { Text("7 días") }
                SegmentedButton(
                    selected = statsGranularity == StatsGranularity.LAST_30_DAYS,
                    onClick = { onGranularityChange(StatsGranularity.LAST_30_DAYS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                ) { Text("30 días") }
                SegmentedButton(
                    selected = statsGranularity == StatsGranularity.WEEKLY,
                    onClick = { onGranularityChange(StatsGranularity.WEEKLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                ) { Text("Semanal") }
                SegmentedButton(
                    selected = statsGranularity == StatsGranularity.MONTHLY,
                    onClick = { onGranularityChange(StatsGranularity.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                ) { Text("Mensual") }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("Total de registros", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            SingleSeriesBarChart(
                values = periodStats.map { it.totalRegistros },
                labels = periodStats.map { it.periodLabel },
                barColor = Color(0xFF5C8FB5)
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Purga: deseos vs actuó", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            TwoSeriesBarChart(
                series1 = periodStats.map { it.deseosPurgar },
                series2 = periodStats.map { it.actuoSobrePurga },
                labels = periodStats.map { it.periodLabel },
                color1 = Color(0xFFE07B5A),
                color2 = Color(0xFFB54040)
            )
            ChartLegend(
                items = listOf(
                    Color(0xFFE07B5A) to "Deseos de purgar",
                    Color(0xFFB54040) to "Actuó sobre purga"
                )
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Episodios de atracón", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            SingleSeriesBarChart(
                values = periodStats.map { it.atracones },
                labels = periodStats.map { it.periodLabel },
                barColor = Color(0xFFD4A55A)
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Restricción previa", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            StackedBarChart(
                series = listOf(
                    periodStats.map { it.restriccionSalteComida },
                    periodStats.map { it.restriccionComiMenos },
                    periodStats.map { it.restriccionRetrase },
                    periodStats.map { it.restriccionNoHubo }
                ),
                labels = periodStats.map { it.periodLabel },
                colors = listOf(
                    Color(0xFF6A8D73),
                    Color(0xFF9BB57A),
                    Color(0xFFC8D98E),
                    Color(0xFFDDDDDD)
                )
            )
            ChartLegend(
                items = listOf(
                    Color(0xFF6A8D73) to "Saltó comida",
                    Color(0xFF9BB57A) to "Comió menos",
                    Color(0xFFC8D98E) to "Retrasó comida",
                    Color(0xFFDDDDDD) to "Sin restricción"
                )
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Single-series bar chart ─────────────────────────────────────────────────

@Composable
private fun SingleSeriesBarChart(
    values: List<Int>,
    labels: List<String>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontSize = 9.sp, color = Color.Gray) }
    val valueStyle = remember { TextStyle(fontSize = 8.sp, color = Color.DarkGray) }
    val maxVal = values.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val n = values.size

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        val labelH = 20.dp.toPx()
        val topPad = 16.dp.toPx()
        val chartH = size.height - labelH - topPad
        val slotW = size.width / n
        val barW = slotW * 0.55f
        val offsetX = slotW * 0.225f

        values.forEachIndexed { i, v ->
            val barH = (v / maxVal) * chartH
            val x = i * slotW + offsetX
            drawRect(
                color = barColor,
                topLeft = Offset(x, topPad + chartH - barH),
                size = Size(barW, barH)
            )
            // Value above bar
            if (v > 0) {
                val vm = textMeasurer.measure(v.toString(), valueStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = v.toString(),
                    topLeft = Offset(
                        x = (i * slotW + slotW / 2) - vm.size.width / 2f,
                        y = topPad + chartH - barH - vm.size.height - 2.dp.toPx()
                    ),
                    style = valueStyle
                )
            }
            // X-axis label
            val lm = textMeasurer.measure(labels.getOrElse(i) { "" }, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labels.getOrElse(i) { "" },
                topLeft = Offset(
                    x = (i * slotW + slotW / 2) - lm.size.width / 2f,
                    y = topPad + chartH + 4.dp.toPx()
                ),
                style = labelStyle,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

// ─── Two-series grouped bar chart ────────────────────────────────────────────

@Composable
private fun TwoSeriesBarChart(
    series1: List<Int>,
    series2: List<Int>,
    labels: List<String>,
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontSize = 9.sp, color = Color.Gray) }
    val valueStyle = remember { TextStyle(fontSize = 8.sp, color = Color.DarkGray) }
    val maxVal = max(
        series1.maxOrNull()?.toFloat() ?: 0f,
        series2.maxOrNull()?.toFloat() ?: 0f
    ).coerceAtLeast(1f)
    val n = series1.size

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        val labelH = 20.dp.toPx()
        val topPad = 16.dp.toPx()
        val chartH = size.height - labelH - topPad
        val slotW = size.width / n
        val barW = slotW * 0.3f
        val gap = slotW * 0.05f
        val groupW = barW * 2 + gap
        val groupOffX = (slotW - groupW) / 2

        series1.forEachIndexed { i, v1 ->
            val v2 = series2.getOrElse(i) { 0 }
            val x1 = i * slotW + groupOffX
            val x2 = x1 + barW + gap

            val h1 = (v1 / maxVal) * chartH
            val h2 = (v2 / maxVal) * chartH

            drawRect(color1, Offset(x1, topPad + chartH - h1), Size(barW, h1))
            drawRect(color2, Offset(x2, topPad + chartH - h2), Size(barW, h2))

            // Values above bars
            if (v1 > 0) {
                val vm1 = textMeasurer.measure(v1.toString(), valueStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = v1.toString(),
                    topLeft = Offset(
                        x = x1 + barW / 2 - vm1.size.width / 2f,
                        y = topPad + chartH - h1 - vm1.size.height - 2.dp.toPx()
                    ),
                    style = valueStyle
                )
            }
            if (v2 > 0) {
                val vm2 = textMeasurer.measure(v2.toString(), valueStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = v2.toString(),
                    topLeft = Offset(
                        x = x2 + barW / 2 - vm2.size.width / 2f,
                        y = topPad + chartH - h2 - vm2.size.height - 2.dp.toPx()
                    ),
                    style = valueStyle
                )
            }

            // X-axis label
            val lm = textMeasurer.measure(labels.getOrElse(i) { "" }, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labels.getOrElse(i) { "" },
                topLeft = Offset(
                    x = (i * slotW + slotW / 2) - lm.size.width / 2f,
                    y = topPad + chartH + 4.dp.toPx()
                ),
                style = labelStyle,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

// ─── Stacked bar chart ────────────────────────────────────────────────────────

@Composable
private fun StackedBarChart(
    series: List<List<Int>>,
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontSize = 9.sp, color = Color.Gray) }
    val valueStyle = remember { TextStyle(fontSize = 8.sp, color = Color.DarkGray) }
    val n = series.firstOrNull()?.size ?: return
    val totals = (0 until n).map { i -> series.sumOf { it.getOrElse(i) { 0 } } }
    val maxVal = totals.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        val labelH = 20.dp.toPx()
        val topPad = 16.dp.toPx()
        val chartH = size.height - labelH - topPad
        val slotW = size.width / n
        val barW = slotW * 0.55f
        val offsetX = slotW * 0.225f

        (0 until n).forEach { i ->
            val x = i * slotW + offsetX
            val totalH = (totals[i] / maxVal) * chartH
            var yBottom = topPad + chartH

            series.forEachIndexed { si, s ->
                val v = s.getOrElse(i) { 0 }
                if (v > 0) {
                    val segH = (v.toFloat() / totals[i]) * totalH
                    yBottom -= segH
                    drawRect(
                        color = colors.getOrElse(si) { Color.Gray },
                        topLeft = Offset(x, yBottom),
                        size = Size(barW, segH)
                    )
                }
            }

            // Total value above bar
            val total = totals[i]
            if (total > 0) {
                val vm = textMeasurer.measure(total.toString(), valueStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = total.toString(),
                    topLeft = Offset(
                        x = x + barW / 2 - vm.size.width / 2f,
                        y = topPad + chartH - totalH - vm.size.height - 2.dp.toPx()
                    ),
                    style = valueStyle
                )
            }

            // X-axis label
            val lm = textMeasurer.measure(labels.getOrElse(i) { "" }, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labels.getOrElse(i) { "" },
                topLeft = Offset(
                    x = (i * slotW + slotW / 2) - lm.size.width / 2f,
                    y = topPad + chartH + 4.dp.toPx()
                ),
                style = labelStyle,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

// ─── Legend ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartLegend(items: List<Pair<Color, String>>) {
    FlowRow(
        modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawRect(color)
                }
                Spacer(Modifier.width(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
