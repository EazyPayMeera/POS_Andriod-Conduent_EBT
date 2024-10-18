package com.analogics.tpaymentsapos.rootUiScreens.txnList.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListScreen(
    navHostController: NavHostController,
    viewModel: TxnViewModel = hiltViewModel()
) {
    // Fetching transactions to display
    val transactions = viewModel.transactionList.collectAsState().value
    val batchId = viewModel.batchList.collectAsState().value
    val startDate = viewModel.startDateList.collectAsState().value
    val endDate = viewModel.endDateList.collectAsState().value
    val sharedViewModel = localSharedViewModel.current
    // State variables
    val showDateTimePicker = remember { mutableStateOf(false) }
    val BatchId = remember { mutableStateOf(false) }
    val isSelectingEndDate = remember { mutableStateOf(true) }
    val selectedStartDate = remember { mutableStateOf<LocalDateTime?>(null) }
    val selectedEndDate = remember { mutableStateOf<LocalDateTime?>(null) }
    val showMenu = remember { mutableStateOf(false) }

    var isBatchId by remember { mutableStateOf(false) }

    // Date picker logic
    if (showDateTimePicker.value) {


        Log.d("DateTimePicker", "Selected End Date: 1")
        DateTimePickerDialog(
            onDismissRequest = { showDateTimePicker.value = false },
            onDateTimeSelected = { selectedDate ->
                selectedEndDate.value = selectedDate // Save selected start date
                isSelectingEndDate.value = true
            }
        )

        if (isSelectingEndDate.value)
        {
            Log.d("DateTimePicker", "Selected End Date: 2")
            DateTimePickerDialog(
                onDismissRequest = { showDateTimePicker.value = false },
                onDateTimeSelected = { selectedDate ->
                    selectedStartDate.value = selectedDate // Save selected start date
                    isSelectingEndDate.value = false
                }
            )
        }

        if (selectedStartDate.value != null && selectedEndDate.value != null) {
            viewModel.filterTransactionsByDateRange(selectedStartDate.value!!, selectedEndDate.value!!
            )
            showDateTimePicker.value = false
        }
    }

/*    if(BatchId.value)
    {
        sharedViewModel.objPosConfig?.BatchId

        if (sharedViewModel.objPosConfig?.BatchId != null) {
            viewModel.filterTransactionsByBatch(sharedViewModel.objPosConfig?.BatchId!!)
            BatchId.value = false
        }
    }*/

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
                HeaderSection(viewModel, sharedViewModel, navHostController, selectedStartDate.value, selectedEndDate.value)
                SummarySection(viewModel)
            }
        }

        // Transaction list section with filter
        GenericCard(
            modifier = Modifier.padding(
                start = androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium,
                end = androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium,
                bottom = androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium,
                top = 8.dp
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

                        Spacer(modifier = Modifier.width(10.dp))

                        // Menu Trigger Icon for filters
                        Image(
                            painter = painterResource(id = R.drawable.filter_image),
                            contentDescription = "Filter",
                            modifier = Modifier
                                .size(24.dp)
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
                            }) {
                                Text("Select Date")
                            }

                            DropdownMenuItem(onClick = {
                                showMenu.value = false
                                BatchId.value = true
                                isBatchId = true
                            }) {
                                Text("Filter by Batch Number")
                            }
                        }
                    }
                }

                Divider(color = Color.Gray, thickness = 1.dp)
                Log.d("Transaction Size", "Size of transactions: ${transactions.size}")
                // If transaction list is empty
                if (transactions.isEmpty()) {
                    Text(
                        text = "Transaction List is Empty",
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
        }
    }

    if (isBatchId) {
        viewModel.fetchStartDates(batchId)
        viewModel.fetchEndDates(batchId)
        BatchDialogueBuilder.create()
            .setTitle("Select Batch Id")
            .CustomListDialog(
                onClose = { isBatchId = false },
                batchIds = batchId, // Ensure this is List<String>
                startDates = startDate,
                endDates = endDate,
                onItemSelected = { selectedId ->
                    viewModel.filterTransactionsByBatchId(selectedId)
                    Log.d("Selected Item", selectedId) // Logging selectedId should not cause any issues now
                }

            )

    }
    LaunchedEffect(Unit) {
        viewModel.fetchTransactions()
        viewModel.filterTransactionsForBatch()
    }
}






@Composable
fun SummarySection(viewModel: TxnViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
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
                Spacer(modifier = Modifier.width(8.dp)) // Optional spacing between image and text
                Text("Purchase", style = MaterialTheme.typography.body2, color = Color.Gray)
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
                Spacer(modifier = Modifier.width(8.dp)) // Optional spacing between image and text
                Text("Refund", style = MaterialTheme.typography.body2, color = Color.Gray)
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
                    horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_10_CompactMedium,
                    vertical = 8.dp
                ), // Match header padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Log.d("TransactionDateTime", "DateTime: ${transaction.invoiceNo}")
                transaction.dateTime.toString().let {
                    TextView(
                        it,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                        fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium
                    )
                }
                TextView(
                    transaction.txnType.toString(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = Roboto,
                    modifier = Modifier.padding(start = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
                    fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_16_CompactMedium
                )
                Log.d("TransactionDateTime", "TxnType: ${transaction.txnType}")
            }

            // Create a Row for amount and icon
            Row(
                verticalAlignment = Alignment.CenterVertically, // Aligns amount and icon vertically
                modifier = Modifier.padding(start = 8.dp) // Optional padding between details and amount
            ) {
                val amountColor = if (transaction.txnType == TxnType.REFUND || transaction.txnStatus == TxnStatus.DECLINED) {
                    Color.Red
                } else {
                    Color(0xFF4CAF50) // Green for other transaction types
                }

                // Amount Text
                transaction.ttlAmount?.let { formatAmount(it) }?.let {
                    TextView(
                        text = it,
                        color = amountColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Roboto,
                        fontSize =androidx.compose.material3.MaterialTheme.dimens.SP_17_CompactMedium,
                        modifier = Modifier.padding(end = 4.dp) // Optional spacing before the icon
                    )
                }

                // Icon aligned to the end
                IconButton(onClick = {
                    sharedViewModel.objRootAppPaymentDetail = transaction
                    navHostController.navigate(AppNavigationItems.TransactionDetailsScreen.route)
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "")
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
fun HeaderSection(viewModel: TxnViewModel, sharedViewModel: SharedViewModel, navHostController: NavHostController, selectedStartDate: LocalDateTime?, selectedEndDate: LocalDateTime?) {
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
                                modifier = Modifier.padding(top = 10.dp) // Keep vertical padding for the date text
                            )
                        }

                        Box(
                            modifier = Modifier.align(Alignment.CenterHorizontally  )
                        ) {
                            Text(
                                text = "To",
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
                        sharedViewModel.objPosConfig?.BatchId?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.caption,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp) // Keep vertical padding for the date text
                            )
                        }
                    }

                }

                Row (){
                    IconButton(onClick = {
                        sharedViewModel.objRootAppPaymentDetail.ttlTxnAmount = formatAmount(
                            viewModel.totalPurchaseTransactions(TxnType.PURCHASE) - viewModel.totalPurchaseTransactions(TxnType.REFUND)
                        )
                        sharedViewModel.objRootAppPaymentDetail.ttlRefundAmount = formatAmount(viewModel.totalPurchaseTransactions(TxnType.REFUND))
                        sharedViewModel.objRootAppPaymentDetail.ttlPurchaseAmount = formatAmount(viewModel.totalPurchaseTransactions(TxnType.PURCHASE))
                        sharedViewModel.objRootAppPaymentDetail.ttlPurchaseCount = viewModel.totalTransactionsCount(TxnType.PURCHASE)
                        sharedViewModel.objRootAppPaymentDetail.ttlTxnCount = (viewModel.totalTransactionsCount(TxnType.PURCHASE) + viewModel.totalTransactionsCount(TxnType.REFUND))
                        sharedViewModel.objRootAppPaymentDetail.ttlRefundCount = viewModel.totalTransactionsCount(TxnType.REFUND)
                        Log.d("PrintButton", "Print button clicked")
                        /*viewModel.printReceipt(context, true,sharedViewModel.objRootAppPaymentDetail)*/isAlertVisible=true}) {
                        Icon(Icons.Default.Print, contentDescription = "")
                    }

                    Spacer(modifier = Modifier.width(8.dp)) // Optional spacing between icon and button

                    Button(
                        onClick = { isDialogVisible=true },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(36.dp),
                        contentPadding = PaddingValues(8.dp) // Smaller padding for compact button
                    ) {
                        TextView(
                            fontWeight = FontWeight.Bold,
                            text = "Close Batch",
                            fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_14_CompactMedium
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
                .setTitle("Confirmation")
                .setSubtitle("Are you sure?")
                .setSmallText("You want to close the Batch")
                .setShowCloseButton(true) // Can set to false if you don't want the close button
                .setCancelButtonText("Confirm")
                .setConfirmButtonText("Cancel")
                .setCancelable(true)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = androidx.compose.material3.MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    viewModel.printReceipt(context, true,true,sharedViewModel,sharedViewModel.objRootAppPaymentDetail)
                }
                .setOnConfirmAction {
                    viewModel.printReceipt(context, true,false,sharedViewModel,sharedViewModel.objRootAppPaymentDetail)
                }
                .setShowButtons(true)
                .setAutoOff(false)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })

        }
        CustomDialogBuilder.ShowComposed()
        if(isAlertVisible)
        {
            CustomDialogBuilder.create()
                .setTitle("Print?")
                .setSubtitle("")
                .setSmallText("Which Report You want")
                .setShowCloseButton(true) // Can set to false if you don't want the close button
                .setCancelButtonText("Summary")
                .setConfirmButtonText("Detail")
                .setCancelable(true)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = androidx.compose.material3.MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    viewModel.printReceipt(context, true,false,sharedViewModel,sharedViewModel.objRootAppPaymentDetail)
                }
                .setOnConfirmAction {
                    viewModel.printReceipt(context, true,true,sharedViewModel,sharedViewModel.objRootAppPaymentDetail)
                }
                .setShowButtons(true)
                .setAutoOff(false)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isAlertVisible = false })
        }
    }
}

@Composable
fun AlertDialog() {

}
