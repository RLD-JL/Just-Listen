package com.rld.justlisten.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartMarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    SmartMarqueeText(
        annotatedText = AnnotatedString(text),
        style = style,
        modifier = modifier,
        color = color,
        onClick = onClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartMarqueeText(
    annotatedText: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    var shouldScroll by remember(annotatedText) { mutableStateOf(false) }
    var hasOverflow by remember(annotatedText) { mutableStateOf(false) }

    LaunchedEffect(hasOverflow) {
        if (hasOverflow) {
            delay(2000)
            shouldScroll = true
        }
    }

    Text(
        text = annotatedText,
        style = style,
        color = color,
        maxLines = 1,
        overflow = if (shouldScroll) TextOverflow.Clip else TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (!shouldScroll) {
                hasOverflow = layoutResult.hasVisualOverflow
            }
        },
        modifier = modifier
            .then(
                if (shouldScroll) {
                    Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 2000
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    )
}
