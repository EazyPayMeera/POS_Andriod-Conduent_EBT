package com.eazypaytech.pos.core.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.eazypaytech.pos.core.themes.dimens

/**
 * Custom image-based switch component.
 *
 * This switch behaves like a toggle button but uses images instead of
 * Material Switch UI.
 *
 * Features:
 * - Custom checked / unchecked images
 * - Optional tint support for both states
 * - Click toggles state (no ripple by default)
 * - Configurable size and styling
 *
 * Usage:
 * Useful for POS UI where native switches are not preferred and
 * branded icons are required.
 *
 * @param checked Current switch state
 * @param onCheckedChange Callback triggered when user toggles switch
 * @param checkedImage Drawable resource for ON state
 * @param uncheckedImage Drawable resource for OFF state
 * @param checkedTintColor Tint applied when checked (default = primary color)
 * @param uncheckedTintColor Tint applied when unchecked (default = onSecondary)
 * @param modifier Compose modifier for external styling
 * @param imageSize Size of switch icon
 */
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedImage: Int,
    uncheckedImage: Int,
    checkedTintColor: Color = MaterialTheme.colorScheme.primary,  // Add dynamic color for checked state
    uncheckedTintColor: Color = MaterialTheme.colorScheme.onSecondary,  // Add dynamic color for unchecked state
    modifier: Modifier = Modifier,
    imageSize: Dp = MaterialTheme.dimens.DP_40_CompactMedium // Default size passed
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Disable ripple effect
            ) { onCheckedChange(!checked) }
            .padding(MaterialTheme.dimens.DP_2_CompactMedium) // Padding for better touch area
    ) {
        Image(
            painter = painterResource(id = if (checked) checkedImage else uncheckedImage),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (checked) checkedTintColor else uncheckedTintColor), // Apply dynamic tint
            modifier = Modifier
                .size(imageSize) // Apply the dynamic size
                .background(MaterialTheme.colorScheme.onPrimary)
                .clip(RoundedCornerShape(MaterialTheme.dimens.DP_21_CompactMedium))
        )
    }
}




