package com.registro.alimentario.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.UserRole

@Composable
fun VisibilitySelector(
    selected: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = listOf(
        UserRole.NUTRICIONISTA to stringResource(R.string.visibility_nutricionista),
        UserRole.PSICOLOGIA to stringResource(R.string.visibility_psicologia),
        UserRole.PSIQUIATRIA to stringResource(R.string.visibility_psiquiatria)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.visibility_section_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        roles.forEach { (role, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = role.id in selected,
                    onCheckedChange = { checked ->
                        val newList = if (checked) {
                            selected + role.id
                        } else {
                            selected - role.id
                        }
                        onSelectionChanged(newList)
                    }
                )
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (selected.isEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.visibility_private_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
