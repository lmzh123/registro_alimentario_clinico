package com.registro.alimentario.ui.shared

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.Connection
import com.registro.alimentario.model.UserRole

data class CrisisResource(
    val name: String,
    val description: String,
    val phone: String? = null,
    val whatsapp: String? = null,
    val url: String? = null,
    val sectionTitle: String? = null
)

// Hardcoded defaults — to be replaced with Firestore config once populated with clinical team
private val defaultResources = listOf(
    CrisisResource(
        sectionTitle = "Líneas nacionales (funcionan en todo el país)",
        name = "Línea Nacional de Salud Mental",
        description = "192 – opción 4 · 24 horas / todos los días\nOrientación psicológica y apoyo en crisis a nivel nacional.",
        phone = "192"
    ),
    CrisisResource(
        name = "Emergencias",
        description = "24/7 · Puedes pedir apoyo en salud mental y te conectan con equipos de atención o ambulancias si es necesario.",
        phone = "123"
    ),
    CrisisResource(
        sectionTitle = "Líneas importantes por ciudades",
        name = "Línea Amiga Saludable — Medellín",
        description = "24/7 · Atención psicológica y activación de urgencias en salud mental. Medellín y área metropolitana.",
        phone = "6044444448"
    ),
    CrisisResource(
        name = "Línea Alma — Medellín (UdeA)",
        description = "24/7 · Atención en salud mental y orientación para la comunidad universitaria de la Universidad de Antioquia.",
        phone = "018000423874"
    ),
    CrisisResource(
        name = "Línea 106 \"El poder de ser escuchado\" — Bogotá",
        description = "24/7",
        phone = "106",
        whatsapp = "573007548933"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisResourcesScreen(
    onNavigateBack: () -> Unit,
    activeConnections: List<Connection> = emptyList()
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

            if (activeConnections.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.crisis_team_section),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.crisis_team_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(activeConnections) { connection ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = connection.therapistName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = UserRole.fromId(connection.therapistRole).displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item {
                Text(
                    text = stringResource(R.string.crisis_hotlines_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(defaultResources) { resource ->
                resource.sectionTitle?.let { title ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            resource.whatsapp?.let { wa ->
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$wa"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_whatsapp),
                                        contentDescription = stringResource(R.string.crisis_whatsapp_button),
                                        tint = Color.Unspecified
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
