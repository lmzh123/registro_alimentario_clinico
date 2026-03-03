package com.registro.alimentario.ui.shared

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R

data class CrisisResource(
    val name: String,
    val description: String,
    val phone: String? = null,
    val url: String? = null
)

// Hardcoded defaults — to be replaced with Firestore config once populated with clinical team
private val defaultResources = listOf(
    CrisisResource(
        name = "Centro de Asistencia al Suicida",
        description = "Línea de crisis disponible las 24 horas",
        phone = "135"
    ),
    CrisisResource(
        name = "SAME - Sistema de Atención Médica de Emergencias",
        description = "Emergencias médicas en Buenos Aires",
        phone = "107"
    ),
    CrisisResource(
        name = "Línea de Salud Mental",
        description = "Atención en salud mental del Ministerio de Salud",
        phone = "0800-999-0091"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisResourcesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crisis_resources_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_cd)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.crisis_resources_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(defaultResources) { resource ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = resource.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resource.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        resource.phone?.let { phone ->
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Phone,
                                    contentDescription = stringResource(R.string.crisis_call_cd, resource.name),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(phone)
                            }
                        }
                    }
                }
            }
        }
    }
}
