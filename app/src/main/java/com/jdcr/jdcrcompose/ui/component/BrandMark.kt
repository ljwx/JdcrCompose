package com.jdcr.jdcrcompose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jdcr.jdcrcompose.R

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    markSize: Dp = 52.dp,
    inverted: Boolean = false,
) {
    val markBackground = if (inverted) Color.White else MaterialTheme.colorScheme.primary
    val markForeground = if (inverted) MaterialTheme.colorScheme.primary else Color.White
    val textColor = if (inverted) Color.White else MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(markSize)
                .background(markBackground, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "J",
                color = markForeground,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = stringResource(R.string.brand_name),
                color = textColor,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                color = textColor.copy(alpha = 0.66f),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
