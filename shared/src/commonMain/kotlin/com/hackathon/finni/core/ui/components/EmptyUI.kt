package com.hackathon.finni.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.theme.spacing

@Composable
fun EmptyContent(
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = MaterialTheme.spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            iconPainter?.let {
                Icon(
                    modifier = Modifier.size(88.dp).padding(bottom = 8.dp),
                    painter = iconPainter,
                    contentDescription = null,
                    tint = iconTint
                )
            }

            ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
                title()
            }
            description?.let {
                Spacer(Modifier.height(2.dp))
                ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) { description() }
            }
            Spacer(Modifier.height(12.dp))
            action?.let {
                action()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}