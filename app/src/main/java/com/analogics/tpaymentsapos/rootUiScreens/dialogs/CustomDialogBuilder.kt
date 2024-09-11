package com.analogics.tpaymentsapos.rootUiScreens.dialogs


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.ui.theme.dashboardCardBgColor
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay

class CustomDialogBuilder private constructor() {


    private var title: String = "Title"
    private var subtitle: String = "Subtitle"
    private var smallText: String = "Small Text"
    private var showCloseButton: Boolean = true
    private var isCancelable: Boolean = true
    private var backgroundColor: Color = dashboardCardBgColor
    private var progressColor: Color = Color(0xFFFF9800)
    private var navAction: (() -> Unit)? = null // New parameter for navigation actio


    fun setTitle(title: String) = apply { this.title = title }
    fun setSubtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun setSmallText(smallText: String) = apply { this.smallText = smallText }
    fun setShowCloseButton(showClose: Boolean) = apply { this.showCloseButton = showClose }
    fun setCancelable(cancelable: Boolean) = apply { this.isCancelable = cancelable }
    fun setBackgroundColor(color: Color) = apply { this.backgroundColor = color }
    fun setProgressColor(color: Color) = apply { this.progressColor = color }
    fun setNavAction(action: () -> Unit) = apply { this.navAction = action } // Set navigation action

    @Composable
    fun buildDialog(onClose: () -> Unit) {
        Dialog(onDismissRequest = { if (isCancelable) onClose() }) {
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
                    ){
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_30_CompactMedium)
                        ) {

                            // Title
                            Text(
                                text = smallText,
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

/*                    // Small text
                    Text(
                        text = smallText,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )*/

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
            // Delay and navigate after 2 seconds
            LaunchedEffect(Unit) {
                delay(2000) // 2 seconds delay
                navAction?.invoke() // Replace "destination_route" with your actual route
            }
            
        }
    }

    companion object {
        fun create(): CustomDialogBuilder = CustomDialogBuilder()
    }
}
