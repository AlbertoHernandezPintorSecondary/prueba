package com.example.pruebatecnica.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebatecnica.presentation.common.model.CharacterStatusUi
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/** Colored dot + "Status · Species". */
@Composable
fun StatusLabel(
    status: CharacterStatusUi,
    species: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .background(status.color, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${stringResource(status.labelRes)} · $species",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusLabelPreview() {
    PruebaTecnicaTheme {
        Column(Modifier.padding(16.dp)) {
            CharacterStatusUi.entries.forEach { status ->
                StatusLabel(status = status, species = "Human", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
