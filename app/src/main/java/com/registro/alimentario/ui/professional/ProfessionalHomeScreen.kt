package com.registro.alimentario.ui.professional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.Connection
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.ui.shared.components.NewItemBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalHomeScreen(
    role: UserRole,
    patients: List<User>,
    pendingConnections: List<Connection>,
    patientBadges: Map<String, Boolean> = emptyMap(),
    onPatientSelected: (User) -> Unit,
    onAcceptRequest: (connectionId: String) -> Unit,
    onDeclineRequest: (connectionId: String) -> Unit,
    onLogout: () -> Unit
) {
    var connectionToDecline by remember { mutableStateOf<Connection?>(null) }

    // Confirmation dialog before declining a request
    connectionToDecline?.let { conn ->
        AlertDialog(
            onDismissRequest = { connectionToDecline = null },
            title = { Text(stringResource(R.string.decline_request_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.decline_request_message,
                        conn.patientName.ifBlank { conn.patientId }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeclineRequest(conn.id)
                    connectionToDecline = null
                }) {
                    Text(stringResource(R.string.decline_request_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { connectionToDecline = null }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.professional_home_title)) },
                actions = {
                    androidx.compose.material3.TextButton(onClick = onLogout) {
                        Text(stringResource(R.string.logout_label))
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
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── Pending connection requests ────────────────────────────────
            if (pendingConnections.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pending_requests_section),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(pendingConnections, key = { "pending_${it.id}" }) { conn ->
                    PendingRequestItem(
                        connection = conn,
                        onAccept = { onAcceptRequest(conn.id) },
                        onDecline = { connectionToDecline = conn }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // ── Active patients ────────────────────────────────────────────
            if (patients.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_patients_connected),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(patients, key = { it.uid }) { patient ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { onPatientSelected(patient) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = patient.displayName.ifBlank { patient.email },
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                if (patientBadges[patient.uid] == true) {
                                    NewItemBadge(modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRequestItem(
    connection: Connection,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = connection.patientName.ifBlank { connection.patientId },
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onAccept) {
                    Text(stringResource(R.string.accept_request_button))
                }
                TextButton(onClick = onDecline) {
                    Text(
                        text = stringResource(R.string.decline_request_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
