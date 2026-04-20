package com.eazypaytech.pos.features.dialogs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.eazypaytech.pos.R
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.themes.dimens

class AlertDialogBuilder {
    var showCloseButton: Boolean = true
    var title: String = ""
    var subtitle: String = ""
    var smallText: String = ""
    var onClose: (() -> Unit)? = null
    var onDismissRequest: (() -> Unit)? = null

    fun setTitle(title: String) = apply { this.title = title }
    fun setSubtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun setSmallText(smallText: String) = apply { this.smallText = smallText }
    fun setShowCloseButton(show: Boolean) = apply { this.showCloseButton = show }
    fun setOnClose(onClose: () -> Unit) = apply { this.onClose = onClose }
    fun setOnDismissRequest(onDismissRequest: () -> Unit) = apply { this.onDismissRequest = onDismissRequest }
    // Build the dialog
    @Composable
    fun build(
        cancelButtonText: String = stringResource(id = R.string.cancel_btn), // Default text for Cancel button
        okButtonText: String = stringResource(id = R.string.ok) // Default text for OK button
    ) {
            CustomAlertDialog(
                title = title,
                subtitle = subtitle,
                smallText = smallText,
                showCloseButton = showCloseButton,
                onClose = onClose ?: {},
                onDismissRequest = onDismissRequest ?: {},
                cancelButtonText = cancelButtonText,
                okButtonText = okButtonText
            )

    }
}

@Composable
fun CustomAlertDialog(
    title: String,
    subtitle: String,
    smallText: String,
    showCloseButton: Boolean,
    onDismissRequest: () -> Unit,
    onClose: () -> Unit,
    cancelButtonText: String = stringResource(id = R.string.cancel_btn), // Default text for Cancel button
    okButtonText: String = stringResource(id = R.string.ok) // Default text for OK button
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            GenericCard(
                modifier = Modifier
                    .wrapContentHeight() // Wraps content height
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GenericCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(), // Wraps content height
                        backgroundColor = colorResource(id = R.color.purple_200), // Replace with any color you want
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_0_CompactMedium),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
                        ) {
                            // Title
                            Text(text = title, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    // Dialog Content
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium))
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium))
                    Text(text = smallText, style = MaterialTheme.typography.bodySmall)
                    // Action Buttons
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimens.DP_21_CompactMedium),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            modifier = Modifier.weight(1f), // Makes the button fill available space
                            onClick = { onDismissRequest() }
                        ) {
                            Text(
                                text = cancelButtonText,
                                modifier = Modifier.fillMaxWidth(), // Makes text take full width
                                textAlign = TextAlign.Center // Center-align the text
                            ) // Use the passed text for Cancel button
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_20_CompactMedium))
                        Button(
                            modifier = Modifier.weight(1f), // Makes the button fill available space
                            onClick = { onClose() } // Handle OK button action
                        ) {
                            Text(
                                text = okButtonText,
                                modifier = Modifier.fillMaxWidth(), // Makes text take full width
                                textAlign = TextAlign.Center // Center-align the text
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertDialogExample() {
    val builder = AlertDialogBuilder()
        .setTitle("Confirmation")
        .setSubtitle("Are you sure?")
        .setSmallText("This action cannot be undone.")
        .setShowCloseButton(true)
        .setOnClose {
            // Handle close button click
        }
        .setOnDismissRequest {
            // Handle dismiss on outside touch or back press
        }

    // Build and show the dialog with custom button texts
    builder.build(
        cancelButtonText = "No", // Custom text for Cancel button
        okButtonText = "Yes" // Custom text for OK button
    )
}

@Composable
@Preview
fun PreviewAlertDialogExample() {
    AlertDialogExample()
}








