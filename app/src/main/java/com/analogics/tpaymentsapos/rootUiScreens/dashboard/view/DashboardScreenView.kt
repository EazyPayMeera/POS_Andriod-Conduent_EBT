package com.analogics.tpaymentsapos.rootUiScreens.dashboard.view


import android.util.Log
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
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.rememberDrawerState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.model.DashboardItemList
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel.DashboardViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppHeader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CardWithImageText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomDrawerContent
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.removeNonDigits
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DashboardView(navHostController: NavHostController) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val sharedViewModel= localSharedViewModel.current

    TrainingView(
        navHostController = navHostController,
        dashboardViewModel,
        dashboardItemLists = dashboardItemListData(navHostController, dashboardViewModel, sharedViewModel)
    ) {}
    LaunchedEffect(Unit) {
        dashboardViewModel.fetchLastTransactions()
    }
}

@Composable
fun dashboardItemListData(
    navHostController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    sharedViewModel : SharedViewModel
): List<DashboardItemList> {

    // Helper function to set the transaction state
    fun setTransactionType(txnType: TxnType) {
        sharedViewModel.objRootAppPaymentDetail.id = removeNonDigits(getCurrentDateTime(AppConstants.UNIQUE_ID_DATE_TIME_FORMAT)).toLong()
        sharedViewModel.objRootAppPaymentDetail.txnType = txnType
    }

    // Helper function to create DashboardItemList
    @Composable
    fun createDashboardItem(
        titleId: Int,
        iconId: Int,
        route: String,
        onClickState: () -> Unit
    ): DashboardItemList {
        return DashboardItemList(
            stringResource(id = titleId),
            iconId,
            onClick = {
                dashboardViewModel.navigateTo(navHostController, route)
                onClickState()
            }
        )
    }

    return listOf(
        createDashboardItem(
            titleId = R.string.purchase,
            iconId = R.drawable.purchase,
            route = AppNavigationItems.InvoiceScreen.route,
            onClickState = { setTransactionType(TxnType.PURCHASE) }
        ),
        createDashboardItem(
            titleId = R.string.refund,
            iconId = R.drawable.dashboard_refund,
            route = AppNavigationItems.PasswordScreen.route,
            onClickState = { setTransactionType(TxnType.REFUND) }
        ),
        createDashboardItem(
            titleId = R.string.pre_auth,
            iconId = R.drawable.dashboard_preauth,
            route = AppNavigationItems.InvoiceScreen.route,
            onClickState = { setTransactionType(TxnType.PREAUTH) }
        ),
        createDashboardItem(
            titleId = R.string.auth_capture,
            iconId = R.drawable.dashboard_auth_capture,
            route = AppNavigationItems.InvoiceScreen.route,
            onClickState = { setTransactionType(TxnType.AUTHCAP) }
        ),
        createDashboardItem(
            titleId = R.string.void_trans,
            iconId = R.drawable.dashboard_void,
            route = AppNavigationItems.PasswordScreen.route,
            onClickState = { setTransactionType(TxnType.VOID) }
        ),
        createDashboardItem(
            titleId = R.string.transactions,
            iconId = R.drawable.dashboard_transaction,
            route = AppNavigationItems.TxnListScreen.route,
            onClickState = { setTransactionType(TxnType.TXNLIST) }
        )
    )
}

@Composable
fun TrainingView(
    navHostController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    dashboardItemLists: List<DashboardItemList>,
    onMenuItemClick: (String) -> Unit
) {
    val sharedViewModel= localSharedViewModel.current
    val selectedButton = dashboardViewModel.selectedButton.value
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Function to handle drawer open and close
    fun toggleDrawer(open: Boolean) {
        coroutineScope.launch {
            if (open) drawerState.open() else drawerState.close()
        }
    }

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawerContent(
                navHostController = navHostController,
                onMenuItemClick = onMenuItemClick,
                onCloseDrawer = { toggleDrawer(false) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppHeader(
                    title = stringResource(id = R.string.application_name),
                    onBackButtonClick = { /* Handle back button click if needed */ },
                    icon1 = R.drawable.baseline_menu_24,
                    onIcon1Click = { toggleDrawer(true) },
                    backgroundColor = MaterialTheme.colorScheme.onPrimary,
                    isIcon2Visible = false
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(10.dp)
                ) {
                    DashboardContentSurface(
                        sharedViewModel = sharedViewModel,
                        viewModel = dashboardViewModel,
                        dashboardItemLists = dashboardItemLists,
                        selectedButton = selectedButton,
                        onButtonClick = { text, onClick ->
                            dashboardViewModel.onButtonClick(text, onClick,sharedViewModel)
                        }
                    )
                }
            }
        )
    }

    /* Initialize Payment SDK */
    LaunchedEffect(Unit) {
        dashboardViewModel.clearTransData(sharedViewModel)
        dashboardViewModel.initPaymentSDK(context, sharedViewModel)
    }

    CustomDialogBuilder.ShowComposed()
}


@Composable
fun DashboardContentSurface(
    sharedViewModel: SharedViewModel,
    viewModel: DashboardViewModel,
    dashboardItemLists: List<DashboardItemList>,
    selectedButton: String?,
    onButtonClick: (String, () -> Unit) -> Unit
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val transactions = viewModel.lastTransactionList.collectAsState().value
    Surface(
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.DP_9_CompactMedium),
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        elevation = MaterialTheme.dimens.DP_10_CompactMedium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_15_CompactMedium),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDemoMode = sharedViewModel.objPosConfig?.isDemoMode
            Log.d("DashboardContentSurface", "isDemoMode: $isDemoMode")

            var visibility by remember { mutableStateOf(true) }

            // Use a Box to maintain space and center the text
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Make the box fill the width
                    .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                    .height(MaterialTheme.dimens.DP_33_CompactMedium) // Set a fixed height for the box
                    .wrapContentHeight() // Ensure the height wraps content
            ) {
                TextView(
                    text = if (visibility && isDemoMode == true) stringResource(id = R.string.training_mode) else "",
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center) // Center the text in the box
                )
            }

            // LaunchedEffect that toggles visibility
            LaunchedEffect(Unit) {
                while (true) {
                    delay(AppConstants.TRAINING_MODE_BLINK_DELAY_MS)
                    visibility = !visibility // Toggle visibility
                }
            }

            dashboardItemLists.chunked(2).forEach { rowConfigs ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.dimens.DP_5_CompactMedium,
                            end = MaterialTheme.dimens.DP_5_CompactMedium,
                            bottom = MaterialTheme.dimens.DP_5_CompactMedium
                        ),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.DP_5_CompactMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowConfigs.forEach { config ->
                        CardWithImageText(
                            text = config.text,
                            imageResId = config.iconResId,
                            isSelected = selectedButton == config.text,
                            onClick = {
                                Log.d("CardWithImageText", "Card clicked: ")
                                onButtonClick(config.text, config.onClick) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(MaterialTheme.dimens.DP_4_CompactMedium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    /*.padding(top = MaterialTheme.dimens.DP_25_CompactMedium)*/,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    AppButton(
                        onClick = {
                            isDialogVisible = true
                            if (transactions.isNotEmpty()) {
                                viewModel.printReceipt(context, true,sharedViewModel.objRootAppPaymentDetail)
                            }
                        },
                        title = stringResource(id = R.string.print_last_receipt),
                        image = painterResource(id = R.drawable.ic_print)
                    )
            }
            if (isDialogVisible) {
                CustomDialogBuilder.create()
                    .setTitle("Printing")
                    .setSubtitle("Please Wait")
                    .setSmallText("Merchant Receipt")
                    .setShowCloseButton(true) // Can set to false if you don't want the close button
                    .setCancelable(true)
                    .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                    .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
                    .buildDialog(onClose = { isDialogVisible = false })
            }
        }
    }
}
