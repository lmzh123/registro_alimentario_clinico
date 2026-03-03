package com.registro.alimentario.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.Emocion
import com.registro.alimentario.model.EmocionEntry

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmotionPicker(
    label: String,
    selected: List<EmocionEntry>,
    onSelectionChanged: (List<EmocionEntry>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(modifier = Modifier.fillMaxWidth()) {
            Emocion.entries.forEach { emocion ->
                val isSelected = selected.any { it.tipo == emocion }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newList = if (isSelected) {
                            selected.filterNot { it.tipo == emocion }
                        } else {
                            selected + EmocionEntry(tipo = emocion)
                        }
                        onSelectionChanged(newList)
                    },
                    label = { Text(emocion.displayName) },
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                )
            }
        }

        // Show free text field for "otro" if selected
        val otroEntry = selected.firstOrNull { it.tipo == Emocion.OTRO }
        if (otroEntry != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = otroEntry.textoLibre,
                onValueChange = { text ->
                    val newList = selected.map {
                        if (it.tipo == Emocion.OTRO) it.copy(textoLibre = text) else it
                    }
                    onSelectionChanged(newList)
                },
                label = { Text(stringResource(R.string.emotions_otro_hint)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 2
            )
        }
    }
}
