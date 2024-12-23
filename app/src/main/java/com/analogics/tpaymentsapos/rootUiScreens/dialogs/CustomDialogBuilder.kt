package com.analogics.tpaymentsapos.rootUiScreens.dialogs

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    private var cancelButtonText: String? = null
    private var confirmButtonText: String? = null
    var onItemSelected: ((String) -> Unit) ?= null
    var listItem:List<String> ?= null

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
    fun onItemSelected(onItemSelected: ((String)) -> Unit) = apply { this.onItemSelected = onItemSelected }
    fun setListItem(listItem:List<String>)=apply { this.listItem= listItem}
    @Composable
    fun buildDialog(onClose: () -> Unit,onItemSelected:((String)->Unit)?=null) {
        if (showDialog.value) {
            Dialog(onDismissRequest = { if (isCancelable) onClose() }) {
                if(listItem!=null)
                {
                    CustomListDialog(onClose, items =listItem,onItemSelected=onItemSelected!! )

                }else {
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
                                    backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.primary, // Replace with any color you want
                                    shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_0_CompactMedium),
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
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium)
                                        )
                                    }
                                }

                                // Close button
                                if (showCloseButton) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium),
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
                                    Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium))
                                }

                                // Subtitle
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.h6,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium)
                                )

                                // Message
                                Text(
                                    text = smallText,
                                    style = MaterialTheme.typography.h6,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium)
                                )

                                // Conditionally show Progress Indicator
                                if (showProgressIndicator) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(androidx.compose.material3.MaterialTheme.dimens.DP_21_CompactMedium)
                                            .size(androidx.compose.material3.MaterialTheme.dimens.DP_70_CompactMedium),
                                        color = progressColor,
                                        strokeWidth = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium,
                                    )
                                }

                                if (showButtons) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Cancel Button
                                        cancelButtonText?.let {
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
                                                shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_13_CompactMedium),
                                                modifier = Modifier
                                                    .weight(1f) // Use weight to make button take equal space
                                                    .height(androidx.compose.material3.MaterialTheme.dimens.DP_46_CompactMedium) // Set the desired height here
                                            ) {
                                                Text(it, color = Color.Black)
                                            }

                                            Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)) // Optional spacing between buttons
                                        }
                                        // Confirm Button
                                        confirmButtonText?.let {
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
                                                shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_13_CompactMedium),
                                                modifier = Modifier
                                                    .weight(1f) // Use weight to make button take equal space
                                                    .height(androidx.compose.material3.MaterialTheme.dimens.DP_46_CompactMedium) // Set the desired height here
                                            ) {
                                                Text(it, color = Color.Black)
                                            }
                                        }
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
        private var _okBtnText: String? = null
        private var _cancelBtnText: String? = null
        var onOk: (() -> Unit)? = null
        var onCancel: (() -> Unit)? = null
        var showProgress = mutableStateOf(false)
        var showAlert = mutableStateOf(false)
        var showPrinting = mutableStateOf(false)

        fun create(): CustomDialogBuilder = CustomDialogBuilder()

        @Composable
        fun ShowAlertDialog(show: Boolean? = null, title: String? = null, subtitle: String? = null, message: String? = null, okBtnText: String? = null, cancelBtnText: String? = null) {
            show?.let { showAlert.value = it }
            title?.let { _title = it }
            subtitle?.let { _subtitle = it }
            message?.let { _message = it }

            if (showAlert.value==true) {
                instance = create()
                    .setTitle(title ?: _title ?: "")
                    .setSubtitle(subtitle ?: _subtitle ?: "")
                    .setSmallText(message ?: _message ?: "")
                    .setShowProgressIndicator(false)
                    .setShowCloseButton(false)
                    .setShowButtons(true)
                    .setConfirmButtonText(okBtnText?:_okBtnText?:stringResource(id = R.string.ok))
                    .setOnConfirmAction { onOk?.invoke() }

                cancelBtnText?:_cancelBtnText?.let {
                    instance?.setCancelButtonText(it)
                    instance?.setOnCancelAction { onCancel?.invoke() }
                }

                instance?.buildDialog(onClose = { showAlert.value = false })

            }
        }

        fun composeAlertDialog(show: Boolean?= true, title: String? = null, subtitle: String? = null, message: String? = null, okBtnText: String? = null, onOkClick: (() -> Unit)? = null, cancelBtnText: String? = null, onCancelClick: (() -> Unit)? = null) {
            showProgress.value = false
            showAlert.value = show != false
            _title = title
            _subtitle = subtitle
            _message = message
            _okBtnText = okBtnText
            _cancelBtnText = cancelBtnText
            onOk = onOkClick
            onCancel = onCancelClick
        }

        @Composable
        fun ShowProgressDialog(show: Boolean? = null, title: String? = null, subtitle: String? = null, message: String? = null) {
            show?.let { showProgress.value = it }
            title?.let { _title = it }
            subtitle?.let { _subtitle = it }
            message?.let { _message = it }

            if (showProgress.value==true) {
                instance = create()
                    .setTitle(title ?: _title ?: stringResource(R.string.processing))
                    .setSubtitle(subtitle ?: _subtitle ?: stringResource(R.string.plz_wait))
                    .setSmallText(message ?: _message ?: "")
                    .setShowProgressIndicator(true)
                    .setShowCloseButton(false)
                instance?.buildDialog(onClose = { showProgress.value = false })
            }
        }

        fun composeProgressDialog(show: Boolean? = true, title: String? = null, subtitle: String? = null, message: String? = null) {
            showAlert.value = false
            showProgress.value = show != false
            _title = title
            _subtitle = subtitle
            _message = message
        }

        @Composable
        fun ShowPrintingDialog(show: Boolean? = null, title: String? = null, subtitle: String? = null, message: String? = null, buttonText: String? = null) {
            show?.let { showPrinting.value = it }
            title?.let { _title = it }
            subtitle?.let { _subtitle = it }
            message?.let { _message = it }

            if (showPrinting.value==true) {
                instance = create()
                    .setTitle(title ?: _title ?: "")
                    .setSubtitle(subtitle ?: _subtitle ?: "")
                    .setSmallText(message ?: _message ?: "")
                    .setShowProgressIndicator(true)
                    .setShowCloseButton(false)
                    .setShowButtons(true)
                    .setConfirmButtonText(buttonText?:_okBtnText?:stringResource(id = R.string.abort))

                instance?.buildDialog(onClose = {
                    onOk?.invoke() // Call the onClose callback if it’s set
                    showPrinting.value = false
                    instance?.dismiss()
                })

            }
        }

        fun composePrintingDialog(show: Boolean? = true, title: String? = null, subtitle: String? = null, message: String? = null, buttonText: String? = null,onClose: (() -> Unit)? = null) {
            showProgress.value = false
            showPrinting.value = show != false
            _title = title
            _subtitle = subtitle
            _message = message
            _okBtnText = buttonText
            onOk = onClose
        }

        @Composable
        fun ShowComposed()
        {
            ShowProgressDialog()
            ShowAlertDialog()
            ShowPrintingDialog()
        }

        fun clearText() {
            _title = null
            _subtitle = null
            _message = null
        }

        fun hideProgress() {
            showProgress.value = false
            showPrinting.value = false
            instance?.dismiss()
            clearText()
        }
    }


    @Composable
    fun CustomListDialog(
        onClose: () -> Unit,
        items: List<String>?,
        onItemSelected: (String) -> Unit
    ) {
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
                        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.primary, // Replace with any color you want
                        shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium),
                    ) {
                        Column {
                            androidx.compose.material3.Text(
                                text = stringResource(id = R.string.sel_app_id),
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                            )
                        }
                    }
                    LazyColumn {
                        items(items ?: emptyList()) { item -> // Safely handle null items
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = androidx.compose.material3.MaterialTheme.dimens.DP_0_CompactMedium),
                                elevation = CardDefaults.elevatedCardElevation(androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium)
                            ) {
                                Column {
                                    BatchSurface(item = item)

/*                                    Divider(
                                        modifier = Modifier.fillMaxWidth(),
                                        thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium,
                                        color = Color.Gray
                                    )*/
                                }
                            }
                        }
                    }

                }


            }
        }
    }

    @Composable
    fun BatchSurface(
        item: String,
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
        ) {
            BatchContent(
                item = item
            )
        }
    }

    @Composable
    fun BatchContent(
        item: String
    ) {
        // Handle the click event to toggle the switch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                .clickable {

                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium), // Added padding for better touch area
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
/*                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Close,  // Use Icon for ImageVector
                        contentDescription = "",
                        modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_28_CompactMedium)
                    )*/

                    androidx.compose.material3.Text(
                        text = item,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                    )
                }

            }
        }
    }



}
