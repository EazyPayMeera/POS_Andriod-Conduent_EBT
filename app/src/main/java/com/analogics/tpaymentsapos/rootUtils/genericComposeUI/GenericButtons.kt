package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.analogics.tpaymentsapos.ui.theme.dimens

//Common  Switch button
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedImage: Int,
    uncheckedImage: Int,
    modifier: Modifier = Modifier,
    imageSize: Dp = MaterialTheme.dimens.DP_40_CompactMedium // Default size passed
) {
    Box(
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .padding(MaterialTheme.dimens.DP_2_CompactMedium) // Padding for better touch area
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = if (checked) checkedImage else uncheckedImage),
            contentDescription = null,
            modifier = Modifier
                .size(imageSize) // Apply the dynamic size
                .background(MaterialTheme.colorScheme.onPrimary)
                .clip(RoundedCornerShape(MaterialTheme.dimens.DP_21_CompactMedium)) // Optional rounded corners
        )
    }
}
