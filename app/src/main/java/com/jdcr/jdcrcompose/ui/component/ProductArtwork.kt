package com.jdcr.jdcrcompose.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jdcr.jdcrcompose.data.Product

private data class ArtworkPalette(
    val background: Color,
    val foreground: Color,
    val accent: Color,
)

private val artworkPalettes = listOf(
    ArtworkPalette(Color(0xFFDCEDE7), Color(0xFF146B55), Color(0xFFB9473F)),
    ArtworkPalette(Color(0xFFFFE1DE), Color(0xFF8F332E), Color(0xFF245D7A)),
    ArtworkPalette(Color(0xFFE5E5F4), Color(0xFF3E4778), Color(0xFFD2872C)),
    ArtworkPalette(Color(0xFFF1E6D2), Color(0xFF755421), Color(0xFF247066)),
)

@Composable
fun ProductArtwork(
    product: Product,
    modifier: Modifier = Modifier,
) {
    val palette = artworkPalettes[((product.id - 1) % artworkPalettes.size).toInt()]

    Box(
        modifier = modifier.background(palette.background, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.BottomStart,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            val stroke = size.minDimension * 0.075f
            drawCircle(
                color = palette.foreground,
                radius = size.minDimension * 0.27f,
                center = Offset(size.width * 0.63f, size.height * 0.42f),
                style = Stroke(width = stroke),
            )
            drawLine(
                color = palette.accent,
                start = Offset(size.width * 0.12f, size.height * 0.72f),
                end = Offset(size.width * 0.72f, size.height * 0.12f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawRect(
                color = palette.foreground.copy(alpha = 0.18f),
                topLeft = Offset(size.width * 0.08f, size.height * 0.1f),
                size = Size(size.width * 0.28f, size.height * 0.28f),
            )
        }
        Text(
            text = product.category,
            color = palette.foreground,
            modifier = Modifier.padding(10.dp),
        )
    }
}
