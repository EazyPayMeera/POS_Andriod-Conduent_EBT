package com.eazypaytech.posafrica.features.dashboard.ui



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.activity.ui.localSharedViewModel
import com.eazypaytech.posafrica.domain.model.DashboardItemList
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.features.password.ui.PasswordUtil
import com.eazypaytech.posafrica.core.ui.components.inputfields.AppHeader
import com.eazypaytech.posafrica.core.ui.components.inputfields.CardWithImageText
import com.eazypaytech.posafrica.core.ui.components.inputfields.CircularMenu
import com.eazypaytech.posafrica.core.ui.components.drawer.CustomDrawerContent
import com.eazypaytech.posafrica.core.utils.HideSoftKeyboard
import com.eazypaytech.posafrica.core.utils.getCurrentDateTime
import com.eazypaytech.posafrica.core.utils.removeNonDigits
import com.eazypaytech.posafrica.core.themes.dimens
//import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
//import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
//import com.eazypaytech.posafrica.rootUiScreens.dashboard.model.DashboardItemList
//import com.eazypaytech.posafrica.rootUiScreens.dashboard.viewModel.DashboardViewModel
//import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
//import com.eazypaytech.posafrica.rootUiScreens.password.view.PasswordUtil
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.AppHeader
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CardWithImageText
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CircularMenu
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CustomDrawerContent
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.HideSoftKeyboard
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.removeNonDigits
//import com.eazypaytech.posafrica.ui.theme.dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun DashboardScreen(navHostController: NavHostController) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current
    val isPasswordPrompt = remember { mutableStateOf(false) }
    val receivedEvent = remember { mutableStateOf("") }

    // Helper function to set the transaction state
    fun setTransactionType(txnType: TxnType) {
        sharedViewModel.objRootAppPaymentDetail.id = removeNonDigits(getCurrentDateTime(AppConstants.UNIQUE_ID_DATE_TIME_FORMAT)).toLong()
        sharedViewModel.objRootAppPaymentDetail.txnType = txnType
        if(sharedViewModel.objPosConfig?.isPromptInvoiceNo!=true)
            dashboardViewModel.setInvoiceNumber(sharedViewModel)
        //Log.d("TRANSACTION_TYPE", "Txn Type Selected: ${sharedViewModel.objRootAppPaymentDetail.txnType}")
    }

    fun handleDashboardTrigger(event: String) {
        when (event) {
            /* Configuration Menu Events */
            AppConstants.BUTTON_CLICK_EVENT_SET_LANGUAGE -> navHostController.navigate(
                AppNavigationItems.LanguageScreen.route
            )
            AppConstants.BUTTON_CLICK_EVENT_USER_MANAGEMENT -> navHostController.navigate(
                AppNavigationItems.UserManagementScreen.route
            )
            AppConstants.BUTTON_CLICK_EVENT_SUMMARY -> navHostController.navigate(
                AppNavigationItems.TxnListScreen.route
            )
            AppConstants.BUTTON_CLICK_EVENT_KEY_MAN -> navHostController.navigate(
                AppNavigationItems.KeyEntryScreen.route
            )
            AppConstants.BUTTON_CLICK_EVENT_CONFIGURATION -> navHostController.navigate(
                AppNavigationItems.ConfigurationScreen.route
            )
            AppConstants.BUTTON_CLICK_EVENT_RE_ACTIVATE_DEVICE -> dashboardViewModel.onReactivate(
                navHostController,
                sharedViewModel
            )
            AppConstants.BUTTON_CLICK_EVENT_LOGOUT -> navHostController.navigate(
                AppNavigationItems.ConfirmShiftScreen.route
            )

            /* Dashboard Menu Events */
            AppConstants.BUTTON_CLICK_EVENT_FOOD_PURCHASE -> {
                navHostController.navigate(
                    AppNavigationItems.TxnSelScreen.route
                )
            }

            AppConstants.BUTTON_CLICK_EVENT_FOODSTAMP_RETURN -> {
                setTransactionType(TxnType.FOODSTAMP_RETURN)
                navHostController.navigate(
                    AppNavigationItems.AmountScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_PURCHASE_CASHBACK -> {
                setTransactionType(TxnType.PURCHASE_CASHBACK)
                navHostController.navigate(
                    AppNavigationItems.AmountScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_CASH_WITHDRAW -> {
                setTransactionType(TxnType.CASH_WITHDRAWAL)
                navHostController.navigate(
                    AppNavigationItems.AmountScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_E_VOUCHER -> {
                setTransactionType(TxnType.E_VOUCHER)
                navHostController.navigate(
                    AppNavigationItems.EBTSelScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_VOID_LAST -> {
                setTransactionType(TxnType.VOID_LAST)
                navHostController.navigate(
                    AppNavigationItems.AmountScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_BALANCE_ENQUIRY -> {
                navHostController.navigate(
                    AppNavigationItems.EBTSelScreen.route
                )
            }
            else -> null
        }
    }

    TrainingView(
        navHostController = navHostController,
        dashboardViewModel,
        dashboardItemLists = dashboardItemListData()
    ) { event, isPassword ->
        isPasswordPrompt.value = isPassword
        if (isPassword) {
            receivedEvent.value = event
        } else {
            handleDashboardTrigger(event)
        }
    }

    PasswordUtil.PromptUserPassword(navHostController, isPasswordPrompt.value) {
        isPasswordPrompt.value = false
        if(it==true)    /* Only If Password Validation Is Successful */
            handleDashboardTrigger(receivedEvent.value)
    }
}



@Composable
fun dashboardItemListData(
): List<DashboardItemList> {

    // Helper function to create DashboardItemList
    @Composable
    fun createDashboardItem(
        titleId: Int,
        iconId: Int,
        event: String,
        isPassword: Boolean = false
    ): DashboardItemList {
        return DashboardItemList(
            stringResource(id = titleId),
            iconId,
            event = event,
            isPassword = isPassword
        )
    }

    return listOf(
        createDashboardItem(
            titleId = R.string.ebt_food_purchase,
            iconId = R.drawable.ebt_dashboard_food_purchase,
            event = AppConstants.BUTTON_CLICK_EVENT_FOOD_PURCHASE,
            false
        ),
        createDashboardItem(
            titleId = R.string.ebt_foodstamp_return,
            iconId = R.drawable.ebt_dashboard_food_return,
            event = AppConstants.BUTTON_CLICK_EVENT_FOODSTAMP_RETURN,
            false
        ),
        createDashboardItem(
            titleId = R.string.ebt_purchase_cashback,
            iconId = R.drawable.ebt_dashboard_cash_purchase,
            event = AppConstants.BUTTON_CLICK_EVENT_PURCHASE_CASHBACK,
            false
        ),
        createDashboardItem(
            titleId = R.string.ebt_cash_withdraw,
            iconId = R.drawable.ebt_dashboard_cash_purchase,
            event = AppConstants.BUTTON_CLICK_EVENT_CASH_WITHDRAW,
            false
        ),
        createDashboardItem(
            titleId = R.string.ebt_e_voucher,
            iconId = R.drawable.ebt_dashboard_e_voucher,
            event = AppConstants.BUTTON_CLICK_EVENT_E_VOUCHER,
            false
        ),
        createDashboardItem(
            titleId = R.string.ebt_bal_inquiry,
            iconId = R.drawable.ebt_dashboard_bal_inquiry,
            event = AppConstants.BUTTON_CLICK_EVENT_BALANCE_ENQUIRY,
            false
        ),
        createDashboardItem(
            titleId = R.string.ebt_void_last,
            iconId = R.drawable.ebt_dashboard_void_last,
            event = AppConstants.BUTTON_CLICK_EVENT_VOID_LAST,
            false
        )
    )
}

@Composable
fun TrainingView(
    navHostController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    dashboardItemLists: List<DashboardItemList>,
    onMenuItemClick: (String, Boolean) -> Unit
) {
    val sharedViewModel= localSharedViewModel.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val isAdmin = dashboardViewModel.isAdmin.collectAsState().value
    val context = LocalContext.current

    // Function to handle drawer open and close
    fun toggleDrawer(open: Boolean) {
        coroutineScope.launch {
            if (open) drawerState.open() else drawerState.close()
        }
    }

    HideSoftKeyboard()

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawerContent(
                isAdmin = isAdmin,
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
                        .padding(MaterialTheme.dimens.DP_11_CompactMedium)
                ) {
                    DashboardContentSurface(
                        sharedViewModel = sharedViewModel,
                        viewModel = dashboardViewModel,
                        dashboardItemLists = dashboardItemLists,
                        onButtonClick = { event, isPassword ->
                            onMenuItemClick(event, isPassword)
                        }
                    )
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            dashboardViewModel.startLogCapture(context)
        }
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
    onButtonClick: (String, Boolean) -> Unit
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
                .padding(MaterialTheme.dimens.DP_21_CompactMedium),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDemoMode = sharedViewModel.objPosConfig?.isDemoMode

            var visibility by remember { mutableStateOf(true) }

            // Use a Box to maintain space and center the text
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth() // Make the box fill the width
//                    //.padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
//                    .height(MaterialTheme.dimens.DP_33_CompactMedium) // Set a fixed height for the box
//                    .wrapContentHeight() // Ensure the height wraps content
//            ) {
////                TextView(
////                    text = if (visibility && isDemoMode == true) stringResource(id = R.string.training_mode) else "",
////                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
////                    color = MaterialTheme.colorScheme.primary,
////                    fontWeight = FontWeight.Bold,
////                    modifier = Modifier
////                        .align(Alignment.Center) // Center the text in the box
////                )
//            }

            // LaunchedEffect that toggles visibility
            LaunchedEffect(isDemoMode) {
                while (isDemoMode==true) {
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
                    rowConfigs.forEach { item ->
                        CardWithImageText(
                            text = item.text,
                            onClick = { onButtonClick(item.event, item.isPassword) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(MaterialTheme.dimens.DP_4_CompactMedium)
                        )
                    }

                    // 👇 Replace Spacer with CircularMenu
                    if (rowConfigs.size == 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(MaterialTheme.dimens.DP_4_CompactMedium),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularMenu(
                                menuOptions = listOf(
                                    context.resources.getString(R.string.cust_recp),
                                    context.resources.getString(R.string.merchant_recp)
                                ),
                                onMenuOptionClick = { option ->
                                    when (option) {
                                        context.resources.getString(R.string.cust_recp) -> {
                                            viewModel.reprintLast(context, true)
                                            isDialogVisible = true
                                        }
                                        context.resources.getString(R.string.merchant_recp) -> {
                                            viewModel.reprintLast(context)
                                            isDialogVisible = true
                                        }
                                    }
                                },
                                onPrintClick = {}
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom
            
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(end = 25.dp, bottom = 25.dp),
//                contentAlignment = Alignment.CenterEnd
//            ) {
//                CircularMenu(
//                    menuOptions = listOf(context.resources.getString((R.string.cust_recp)), context.resources.getString((R.string.merchant_recp))),
//                    onMenuOptionClick = { option ->
//                        when (option) {
//                            context.resources.getString((R.string.cust_recp)) -> {
//                                viewModel.reprintLast(context,true)
//                                isDialogVisible = true
//                            }
//                            context.resources.getString((R.string.merchant_recp)) -> {
//                                viewModel.reprintLast(context)
//                                isDialogVisible = true
//                            }
//                        }
//                    },
//                    onPrintClick = {}
//                )
//            }
            CustomDialogBuilder.ShowComposed()
        }
    }
}



