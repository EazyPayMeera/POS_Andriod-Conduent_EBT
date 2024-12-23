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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.DateTimePickerDialog
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.ListDialogueBuilder
import com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel.TxnViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CircularMenu
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getTxnStatusStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getTxnTypeStringId
import com.analogics.tpaymentsapos.ui.theme.Roboto
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListScreen(
    navHostController: NavHostController,
    viewModel: TxnViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current
    val txnList = viewModel.txnList.collectAsState().value
    val batchList = viewModel.batchList.collectAsState().value
    val showFilterMenu = viewModel.showFilterMenu.collectAsState().value
    val showBatchPicker = viewModel.showBatchPicker.collectAsState().value
    val showDateTimePicker = viewModel.showDateTimePicker.collectAsState().value
    val showProgressViewModel = viewModel.showProgress.collectAsState().value

    val showProgressIndicator = remember { mutableStateOf(false) }
    val isSelectingEndDate = remember { mutableStateOf(false) }
    val selectedStartDate = remember { mutableStateOf<LocalDateTime?>(null) }
    val selectedEndDate = remember { mutableStateOf<LocalDateTime?>(null) }

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
                HeaderSection(viewModel, navHostController, sharedViewModel)
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
                                viewModel.onSeeAllClicked()
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
                                    viewModel.onFilterClick()
                                }
                        )

                        // Filter dropdown menu
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { viewModel.onDismissMenu() }
                        ) {
                            // Date filter
                            DropdownMenuItem(onClick = {
                                viewModel.onDateTimeFilterClick()
                            }) {
                                Text(stringResource(id = R.string.select_date))
                            }

                            DropdownMenuItem(onClick = {
                                viewModel.onBatchFilterClick()
                            }) {
                                Text(stringResource(id = R.string.filter_by_batch))
                            }
                        }
                    }
                }

                Divider(color = Color.Gray, thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium)
                // If transaction list is empty
                if (txnList.isEmpty()) {
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
                        items(txnList.size) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sharedViewModel.objRootAppPaymentDetail =
                                            txnList[index]
                                        navHostController.navigate(AppNavigationItems.TransactionDetailsScreen.route)
                                    }
                            ) {
                                TransactionItem(
                                    transaction = txnList[index],
                                    navHostController = navHostController,
                                    sharedViewModel = sharedViewModel
                                )
                            }
                        }
                    }
                }
            }

            // Progress Indicator
            if (showProgressIndicator.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                    CircularProgressIndicator(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                            .size(androidx.compose.material3.MaterialTheme.dimens.DP_100_CompactMedium)
                            .graphicsLayer(alpha = 1.0f)
                    )
                }
            }

            // Floating Circular Menu (Replacing FAB)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(androidx.compose.material3.MaterialTheme.dimens.DP_7_CompactMedium), // Add padding as necessary
                contentAlignment = Alignment.BottomCenter
            ) {
                CircularMenu(
                    menuOptions = listOf(context.resources.getString((R.string.summary)), context.resources.getString((R.string.detail))),
                    onMenuOptionClick = { selectedOption ->
                        when(selectedOption)
                        {
                            context.resources.getString((R.string.summary)) -> {
                                viewModel.printReceipt(0,sharedViewModel,context, false,true,isDetail = false,sharedViewModel.objRootAppPaymentDetail)
                            }
                            context.resources.getString((R.string.detail)) -> {
                                viewModel.printReceipt(0,sharedViewModel,context, false,false,isDetail = true,sharedViewModel.objRootAppPaymentDetail)
                            }
                        }

                    },
                    onPrintClick = {
                        Log.d("Print","Clicked on Print Image")
                        sharedViewModel.objRootAppPaymentDetail.ttlTxnAmount = formatAmount(
                            viewModel.totalPurchaseTransactions(TxnType.PURCHASE) + viewModel.totalPurchaseTransactions(TxnType.AUTHCAP) - viewModel.totalPurchaseTransactions(TxnType.REFUND)
                        )
                        sharedViewModel.objRootAppPaymentDetail.ttlRefundAmount = formatAmount(viewModel.totalPurchaseTransactions(TxnType.REFUND))
                        sharedViewModel.objRootAppPaymentDetail.ttlPurchaseAmount = formatAmount(viewModel.totalPurchaseTransactions(TxnType.PURCHASE) + viewModel.totalPurchaseTransactions(TxnType.AUTHCAP))
                        sharedViewModel.objRootAppPaymentDetail.ttlPurchaseCount = viewModel.totalTransactionsCount(TxnType.PURCHASE)
                        sharedViewModel.objRootAppPaymentDetail.ttlTxnCount = (
                                viewModel.totalTransactionsCount(TxnType.PURCHASE) + viewModel.totalTransactionsCount(TxnType.REFUND)
                                )
                        sharedViewModel.objRootAppPaymentDetail.ttlRefundCount = viewModel.totalTransactionsCount(TxnType.REFUND)
                        sharedViewModel.objRootAppPaymentDetail.ttlTipAmount = viewModel.totalTipAmount().toString()
                        sharedViewModel.objRootAppPaymentDetail.ttlTipCount = viewModel.totalTipCount()
                    }
                )
            }
            CustomDialogBuilder.ShowComposed()
    }
    }

    if (showBatchPicker) {
        ListDialogueBuilder.create()
            .setTitle(stringResource(id = R.string.sel_batch_id))
            .BatchListDialog(
                onClose = { viewModel.onDismissMenu() },
                batchList = batchList,
                onItemSelected = { selectedId ->
                    viewModel.filterTransactionsByBatchId(selectedId)
                }

            )
    }

    if (showDateTimePicker) {
        DateTimePickerDialog(
            onDismissRequest = {  },
            onDateTimeSelected = { selectedDate ->
                selectedStartDate.value = selectedDate // Save selected start date
                isSelectingEndDate.value = true
            }
        )

        if (isSelectingEndDate.value)
        {
            DateTimePickerDialog(
                onDismissRequest = { isSelectingEndDate.value = false},
                onDateTimeSelected = { selectedDate ->
                    selectedEndDate.value = selectedDate // Save selected start date
                    viewModel.onDateTimeFilterApplied(selectedStartDate.value, selectedEndDate.value)
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(navHostController)
    }
    LaunchedEffect(showProgressViewModel) {
        if(showProgressViewModel == true)
            showProgressIndicator.value = true
        else {
            delay(viewModel.minAnimDelayMS)
            showProgressIndicator.value = false
        }
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
                    stringResource(id = getTxnTypeStringId(transaction.txnType)),
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
                val amountColor = when (transaction.txnStatus) {
                    TxnStatus.APPROVED,
                    TxnStatus.CAPTURED
                    -> Color(0xFF4CAF50)
                    else -> Color.Red
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
                                .widthIn(max = androidx.compose.material3.MaterialTheme.dimens.DP_125_CompactMedium) // Set a max width for the amount text
                                .padding(end = androidx.compose.material3.MaterialTheme.dimens.DP_4_CompactMedium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        stringResource(id = getTxnStatusStringId(transaction.txnStatus)),
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
    navHostController: NavHostController,
    sharedViewModel : SharedViewModel
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val isBatchOpen = viewModel.isBatchOpen.collectAsState().value
    val listTypeLabel = viewModel.listTypeLabel.collectAsState().value

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
                    Text(
                        text = listTypeLabel,
                        style = if(listTypeLabel.length>15) MaterialTheme.typography.caption else MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = if(listTypeLabel.length>15) androidx.compose.material3.MaterialTheme.dimens.DP_2_CompactMedium else androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium) // Keep vertical padding for the date text
                    )
                }

                Row ()
                {

                    Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)) // Optional spacing between icon and button

                    Button(
                        onClick = { isDialogVisible=true },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(androidx.compose.material3.MaterialTheme.dimens.DP_36_CompactMedium),
                        enabled = isBatchOpen,
                        contentPadding = PaddingValues(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium) // Smaller padding for compact button
                    ) {
                        TextView(
                            text = stringResource(id = R.string.close_batch),
                            fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_14_CompactMedium,
                            fontWeight = FontWeight.Bold,
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
                val dNetTotal = viewModel.totalPurchaseTransactions(TxnType.PURCHASE) - viewModel.totalPurchaseTransactions(
                    TxnType.REFUND
                )
                val netTotal = formatAmount(dNetTotal)

                Text(
                    text = stringResource(id = R.string.net_total),
                    style = when(netTotal.length) {
                        in 0..13 -> MaterialTheme.typography.h5
                        else -> MaterialTheme.typography.h6
                    },
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = netTotal,
                    style = when(netTotal.length) {
                        in 0..13 -> MaterialTheme.typography.h5
                        else -> MaterialTheme.typography.h6
                        },
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
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
                    viewModel.closeOpenBatches(sharedViewModel)
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

