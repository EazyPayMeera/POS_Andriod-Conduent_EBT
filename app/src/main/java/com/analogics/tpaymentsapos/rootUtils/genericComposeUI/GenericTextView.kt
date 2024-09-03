package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
    shape: RoundedCornerShape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium),
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