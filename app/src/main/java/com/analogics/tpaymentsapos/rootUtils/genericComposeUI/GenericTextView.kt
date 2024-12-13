package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.analogics.tpaymentsapos.ui.theme.Roboto
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun TextView(
    text: String,
    fontSize: TextUnit,
    color: Color = Color.Black, // Default color
    fontWeight: FontWeight = FontWeight.Normal, // Default font weight
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier, // Default modifier
    style: TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    fontFamily: FontFamily = Roboto,
    onClick: (() -> Unit)? = null // Optional onClick
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Text(
        text = text,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        maxLines = maxLines,
        modifier = clickableModifier,
        style = style,
        textAlign = textAlign
    )
}


@Composable
fun GenericCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_18_CompactMedium),
    elevation: Dp = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        backgroundColor = backgroundColor,
    ) {
        content()
    }
}


@Composable
fun showToast(message: String) {
    val context = LocalContext.current
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun SignatureBox(
    touchPoints: SnapshotStateList<Offset>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .padding(0.dp)
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val clampedOffset = Offset(
                            x = offset.x.coerceIn(0f, size.width.toFloat()),
                            y = offset.y.coerceIn(0f, size.height.toFloat())
                        )
                        touchPoints.add(clampedOffset)
                    },
                    onDrag = { change, _ ->
                        val clampedOffset = Offset(
                            x = change.position.x.coerceIn(0f, size.width.toFloat()),
                            y = change.position.y.coerceIn(0f, size.height.toFloat())
                        )
                        touchPoints.add(clampedOffset)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var strokeWidth = with(density) { 4.dp.toPx() }
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK // Android color for Paint
                strokeWidth = strokeWidth
                strokeCap = android.graphics.Paint.Cap.ROUND // Set the stroke cap for Android Paint
            }
            for (i in 1 until touchPoints.size) {
                val start = touchPoints[i - 1]
                val end = touchPoints[i]
                drawLine(
                    start = start,
                    end = end,
                    color = Color.Black, // Compose Color
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}