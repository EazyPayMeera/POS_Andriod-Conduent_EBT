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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.analogics.tpaymentsapos.ui.theme.dimens

//Common  Switch button
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedImage: Int,
    uncheckedImage: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            // Background color for the switch container
            .clickable { onCheckedChange(!checked) }
            .padding(MaterialTheme.dimens.DP_2_CompactMedium) // Padding around the switch
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = if (checked) checkedImage else uncheckedImage),
            contentDescription = null,
            modifier = Modifier
                .size(MaterialTheme.dimens.DP_40_CompactMedium) // Size of the switch thumb
                .background(Color.White) // Background for the thumb
                .clip(RoundedCornerShape(MaterialTheme.dimens.DP_21_CompactMedium)) // Optional: Rounded corners for the thumb
        )
    }
}