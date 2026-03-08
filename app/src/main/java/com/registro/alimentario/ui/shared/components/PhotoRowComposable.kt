package com.registro.alimentario.ui.shared.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.registro.alimentario.R

@Composable
fun PhotoRow(
    localUris: List<Uri>,
    uploadedUrls: List<String>,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemoveLocal: (Uri) -> Unit,
    onRemoveUploaded: (String) -> Unit,
    onPhotoTapped: (String) -> Unit,
    editable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val totalCount = localUris.size + uploadedUrls.size
    val atLimit = totalCount >= 5

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.photos_section_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (totalCount > 0) {
            LazyRow {
                items(uploadedUrls) { url ->
                    PhotoThumbnail(
                        model = url,
                        onTap = { onPhotoTapped(url) },
                        onRemove = if (editable) ({ onRemoveUploaded(url) }) else null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                items(localUris) { uri ->
                    PhotoThumbnail(
                        model = uri,
                        onTap = { onPhotoTapped(uri.toString()) },
                        onRemove = if (editable) ({ onRemoveLocal(uri) }) else null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (editable) {
            if (atLimit) {
                Text(
                    text = stringResource(R.string.photos_limit_reached),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row {
                    OutlinedButton(onClick = onTakePhoto, enabled = !atLimit) {
                        Text(stringResource(R.string.take_photo_button))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onChooseGallery, enabled = !atLimit) {
                        Text(stringResource(R.string.choose_gallery_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    model: Any,
    onTap: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(80.dp)) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = stringResource(R.string.photo_thumbnail_cd),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                )
            },
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onTap)
        )
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_photo_cd),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
