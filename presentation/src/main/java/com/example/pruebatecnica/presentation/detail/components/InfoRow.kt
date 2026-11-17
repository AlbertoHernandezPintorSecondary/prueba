package com.example.pruebatecnica.presentation.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/** Label + value pair followed by a divider. */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    PruebaTecnicaTheme {
        InfoRow(label = "Origen", value = "Earth (C-137)", modifier = Modifier.padding(horizontal = 16.dp))
    }
}
