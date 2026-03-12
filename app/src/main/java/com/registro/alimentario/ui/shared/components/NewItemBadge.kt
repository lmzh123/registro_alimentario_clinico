package com.registro.alimentario.ui.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewItemBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color = MaterialTheme.colorScheme.error, shape = CircleShape)
    )
}
