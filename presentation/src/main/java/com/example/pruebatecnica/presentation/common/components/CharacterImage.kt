package com.example.pruebatecnica.presentation.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.pruebatecnica.presentation.common.preview.PreviewData
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/** Remote character image (Coil) with a neutral placeholder while loading or on error. */
@Composable
fun CharacterImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = placeholder,
        error = placeholder,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CharacterImagePreview() {
    PruebaTecnicaTheme {
        CharacterImage(
            imageUrl = PreviewData.rick.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
        )
    }
}
