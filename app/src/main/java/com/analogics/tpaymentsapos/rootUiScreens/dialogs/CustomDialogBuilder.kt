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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.ui.theme.dashboardCardBgColor
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay

class CustomDialogBuilder private constructor() {

    private var title: String = ""
    private var subtitle: String = ""
    private var smallText: String = ""
    private var showCloseButton: Boolean = true
    private var isCancelable: Boolean = true
    private var backgroundColor: Color = dashboardCardBgColor
    private var showProgressIndicator: Boolean = true
    private var progressColor: Color = Color(0xFFFF9800)
    private var navAction: (() -> Unit)? = null // New parameter for navigation action
    private var onCancelAction: (() -> Unit)? = null // Action for Cancel button
    private var onConfirmAction: (() -> Unit)? = null // Action for Confirm button
    private var showButtons: Boolean = false // New parameter to control the visibility of both buttons
    private var autooff: Boolean = true // New parameter to control the visibility of both buttons
    private val showDialog = mutableStateOf(true)
    private var cancelButtonText: String = ""
    private var confirmButtonText: String = ""

    fun setTitle(title: String) = apply { this.title = title }
    fun setSubtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun setSmallText(smallText: String) = apply { this.smallText = smallText }
    fun setShowCloseButton(showClose: Boolean) = apply { this.showCloseButton = showClose }
    fun setCancelable(cancelable: Boolean) = apply { this.isCancelable = cancelable }
    fun setBackgroundColor(color: Color) = apply { this.backgroundColor = color }
    fun setProgressColor(color: Color) = apply { this.progressColor = color }
    fun setShowProgressIndicator(show: Boolean) = apply { this.showProgressIndicator = show } // New method to control progress indicator
    fun setNavAction(action: () -> Unit) = apply { this.navAction = action } // Set navigation action
    fun setOnCancelAction(action: () -> Unit) = apply { this.onCancelAction = action }
    fun setOnConfirmAction(action: () -> Unit) = apply { this.onConfirmAction = action }
    fun setShowButtons(show: Boolean) = apply { this.showButtons = show } // Method to control the visibility of both buttons
    fun setAutoOff(show: Boolean) = apply { this.autooff = show } // Method to control the visibility of both buttons
    fun dismiss() = apply { this.showDialog.value = false } // Method to control the visibility of both buttons
    fun setCancelButtonText(text: String) = apply { this.cancelButtonText = text }
    fun setConfirmButtonText(text: String) = apply { this.confirmButtonText = text }

    @Composable
    fun buildDialog(onClose: () -> Unit) {
        if (showDialog.value) {
            Dialog(onDismissRequest = { if (isCancelable) onClose() }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                ) {
                    GenericCard(
                        modifier = Modifier
                            .wrapContentHeight() // Wraps content height
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_18_CompactMedium),
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
                                shape = RoundedCornerShape(0.dp),
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_30_CompactMedium)
                                ) {
                                    // Title
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.h6,
                                        color = Color.Black,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            // Close button
                            if (showCloseButton) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    IconButton(onClick = onClose) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_30_CompactMedium))
                            }

                            // Subtitle
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.h5,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // Message
                            Text(
                                text = smallText,
                                style = MaterialTheme.typography.h6,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Conditionally show Progress Indicator
                            if (showProgressIndicator) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .size(70.dp),
                                    color = progressColor,
                                    strokeWidth = 4.dp,
                                )
                            }

                            if (showButtons) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Cancel Button
                                    Button(
                                        onClick = {
                                            onCancelAction?.invoke()
                                            onClose() // Close the dialog when the cancel button is clicked
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = colorResource(
                                                R.color.grey
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f) // Use weight to make button take equal space
                                            .height(46.dp) // Set the desired height here
                                    ) {
                                        Text(cancelButtonText, color = Color.Black)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp)) // Optional spacing between buttons

                                    // Confirm Button
                                    Button(
                                        onClick = {
                                            onConfirmAction?.invoke() // Execute confirm action
                                            onClose() // Close the dialog when the confirm button is clicked
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = colorResource(
                                                R.color.grey
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f) // Use weight to make button take equal space
                                            .height(46.dp) // Set the desired height here
                                    ) {
                                        Text(confirmButtonText, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // Delay and navigate after 2 seconds
                if (autooff) {
                    LaunchedEffect(Unit) {
                        delay(2000) // 2 seconds delay
                        navAction?.invoke() // Replace "destination_route" with your actual route
                    }
                }
            }
        }
    }

    companion object {
        private var instance: CustomDialogBuilder? = null
        private var _title: String? = null
        private var _subtitle: String? = null
        private var _message: String? = null
        var show = mutableStateOf(false)

        fun create(): CustomDialogBuilder = CustomDialogBuilder()

        @Composable
        fun ShowProgressDialog(showDialog: Boolean? = true, title: String? = null, subtitle: String? = null, message: String? = null) {
            instance?.dismiss()
            showDialog?.let { show.value = it }
            if (show.value) {
                instance = create()
                    .setTitle(title ?: _title ?: stringResource(R.string.processing))
                    .setSubtitle(subtitle ?: _subtitle ?: stringResource(R.string.plz_wait))
                    .setSmallText(message ?: _message ?: "")
                    .setShowProgressIndicator(true)
                    .setShowCloseButton(false)

                instance?.buildDialog {
                    show.value = false
                }
            }
        }

        fun SetProgressDialog(title: String? = null, subtitle: String? = null, message: String? = null) {
            _title = title
            _subtitle = subtitle
            _message = message
        }

        fun ClearText() {
            _title = null
            _subtitle = null
            _message = null
        }

        fun HideProgress() {
            show.value = false
            instance?.dismiss()
            ClearText()
        }
    }
}
