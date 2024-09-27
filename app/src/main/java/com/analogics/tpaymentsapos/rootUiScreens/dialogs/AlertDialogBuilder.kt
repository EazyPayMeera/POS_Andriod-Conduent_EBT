package com.analogics.tpaymentsapos.rootUiScreens.dialogs

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.ui.theme.dimens

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
    fun build() {
        CustomAlertDialog(
            title = title,
            subtitle = subtitle,
            smallText = smallText,
            showCloseButton = showCloseButton,
            onClose = onClose ?: {},
            onDismissRequest = onDismissRequest ?: {}
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
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
            ) {
                GenericCard (modifier = Modifier
                    .wrapContentHeight() // Wraps content height
                    .fillMaxWidth()
                    .align(Alignment.Center),
                    shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_18_CompactMedium),){
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        GenericCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(), // Wraps content height
                            backgroundColor = colorResource(id = R.color.purple_200), // Replace with any color you want
                            shape = RoundedCornerShape(0.dp),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_30_CompactMedium)
                            ) {

                                // Title
                                Text(text = title, style = MaterialTheme.typography.titleLarge)


                            }
                        }
                        // Dialog Content

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = smallText, style = MaterialTheme.typography.bodySmall)
                        // Action Buttons
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.End,

                        ) {
                            Button(onClick = { onDismissRequest() }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick =  {onDismissRequest()} ) {
                                Text("OK")
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

    // Build and show the dialog
    builder.build()
}

@Composable
@Preview
fun PreviewAlertDialogExample() {
    AlertDialogExample()
}






