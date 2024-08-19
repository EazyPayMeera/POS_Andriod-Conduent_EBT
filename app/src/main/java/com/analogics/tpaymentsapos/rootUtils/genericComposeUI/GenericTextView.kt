package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.analogics.tpaymentsapos.ui.theme.Roboto

@Composable
fun TextView(
    text: String,
    fontSize: TextUnit,
    color: Color = Color.Black, // Default color
    fontWeight: FontWeight = FontWeight.Normal, // Default font weight
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier, // Default modifie
    style: TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    fontFamily: FontFamily = Roboto
) {
    Text(
        text = text,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        maxLines = maxLines,
        modifier = modifier,
        style = style,
        textAlign = textAlign
    )
}

@Composable
fun GenericCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    elevation: Dp = 4.dp,
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