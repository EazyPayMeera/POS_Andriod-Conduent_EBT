package com.eazypaytech.pos.core.ui.components.textview


import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.eazypaytech.pos.core.themes.fonts.Roboto
import com.eazypaytech.pos.core.themes.dimens

/**
 * Reusable TextView wrapper and GenericCard container components.
 *
 * TextView:
 * A flexible composable wrapper around Material3 Text that supports
 * consistent styling, optional click handling, font customization,
 * and max line control across the application.
 *
 * Features:
 * - Centralized text styling for consistent UI
 * - Optional click support (acts like TextButton when needed)
 * - Supports custom font, size, weight, alignment, and style
 * - Prevents repetitive Text configuration in screens
 */

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

/** GenericCard:
 * A reusable Material Card wrapper for consistent container UI.
 *
 * Features:
 * - Configurable shape, elevation, and background color
 * - Used for grouping UI content in POS screens
 * - Ensures consistent card styling across the app
 *
 * Usage:
 * These components are used across the POS application to maintain
 * consistent typography and card-based layouts while reducing UI duplication.
 */
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



