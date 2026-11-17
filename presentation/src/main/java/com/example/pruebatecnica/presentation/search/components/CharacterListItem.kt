package com.example.pruebatecnica.presentation.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebatecnica.presentation.common.model.CharacterUi
import com.example.pruebatecnica.presentation.common.components.CharacterImage
import com.example.pruebatecnica.presentation.common.components.StatusLabel
import com.example.pruebatecnica.presentation.common.preview.PreviewData
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

@Composable
fun CharacterListItem(
    character: CharacterUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            CharacterImage(
                imageUrl = character.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
            )
        },
        headlineContent = {
            Text(character.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = { StatusLabel(character.status, character.species) },
    )
}

@Preview(showBackground = true)
@Composable
private fun CharacterListItemPreview() {
    PruebaTecnicaTheme {
        CharacterListItem(character = PreviewData.rick, onClick = {})
    }
}
