package com.illusionman1212.lyricsgrabbr.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LGIconButton(
    tooltip: String,
    onClick: () -> Unit,
    badge: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Layout(
        content = {
            PlainTooltipBox(
                tooltip = {
                    Text(text = tooltip)
                },
            ) {
                IconButton(
                    onClick = onClick,
                    Modifier.tooltipAnchor(),
                ) {
                    content()
                }
            }
            Box(Modifier.layoutId("badge")) {
                badge?.let { it() }
            }
        }
    ) { measurables, constraints ->
        val iconPlaceable = measurables[0].measure(constraints)
        val badgePlaceable = measurables.firstOrNull { it.layoutId == "badge" }?.measure(constraints)

        layout(iconPlaceable.width, iconPlaceable.height) {
            iconPlaceable.placeRelative(0, 0)
            badgePlaceable?.placeRelative(
                x = iconPlaceable.width - badgePlaceable.width - 16,
                y = 16,
            )
        }
    }

//    PlainTooltipBox(tooltip = {
//        Text(text = tooltip)
//    }) {
//        if (badge != null) {
//            IconButton(
//                onClick = onClick,
//                Modifier.tooltipAnchor(),
//            ) {
//                BadgedBox(badge = badge) {
//                    content()
//                }
//            }
//        } else {
//            IconButton(
//                onClick = onClick,
//                Modifier.tooltipAnchor(),
//            ) {
//                content()
//            }
//        }
//    }
}