package com.analogics.tpaymentsapos.rootUiScreens.dialogs


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.DialogueScreen
import com.analogics.tpaymentsapos.ui.theme.dashboardCardBgColor

class CustomDialogBuilder private constructor() {

    private var title: String = "Title"
    private var subtitle: String = "Subtitle"
    private var smallText: String = "Small Text"
    private var showCloseButton: Boolean = true
    private var isCancelable: Boolean = true
    private var backgroundColor: Color = dashboardCardBgColor
    private var progressColor: Color = Color(0xFFFF9800)


    fun setTitle(title: String) = apply { this.title = title }
    fun setSubtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun setSmallText(smallText: String) = apply { this.smallText = smallText }
    fun setShowCloseButton(showClose: Boolean) = apply { this.showCloseButton = showClose }
    fun setCancelable(cancelable: Boolean) = apply { this.isCancelable = cancelable }
    fun setBackgroundColor(color: Color) = apply { this.backgroundColor = color }
    fun setProgressColor(color: Color) = apply { this.progressColor = color }

    @Composable
    fun buildDialog(onClose: () -> Unit) {
        Dialog(onDismissRequest = { if (isCancelable) onClose() }) {
            DialogueScreen {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
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
                    }

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Subtitle
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.h5,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Small text
                    Text(
                        text = smallText,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )

                    // Progress Indicator
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(20.dp)
                            .size(70.dp),
                        color = progressColor,
                        strokeWidth = 4.dp,
                    )
                }
            }
        }
    }

    companion object {
        fun create(): CustomDialogBuilder = CustomDialogBuilder()
    }
}
