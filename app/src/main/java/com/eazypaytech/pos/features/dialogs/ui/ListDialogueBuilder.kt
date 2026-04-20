package com.eazypaytech.pos.features.dialogs.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.analogics.securityframework.database.entity.BatchEntity
import com.eazypaytech.pos.R
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.themes.fonts.Roboto
import com.eazypaytech.pos.core.themes.dimens

class ListDialogueBuilder private constructor() {

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

    companion object {
        fun create(): ListDialogueBuilder = ListDialogueBuilder()
    }

    @Composable
    fun BatchListDialog(
        onClose: () -> Unit,
        batchList: List<BatchEntity>?, // List of batch IDs
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
                            shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_0_CompactMedium),
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
                        // Check if batchIds is empty
                        if (batchList?.isEmpty()==true) {
                            // Display "Batch list Empty" message
                            Text(
                                text = stringResource(id = R.string.empty_list),
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier
                                    .padding(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn {
                                itemsIndexed(batchList?: emptyList()) { index, item ->

                                    BatchListItemSurface(
                                        isBatchOpen = item.batchStatus?.toString()?.lowercase()=="open",
                                        batchId = stringResource(id = R.string.lbl_batch_id)+(item.batchId?.toIntOrNull()?.toString()?:""), // Pass the combined String to DrawersSurface
                                        cashierId = item.cashierId?:"",
                                        startDate = item.openedDateTime?.toString()?:"",
                                        endDate = item.closedDateTime?.toString()?:"",
                                        onItemSelected = {
                                            onItemSelected(item.batchId.toString()) // Pass only batchId for selection
                                            onClose()
                                        }
                                    )

                                    // Divider between items, but not after the last item
                                    if (index < (batchList?.size?:0) - 1) {
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
    }

    @Composable
    fun UserListDialog(
        title: String,
        msgOnEmpty: String,
        onClose: () -> Unit,
        users: List<String>,
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
                            shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_0_CompactMedium),
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
                                    text = title, // Update with appropriate title if needed
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(bottom = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        // Check if users list is empty
                        if (users.isEmpty()) {
                            Text(
                                text = msgOnEmpty,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier
                                    .padding(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            // Display each user ID with a divider after each full entry in the users list
                            Column {
                                users.forEachIndexed { index, userId ->
                                    // Clean userId (ensure there are no unwanted characters)
                                    val cleanUserIds = userId.replace("[", "").replace("]", "").split(",") // Split by comma if there are multiple IDs in a single entry

                                    // Process each clean userId and display it
                                    cleanUserIds.forEach { cleanUserId ->
                                        val trimmedUserId = cleanUserId.trim() // Ensure no extra spaces
                                        Log.d("UserListDialog", "CleanUserID: $trimmedUserId")

                                        // Display the user in a UserSurface
                                        UserSurface(
                                            modifier = Modifier.fillMaxWidth(),
                                            user = trimmedUserId, // Pass cleaned user ID to UserSurface
                                            onItemSelected = {
                                                Log.d("UserSelection", "Selected User ID: $trimmedUserId")
                                                onItemSelected(trimmedUserId) // Pass cleaned ID for selection
                                                onClose()
                                            }
                                        )

                                        // Add divider after each user, except for the last one
                                        if (index < users.size - 1 || cleanUserIds.indexOf(cleanUserId) < cleanUserIds.size - 1) {
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
        }
    }




    @Composable
    fun BatchListItemSurface(
        isBatchOpen: Boolean,
        batchId: String, // Change to String
        cashierId : String,
        startDate: String, // Change to String
        endDate: String, // Change to String4
        onItemSelected: () -> Unit // Keep the click handler as before
    ) {
        Surface(
            modifier = Modifier
                .height(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium)
                .clickable { onItemSelected() }, // Call the onItemSelected when clicked
            color = MaterialTheme.colors.surface
        ) {
            BatchListItemSurfaceContent(isBatchOpen,batchId, cashierId, startDate, endDate)
        }
    }

    @Composable
    fun UserSurface(
        modifier: Modifier = Modifier,
        user:String,
        onItemSelected: () -> Unit // Keep the click handler as before
    ) {
        Surface(
            modifier = Modifier
                .height(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium)
                .clickable { onItemSelected() }, // Call the onItemSelected when clicked
            color = MaterialTheme.colors.surface
        ) {
            Log.d("UserSurface", "Selected User ID: $user")
            UsersContent(user)
        }
    }

    @Composable
    fun BatchListItemSurfaceContent(
        isBatchOpen: Boolean,
        batchId: String, // Changed to String
        cashierId: String, // Changed to String
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
                    Text(
                        text = batchId,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Roboto,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium),
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_21_CompactMedium
                    )


                    Text(
                        text = cashierId,
                        color =  Color.Gray,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(
                                start = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium,
                                top = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium
                            )
                            .align( Alignment.Start) // Padding between dates
                    )
                }

                // Right column for startDate and endDate
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    // Start Date
                    Text(
                        text = startDate,
                        color = Color.Gray,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(end = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.End) // Right padding
                    )

                    Text(
                        text = if (isBatchOpen) "" else stringResource(id = R.string.to),
                        color = Color.Gray,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .padding(
                                end = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium
                            )
                            .align(Alignment.CenterHorizontally) // Padding between dates
                    )

                    // End Date
                    // End Date, show "-" if status is "open"
                    Text(
                        text = if (isBatchOpen) stringResource(id = R.string.open) else endDate,
                        color = if(isBatchOpen) Color(0xFF4CAF50) else Color.Gray,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(
                                end = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium
                            )
                            .align(if (isBatchOpen) Alignment.CenterHorizontally else Alignment.End) // Padding between dates
                    )
                }
            }
        }

    }

    @Composable
    fun UsersContent(
        user:String,
    ) {
        Column {
            // Row to align two columns horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium,
                        vertical = androidx.compose.material3.MaterialTheme.dimens.DP_22_CompactMedium
                    ), // Padding around the content
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column for batchId and "Open"
                Column(modifier = Modifier.weight(1f)) {
                    // Batch ID Text
                    TextView(
                        text = user,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Roboto,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium),
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_27_CompactMedium
                    )

                }

            }
        }
    }
}


