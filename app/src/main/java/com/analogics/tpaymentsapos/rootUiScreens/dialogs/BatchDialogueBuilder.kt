package com.analogics.tpaymentsapos.rootUiScreens.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

class BatchDialogueBuilder private constructor() {

    private var title: String = ""
    private var subtitle: String = ""
    private var smallText: String = ""
    private var backgroundColor: Color = Color.White
    private var showProgressIndicator: Boolean = true // Keep this for progress indicator
    private var progressColor: Color = Color(0xFFFF9800) // Keep this for progress color
    private var navAction: (() -> Unit)? = null // New parameter for navigation action
    private val showDialog = mutableStateOf(true)
    var onItemSelected: ((String) -> Unit)? = null
    var listItem: List<String>? = null

    fun setTitle(title: String) = apply { this.title = title }
    fun setSubtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun setSmallText(smallText: String) = apply { this.smallText = smallText }
    fun setBackgroundColor(color: Color) = apply { this.backgroundColor = color }
    fun setProgressColor(color: Color) = apply { this.progressColor = color }
    fun setShowProgressIndicator(show: Boolean) = apply { this.showProgressIndicator = show }
    fun setNavAction(action: () -> Unit) = apply { this.navAction = action }
    fun dismiss() = apply { this.showDialog.value = false }
    fun onItemSelected(onItemSelected: (String) -> Unit) = apply { this.onItemSelected = onItemSelected }
    fun setListItem(listItem: List<String>) = apply { this.listItem = listItem }

    @Composable
    fun buildDialog(onClose: () -> Unit, onItemSelected: ((String) -> Unit)? = null) {
        if (showDialog.value) {
            Dialog(onDismissRequest = { onClose() }) {

                listItem?.let { CustomListDialog(onClose, items = it, onItemSelected = onItemSelected!!) }

                LaunchedEffect(Unit) {
                    delay(2000)
                    navAction?.invoke()
                }
            }
        }
    }

    companion object {
        private var instance: BatchDialogueBuilder? = null
        private var _title: String? = null
        private var _subtitle: String? = null
        private var _message: String? = null
        private var _buttonText: String? = null
        var showProgress = mutableStateOf(false)
        var showAlert = mutableStateOf(false)

        fun create(): BatchDialogueBuilder = BatchDialogueBuilder()

        @Composable
        fun ShowAlertDialog(
            show: Boolean? = null,
            title: String? = null,
            subtitle: String? = null,
            message: String? = null,
            buttonText: String? = null
        ) {
            show?.let { showAlert.value = it }
            title?.let { _title = it }
            subtitle?.let { _subtitle = it }
            message?.let { _message = it }
            buttonText?.let { _buttonText = it }

            if (showAlert.value) {
                Dialog(onDismissRequest = { showAlert.value = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 8.dp,
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = _title ?: "",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(text = _subtitle ?: "")
                            Text(text = _message ?: "")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAlert.value = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = _buttonText ?: "OK")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomListDialog(
    onClose: () -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = { onClose() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Select an item",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(items.size) { index ->
                        Text(
                            text = items[index],
                            modifier = Modifier
                                .clickable {
                                    onItemSelected(items[index])
                                    onClose()
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }

}
