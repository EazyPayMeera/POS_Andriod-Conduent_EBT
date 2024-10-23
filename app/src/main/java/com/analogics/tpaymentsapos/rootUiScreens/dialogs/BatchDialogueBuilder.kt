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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.Roboto
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay

class BatchDialogueBuilder private constructor() {

    private var title: String = ""
    private var subtitle: String = ""
    private var smallText: String = ""
    private var backgroundColor: Color = Color.White
    private var showProgressIndicator: Boolean = true // Keep this for progress indicator
    private var navAction: (() -> Unit)? = null // New parameter for navigation action
    private val showDialog = mutableStateOf(true)
    var onItemSelected: ((String) -> Unit)? = null
    var listItem: List<String>? = null

    fun setTitle(title: String) = apply { this.title = title }
    fun setSubtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun setSmallText(smallText: String) = apply { this.smallText = smallText }
    fun setBackgroundColor(color: Color) = apply { this.backgroundColor = color }
    fun setShowProgressIndicator(show: Boolean) = apply { this.showProgressIndicator = show }
    fun setNavAction(action: () -> Unit) = apply { this.navAction = action }
    fun dismiss() = apply { this.showDialog.value = false }
    fun onItemSelected(onItemSelected: (String) -> Unit) =
        apply { this.onItemSelected = onItemSelected }

    fun setListItem(listItem: List<String>) = apply { this.listItem = listItem }

    @Composable
    fun buildDialog(onClose: () -> Unit, onItemSelected: ((String) -> Unit)? = null) {
        if (showDialog.value) {
            Dialog(onDismissRequest = { onClose() }) {

                listItem?.let {
                    CustomListDialog(
                        onClose,
                        batchIds = it,
                        startDates = it,
                        endDates = it,
                        onItemSelected = onItemSelected!!
                    )
                }

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
                        shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                        elevation = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium,
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = _title ?: "",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                            )
                            Text(text = _subtitle ?: "")
                            Text(text = _message ?: "")
                            Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium))
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

    @Composable
    fun CustomListDialog(
        onClose: () -> Unit,
        batchIds: List<String>, // List of batch IDs
        startDates: List<String?>, // List of start dates (nullable)
        endDates: List<String>,
        onItemSelected: (String) -> Unit // Callback for item selection
    ) {
        Dialog(onDismissRequest = { onClose() }) {
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            elevation = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium,
                            backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                                    .fillMaxWidth()
                            ) {
                                // Header text
                                Text(
                                    text = title, // Change title if needed
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(bottom = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        // List of items displayed in a column
                        Column {
                            batchIds.forEachIndexed { index, batchId ->
                                // Get the corresponding start date (null if not available)
                                val startDates =
                                    if (index < startDates.size) startDates[index] else null
                                val endDates = if (index < endDates.size) endDates[index] else null
                                val startdate =
                                    startDates?.replace("[", "")?.replace("]", "")?.trim()
                                val enddate = endDates?.replace("[", "")?.replace("]", "")?.trim()

                                DrawersSurface(
                                    modifier = Modifier.fillMaxWidth(),
                                    batchId = batchId, // Pass the combined String to DrawersSurface,
                                    startDate = startdate.toString(),
                                    endDate = enddate.toString(),
                                    onItemSelected = {
                                        onItemSelected(batchId) // Pass only batchId for selection
                                        onClose()
                                    }
                                )


                                // Divider between items, but not after the last item
                                if (index < batchIds.size - 1) {
                                    Divider(
                                        modifier = Modifier.fillMaxWidth(),
                                        thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun DrawersSurface(
        modifier: Modifier = Modifier,
        batchId: String, // Change to String
        startDate: String, // Change to String
        endDate: String, // Change to String
        onItemSelected: () -> Unit // Keep the click handler as before
    ) {
        Surface(
            modifier = Modifier
                .height(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium)
                .clickable { onItemSelected() }, // Call the onItemSelected when clicked
            color = MaterialTheme.colors.surface
        ) {
            DrawersContent(batchId, startDate, endDate)
        }
    }

    @Composable
    fun DrawersContent(
        batchId: String, // Changed to String
        startDate: String, // Changed to String
        endDate: String // Changed to String
    ) {
        Column {
            // Row to align two columns horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium,
                        vertical = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium
                    ), // Padding around the content
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column for batchId and "Open"
                Column(modifier = Modifier.weight(1f)) {
                    // Batch ID Text
                    TextView(
                        text = batchId,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Roboto,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium),
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_21_CompactMedium
                    )

                    // "Open" Text
                    TextView(
                        text = stringResource(id = R.string.open),
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontFamily = Roboto,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_22_CompactMedium,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium)
                    )
                }

                // Right column for startDate and endDate
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    // Start Date
                    TextView(
                        text = startDate,
                        color = Color.Gray,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        modifier = Modifier.padding(end = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium) // Right padding
                    )

                    // End Date
                    Text(
                        text = if(endDate == "null") "-" else endDate, // Check for null or empty string
                        color = Color.Gray,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        modifier = Modifier.padding(top = 4.dp, end = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium) // Padding between dates
                    )
                }
            }
        }
    }


    // DrawerItem data class definition (same as in your example)
    data class DrawerItem(
        val imageRes: ImageVector,
        val text: String,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    )


}


