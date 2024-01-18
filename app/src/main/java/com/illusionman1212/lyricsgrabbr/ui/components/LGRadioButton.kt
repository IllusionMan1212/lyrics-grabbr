package com.illusionman1212.lyricsgrabbr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LGRadioButton(text: String, onClick: () -> Unit, selected: Boolean) {
    Box (Modifier.clickable { onClick() }.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Text(text = text)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LGRadioButtonPreview() {
    LGRadioButton(text = "Radio Button", onClick = {}, selected = true)
}