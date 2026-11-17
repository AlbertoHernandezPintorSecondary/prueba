package com.example.pruebatecnica.presentation.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebatecnica.presentation.common.model.CharacterUi
import com.example.pruebatecnica.presentation.R
import com.example.pruebatecnica.presentation.common.components.CharacterImage
import com.example.pruebatecnica.presentation.common.components.StatusLabel
import com.example.pruebatecnica.presentation.common.preview.PreviewData
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

@Composable
fun CharacterDetailContent(
    character: CharacterUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CharacterImage(
            imageUrl = character.imageUrl,
            contentDescription = character.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Column(Modifier.padding(16.dp)) {
            Text(character.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            StatusLabel(character.status, character.species)
            Spacer(Modifier.height(16.dp))

            InfoRow(stringResource(R.string.detail_species), character.species)
            character.type?.let { type ->
                InfoRow(stringResource(R.string.detail_type), type)
            }
            InfoRow(stringResource(R.string.detail_gender), character.gender)
            InfoRow(stringResource(R.string.detail_origin), character.origin)
            InfoRow(stringResource(R.string.detail_location), character.location)
            InfoRow(stringResource(R.string.detail_episodes), character.episodeCount.toString())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailContentPreview() {
    PruebaTecnicaTheme {
        CharacterDetailContent(character = PreviewData.alienRick)
    }
}
