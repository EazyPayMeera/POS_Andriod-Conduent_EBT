package com.analogics.tpaymentsapos.rootUiScreens.txnList.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.BatchDialogueBuilder
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.DateTimePickerDialog
import com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel.TxnViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.Roboto
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListScreen(
    navHostController: NavHostController,
    viewModel: TxnViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val transactions = viewModel.transactionList.collectAsState().value
    val batchId = viewModel.batchList.collectAsState().value
    val startDate = viewModel.startDateList.collectAsState().value
    val batchStatus = viewModel.batchStatusList.collectAsState().value
    val endDate = viewModel.endDateList.collectAsState().value
    val sharedViewModel = localSharedViewModel.current
    val openBatchId = viewModel.openBatch.collectAsState().value
    Log.d("Inside Header Section", "Open $openBatchId")


    val showDateTimePicker = remember { mutableStateOf(false) }
    val BatchId = remember { mutableStateOf(false) }
    val isSelectingEndDate = remember { mutableStateOf(true) }
    val selectedStartDate = remember { mutableStateOf<LocalDateTime?>(null) }
    val selectedEndDate = remember { mutableStateOf<LocalDateTime?>(null) }
    val showMenu = remember { mutableStateOf(false) }

    var isBatchId by remember { mutableStateOf(false) }


    if (showDateTimePicker.value) {
        Log.d("Date Time Picker", "Prompt DATE PICKER")
        DateTimePickerDialog(
            onDismissRequest = { showDateTimePicker.value = false },
            onDateTimeSelected = { selectedDate ->
                selectedEndDate.value = selectedDate // Save selected start date
                isSelectingEndDate.value = true
            }
        )

        if (isSelectingEndDate.value)
        {
            Log.d("Date Time Picker", "Go For start Date")
            DateTimePickerDialog(
                onDismissRequest = { showDateTimePicker.value = false },
                onDateTimeSelected = { selectedDate ->
                    selectedStartDate.value = selectedDate // Save selected start date
                    isSelectingEndDate.value = false
                }

            )
        }

        if (selectedStartDate.value != null && selectedEndDate.value != null) {
            Log.d("Date Time Picker", "Go for Filter Transaction By Date ")
            viewModel.filterTransactionsByStartEndDate(selectedStartDate.value!!, selectedEndDate.value!!
            )
            showDateTimePicker.value = false
            Log.d("Date Time Picker", "showDateTimePicker.value = false")

        }

    }


    Column {
        CommonTopAppBar(
            title = stringResource(R.string.transactions),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Transactions summary card
        GenericCard(
            modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(modifier = Modifier) {
                HeaderSection(viewModel, sharedViewModel, navHostController, selectedStartDate.value, selectedEndDate.value, viewModel.isClosedBatchEnabled.value)
                SummarySection(viewModel)
            }
        }

        // Transaction list section with filter
        GenericCard(
            modifier = Modifier.padding(
                start = androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium,
                end = androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium,
                bottom = androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium,
                top = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium
            )
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.recentActivity),
                        style = MaterialTheme.typography.h6,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.see_all),
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray,
                            modifier = Modifier.clickable {
                                viewModel.fetchTransactions()
                            }
                        )

                        Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium))

                        // Menu Trigger Icon for filters
                        Image(
                            painter = painterResource(id = R.drawable.filter_image),
                            contentDescription = "",
                            modifier = Modifier
                                .size(androidx.compose.material3.MaterialTheme.dimens.DP_23_CompactMedium)
                                .clickable {
                                    showMenu.value = true
                                }
                        )

                        // Filter dropdown menu
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            // Date filter
                            DropdownMenuItem(onClick = {
                                showMenu.value = false
                                showDateTimePicker.value = true // Show date picker
                                Log.d("Date Time Picker", "Select Date is Clicked")
                                //viewModel.fetchTransactions()
                                selectedStartDate.value = null
                                selectedEndDate.value = null
                                Log.d("Date Time Picker", "Selected dates cleared")
                            }) {
                                Text(stringResource(id = R.string.select_date))
                            }

                            DropdownMenuItem(onClick = {
                                viewModel.fetchStartDates(batchId)
                                viewModel.fetchEndDates(batchId)
                                viewModel.fetchBatchStatus(batchId)
                                showMenu.value = false
                                BatchId.value = true
                                isBatchId = true
                                selectedStartDate.value = null
                                selectedEndDate.value = null
                            }) {
                                Text(stringResource(id = R.string.filter_by_batch))
                            }
                        }
                    }
                }

                Divider(color = Color.Gray, thickness = 1.dp)

                // If transaction list is empty
                if (transactions.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.empty_list),
                        style = MaterialTheme.typography.body1,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    // Transaction list
                    LazyColumn {
                        items(transactions.size) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sharedViewModel.objRootAppPaymentDetail =
                                            transactions[index]
                                        navHostController.navigate(AppNavigationItems.TransactionDetailsScreen.route)
                                    }
                            ) {
                                TransactionItem(
                                    transaction = transactions[index],
                                    navHostController = navHostController,
                                    sharedViewModel = sharedViewModel
                                )
                            }
                        }
                    }
                }
            }

            // Floating Circular Menu (Replacing FAB)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Add padding as necessary
                contentAlignment = Alignment.BottomCenter
            ) {
                CircularMenu(
                    onMenuOptionClick = { selectedOption ->
                        when(selectedOption)
                        {
                            context.resources.getString((R.string.summary)) -> {
                                viewModel.printReceipt(context, true,false,sharedViewModel,sharedViewModel.objRootAppPaymentDetail)
                            }
                            context.resources.getString((R.string.detail)) -> {
                                viewModel.printReceipt(context, true,true,sharedViewModel,sharedViewModel.objRootAppPaymentDetail)
                            }
                        }

                    },
                    onPrintClick = {
                        Log.d("Print","Clicked on Print Image")
                        sharedViewModel.objRootAppPaymentDetail.ttlTxnAmount = formatAmount(
                            viewModel.totalPurchaseTransactions(TxnType.PURCHASE) - viewModel.totalPurchaseTransactions(TxnType.REFUND)
                        )
                        sharedViewModel.objRootAppPaymentDetail.ttlRefundAmount = formatAmount(viewModel.totalPurchaseTransactions(TxnType.REFUND))
                        sharedViewModel.objRootAppPaymentDetail.ttlPurchaseAmount = formatAmount(viewModel.totalPurchaseTransactions(TxnType.PURCHASE))
                        sharedViewModel.objRootAppPaymentDetail.ttlPurchaseCount = viewModel.totalTransactionsCount(TxnType.PURCHASE)
                        sharedViewModel.objRootAppPaymentDetail.ttlTxnCount = (
                                viewModel.totalTransactionsCount(TxnType.PURCHASE) + viewModel.totalTransactionsCount(TxnType.REFUND)
                                )
                        sharedViewModel.objRootAppPaymentDetail.ttlRefundCount = viewModel.totalTransactionsCount(TxnType.REFUND)
                    }
                )
            }
            CustomDialogBuilder.ShowComposed()
    }
    }



    if (isBatchId) {
        if(startDate.isEmpty() && endDate.isEmpty()) {
            Log.d("Start Ad End Date", "Start and End Date Is Empty")
        }
        BatchDialogueBuilder.create()
            .setTitle(stringResource(id = R.string.sel_batch_id))
            .CustomListDialog(
                onClose = { isBatchId = false },
                status = batchStatus,
                batchIds = batchId, // Ensure this is List<String>
                startDates = startDate,
                endDates = endDate,
                onItemSelected = { selectedId ->
                    viewModel.filterTransactionsByBatchId(selectedId)
                }

            )
    }
    LaunchedEffect(Unit) {
        Log.d("Date Time Picker", "Fetch All the Transactions")
        viewModel.fetchTransactions()
        viewModel.filterTransactionsForBatch()
    }

    LaunchedEffect(viewModel.isClosedBatchEnabled) {
        viewModel.isBatchOpen()
    }
}






@Composable
fun SummarySection(viewModel: TxnViewModel) {
    Column(modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Aligns items vertically at the center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // Ensures the image and text are aligned vertically together
            ) {
                Image(
                    painter = painterResource(id = R.drawable.purchase_txn),
                    contentDescription = "",
                    modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed
                )
                Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)) // Optional spacing between image and text
                Text(stringResource(id = R.string.purchase), style = MaterialTheme.typography.body2, color = Color.Gray)
            }

            Text(
                formatAmount(viewModel.totalPurchaseTransactions(TxnType.PURCHASE)),
                style = MaterialTheme.typography.body2
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Aligns items vertically at the center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // Ensures the image and text are aligned vertically together
            ) {
                Image(
                    painter = painterResource(id = R.drawable.refund_txn),
                    contentDescription = "",
                    modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed
                )
                Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)) // Optional spacing between image and text
                Text(stringResource(id = R.string.refund), style = MaterialTheme.typography.body2, color = Color.Gray)
            }

            Text(
                formatAmount(viewModel.totalPurchaseTransactions(TxnType.REFUND)),
                style = MaterialTheme.typography.body2
            )
        }

    }
}

@Composable
fun TransactionItem(
    transaction: ObjRootAppPaymentDetails,
    navHostController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium,
                    vertical = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Transaction type and date column on the left
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    transaction.txnType.toString(),
                    fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                    fontFamily = Roboto,
                    overflow = TextOverflow.Ellipsis
                )
                transaction.dateTime.toString().let {
                    Text(
                        it,
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Amount, status, and icon in a row on the right
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val amountColor = if (transaction.txnType == TxnType.REFUND || transaction.txnStatus == TxnStatus.DECLINED) {
                    Color.Red
                } else {
                    Color(0xFF4CAF50)
                }

                // Column for amount and status
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                ) {
                    transaction.ttlAmount?.let { formatAmount(it) }?.let {
                        Text(
                            text = it,
                            color = amountColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Roboto,
                            fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_17_CompactMedium,
                            modifier = Modifier
                                .widthIn(max = 100.dp) // Set a max width for the amount text
                                .padding(end = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        transaction.txnStatus.toString(),
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium,
                        color = Color.Gray,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Icon aligned to the end
                IconButton(onClick = {
                    sharedViewModel.objRootAppPaymentDetail = transaction
                    navHostController.navigate(AppNavigationItems.TransactionDetailsScreen.route)
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium,
            color = Color.Gray
        )
    }
}






@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection(
    viewModel: TxnViewModel,
    sharedViewModel: SharedViewModel,
    navHostController: NavHostController,
    selectedStartDate: LocalDateTime?,
    selectedEndDate: LocalDateTime?,
    isBatchCloseEnabled : Boolean
) {
    val context = LocalContext.current
    var isDialogVisible by remember { mutableStateOf(false) }
    var isAlertVisible by remember { mutableStateOf(false) }
    var isSummaryReport by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = androidx.compose.material3.MaterialTheme.dimens.DP_15_CompactMedium, // Add extra top padding
                start = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium,
                end = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium // Maintain smaller horizontal padding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium), // Consistent horizontal padding for the inner row
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    if(selectedEndDate != null && selectedStartDate != null) {
                        selectedStartDate?.let {
                            Text(
                                text = it.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                                style = MaterialTheme.typography.caption,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium) // Keep vertical padding for the date text
                            )
                        }

                        Box(
                            modifier = Modifier.align(Alignment.CenterHorizontally  )
                        ) {
                            Text(
                                text = stringResource(id = R.string.to),
                                style = MaterialTheme.typography.caption,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.Center) // Center the Text within the Box
                            )
                        }


                        selectedEndDate?.let {
                            Text(
                                text = it.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                                style = MaterialTheme.typography.caption,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    else
                    {
                        sharedViewModel.objPosConfig?.batchId?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.caption,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp) // Keep vertical padding for the date text
                            )
                        }
                    }

                }

                Row ()
                {

                    Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)) // Optional spacing between icon and button

                    Button(
                        onClick = { isDialogVisible=true },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(androidx.compose.material3.MaterialTheme.dimens.DP_36_CompactMedium),
                        enabled = isBatchCloseEnabled,
                        contentPadding = PaddingValues(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium) // Smaller padding for compact button
                    ) {
                        TextView(
                            text = stringResource(id = R.string.close_batch),
                            fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_14_CompactMedium,
                            fontWeight = FontWeight.Bold,
                            /*overflow = TextOverflow.Ellipsis*/
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium), // Consistent padding for the bottom row
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.net_total),
                    style = MaterialTheme.typography.h5,
                    color = Color.Gray
                )
                val amount = formatAmount(
                    viewModel.totalPurchaseTransactions(TxnType.PURCHASE) - viewModel.totalPurchaseTransactions(
                        TxnType.REFUND
                    ))
                Text(
                    text = amount,
                    style = when(amount.length) {
                        in 0..7 -> MaterialTheme.typography.h4
                        in 8..13 -> MaterialTheme.typography.h5
                        else -> MaterialTheme.typography.h6
                        },
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
        }

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle(stringResource(id = R.string.confirmation))
                .setSubtitle(stringResource(id = R.string.are_you_sure))
                .setSmallText(stringResource(id = R.string.want_to_close_batch))
                .setShowCloseButton(true) // Can set to false if you don't want the close button
                .setCancelButtonText(stringResource(id = R.string.yes))
                .setConfirmButtonText(stringResource(id = R.string.cancel_no))
                .setCancelable(true)
                .setBackgroundColor(MaterialTheme.colors.surface)
                .setProgressColor(color = androidx.compose.material3.MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    viewModel.closeOpenBatches()
                }
                .setOnConfirmAction {

                }
                .setShowButtons(true)
                .setAutoOff(false)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })

        }
    }
}

@Composable
fun AlertDialog() {

}


@Composable
fun CircularMenu(
    onMenuOptionClick: (String) -> Unit,
    onPrintClick: () -> Unit // Add a new parameter for the print click action
) {
    val menuOptions = listOf(
        stringResource(id = R.string.summary),
        stringResource(id = R.string.detail)
    )
    var expanded by remember { mutableStateOf(false) }
    val distance = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val printButtonInitialColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    var printButtonColor by remember { mutableStateOf(printButtonInitialColor) }

    LaunchedEffect(expanded) {
        distance.animateTo(
            targetValue = if (expanded) 80f else 0f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = Modifier
            .size(androidx.compose.material3.MaterialTheme.dimens.DP_100_CompactMedium)
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        menuOptions.forEachIndexed { index, option ->
            val angle = when (index) {
                0 -> -30f // Right
                1 -> 210f // Left
                else -> 0f
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(
                        x = (distance.value * cos(Math.toRadians(angle.toDouble()))).dp,
                        y = (distance.value * sin(Math.toRadians(angle.toDouble()))).dp
                    )
                    .size(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium)
                    .shadow(
                        androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium,
                        shape = CircleShape
                    )
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable {
                        onMenuOptionClick(option)
                        expanded = false
                        scope.launch {
                            printButtonColor = printButtonInitialColor
                        }
                    }
            ) {
                Text(
                    text = option,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                    fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_13_CompactMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium)
                .shadow(
                    androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium,
                    shape = CircleShape
                )
                .background(printButtonColor, shape = CircleShape)
                .clickable {
                    onPrintClick()
                    scope.launch {
                        printButtonColor = if (expanded) {
                            Color.Gray
                        } else {
                            printButtonInitialColor
                        }
                    }
                    expanded = !expanded
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.printer_logo), // Replace with your image resource
                contentDescription = stringResource(id = R.string.print), // Provide a content description for accessibility
                modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_60_CompactMedium) // Adjust size as needed
            )
        }
    }
}
