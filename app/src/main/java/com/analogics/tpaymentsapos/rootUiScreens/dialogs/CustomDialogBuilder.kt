package com.analogics.tpaymentsapos.rootUiScreens.dialogs


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.ui.theme.dashboardCardBgColor
import com.analogics.tpaymentsapos.ui.theme.dimens

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
            BackgroundScreen {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    // Close button
                    if (showCloseButton) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
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
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Subtitle
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.h5,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Small text
                    Text(
                        text = smallText,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
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
