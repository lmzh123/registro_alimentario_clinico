package com.registro.alimentario.ui.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.registro.alimentario.R
import com.registro.alimentario.model.Connection
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.viewmodel.SearchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTherapistsScreen(
    connections: List<Connection>,
    searchQuery: String,
    searchState: SearchState,
    searchResult: User?,
    requestSent: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSendRequest: (therapistId: String, therapistName: String, therapistRole: String) -> Unit,
    onRevokeConnection: (connectionId: String) -> Unit,
    onResetRequestSent: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val requestSentMessage = stringResource(R.string.request_sent_confirmation)

    LaunchedEffect(requestSent) {
        if (requestSent) {
            snackbarHostState.showSnackbar(requestSentMessage)
            onResetRequestSent()
        }
    }

    var connectionToRevoke by remember { mutableStateOf<Connection?>(null) }

    // Confirmation dialog for revoke / cancel
    connectionToRevoke?.let { conn ->
        val isPending = conn.status == Connection.STATUS_PENDING
        AlertDialog(
            onDismissRequest = { connectionToRevoke = null },
            title = {
                Text(
                    stringResource(
                        if (isPending) R.string.cancel_request_title else R.string.revoke_connection_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        if (isPending) R.string.cancel_request_message else R.string.revoke_connection_message,
                        conn.therapistName.ifBlank { conn.therapistRole }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRevokeConnection(conn.id)
                    connectionToRevoke = null
                }) {
                    Text(
                        stringResource(
                            if (isPending) R.string.cancel_request_button else R.string.revoke_button
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { connectionToRevoke = null }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_therapists_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_cd)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── Active and pending connections ────────────────────────────
            if (connections.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.connections_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                val active = connections.filter { it.status == Connection.STATUS_ACTIVE }
                val pending = connections.filter { it.status == Connection.STATUS_PENDING }

                if (active.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.connections_section_active),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(active, key = { it.id }) { conn ->
                        ConnectionItem(
                            connection = conn,
                            onRevoke = { connectionToRevoke = conn }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (pending.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.connections_section_pending),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pending, key = { it.id }) { conn ->
                        ConnectionItem(
                            connection = conn,
                            onRevoke = { connectionToRevoke = conn }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ── Therapist search ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.search_therapist_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text(stringResource(R.string.search_therapist_email_label)) },
                        placeholder = { Text(stringResource(R.string.search_therapist_email_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSearch,
                        enabled = searchQuery.isNotBlank() && searchState != SearchState.LOADING
                    ) {
                        Text(stringResource(R.string.search_therapist_button))
                    }
                }
            }

            // ── Search result ─────────────────────────────────────────────
            when (searchState) {
                SearchState.LOADING -> item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Buscando…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SearchState.NOT_FOUND -> item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.therapist_not_found),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SearchState.ERROR -> item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se pudo realizar la búsqueda. Verificá tu conexión e intentá de nuevo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                SearchState.FOUND -> {
                    val therapist = searchResult
                    if (therapist != null) {
                        val alreadyConnected = connections.any { it.therapistId == therapist.uid }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = therapist.displayName.ifBlank { therapist.email },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = therapist.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = UserRole.fromId(therapist.role.id).displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (alreadyConnected) {
                                        Text(
                                            text = stringResource(R.string.already_connected),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                onSendRequest(
                                                    therapist.uid,
                                                    therapist.displayName.ifBlank { therapist.email },
                                                    therapist.role.id
                                                )
                                            }
                                        ) {
                                            Text(stringResource(R.string.send_connection_request))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ConnectionItem(
    connection: Connection,
    onRevoke: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.therapistName.ifBlank { connection.therapistRole },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = UserRole.fromId(connection.therapistRole).displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        if (connection.status == Connection.STATUS_ACTIVE)
                            R.string.connection_status_active
                        else
                            R.string.connection_status_pending
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (connection.status == Connection.STATUS_ACTIVE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onRevoke) {
                Text(
                    text = stringResource(
                        if (connection.status == Connection.STATUS_PENDING)
                            R.string.cancel_request_button
                        else
                            R.string.revoke_button
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
