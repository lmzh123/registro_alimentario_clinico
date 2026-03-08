package com.registro.alimentario.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.registro.alimentario.model.ComentarioClinico
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CommentList(
    comments: List<ComentarioClinico>,
    modifier: Modifier = Modifier,
    currentUserId: String = "",
    editingCommentId: String? = null,
    editingCommentText: String = "",
    onEditStart: (ComentarioClinico) -> Unit = {},
    onEditTextChange: (String) -> Unit = {},
    onEditSubmit: () -> Unit = {},
    onEditCancel: () -> Unit = {},
    onDelete: (ComentarioClinico) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.comments_section_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (comments.isEmpty()) {
            Text(
                text = stringResource(R.string.no_comments),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            comments.forEach { comment ->
                val isOwner = currentUserId.isNotEmpty() && comment.profesionalId == currentUserId
                val isEditing = editingCommentId == comment.id
                CommentItem(
                    comment = comment,
                    isOwner = isOwner,
                    isEditing = isEditing,
                    editingText = if (isEditing) editingCommentText else "",
                    onEditStart = { onEditStart(comment) },
                    onEditTextChange = onEditTextChange,
                    onEditSubmit = onEditSubmit,
                    onEditCancel = onEditCancel,
                    onDelete = { onDelete(comment) }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: ComentarioClinico,
    isOwner: Boolean,
    isEditing: Boolean,
    editingText: String,
    onEditStart: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onEditSubmit: () -> Unit,
    onEditCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_comment_confirm_title)) },
            text = { Text(stringResource(R.string.delete_comment_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SuggestionChip(
                onClick = {},
                label = { Text(comment.rol.displayName, style = MaterialTheme.typography.labelSmall) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateFormat.format(comment.fecha.toDate()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (isOwner && !isEditing) {
                IconButton(onClick = onEditStart) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_comment_cd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_comment_cd),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = editingText,
                onValueChange = onEditTextChange,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            Row {
                IconButton(onClick = onEditCancel) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_comment_edit_cd))
                }
                IconButton(onClick = onEditSubmit, enabled = editingText.isNotBlank()) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_comment_edit_cd))
                }
            }
        } else {
            Text(
                text = comment.texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CommentInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(R.string.comment_hint)) },
            modifier = Modifier.weight(1f),
            maxLines = 4
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.send_comment_cd)
            )
        }
    }
}