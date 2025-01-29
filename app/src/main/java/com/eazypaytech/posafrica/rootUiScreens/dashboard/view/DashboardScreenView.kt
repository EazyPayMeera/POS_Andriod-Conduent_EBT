package com.eazypaytech.posafrica.rootUiScreens.dashboard.view


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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dashboard.model.DashboardItemList
import com.eazypaytech.posafrica.rootUiScreens.dashboard.viewModel.DashboardViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.password.view.PasswordUtil
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.AppHeader
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CardWithImageText
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CircularMenu
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CustomDrawerContent
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.HideSoftKeyboard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.removeNonDigits
import com.eazypaytech.posafrica.ui.theme.dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DashboardView(navHostController: NavHostController) {
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
            AppConstants.BUTTON_CLICK_EVENT_PURCHASE -> {
                setTransactionType(TxnType.PURCHASE)
                navHostController.navigate(
                    if (sharedViewModel.objPosConfig?.isPromptInvoiceNo == true) AppNavigationItems.InvoiceScreen.route else AppNavigationItems.AmountScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_REFUND -> {
                setTransactionType(TxnType.REFUND)
                navHostController.navigate(
                    AppNavigationItems.InvoiceScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_PREAUTH -> {
                setTransactionType(TxnType.PREAUTH)
                navHostController.navigate(
                    if (sharedViewModel.objPosConfig?.isPromptInvoiceNo == true) AppNavigationItems.InvoiceScreen.route else AppNavigationItems.AmountScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_AUTH_CAPTURE -> {
                setTransactionType(TxnType.AUTHCAP)
                navHostController.navigate(
                    AppNavigationItems.InvoiceScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_VOID -> {
                setTransactionType(TxnType.VOID)
                navHostController.navigate(
                    AppNavigationItems.InvoiceScreen.route
                )
            }
            AppConstants.BUTTON_CLICK_EVENT_TRANSACTIONS -> {
                setTransactionType(TxnType.TXNLIST)
                navHostController.navigate(
                    AppNavigationItems.TxnListScreen.route
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
            titleId = R.string.purchase,
            iconId = R.drawable.purchase,
            event = AppConstants.BUTTON_CLICK_EVENT_PURCHASE,
            false
        ),
        createDashboardItem(
            titleId = R.string.refund,
            iconId = R.drawable.dashboard_refund,
            event = AppConstants.BUTTON_CLICK_EVENT_REFUND,
            true
        ),
        createDashboardItem(
            titleId = R.string.pre_auth,
            iconId = R.drawable.dashboard_preauth,
            event = AppConstants.BUTTON_CLICK_EVENT_PREAUTH,
            false
        ),
        createDashboardItem(
            titleId = R.string.auth_capture,
            iconId = R.drawable.dashboard_auth_capture,
            event = AppConstants.BUTTON_CLICK_EVENT_AUTH_CAPTURE,
            false
        ),
        createDashboardItem(
            titleId = R.string.void_trans,
            iconId = R.drawable.dashboard_void,
            event = AppConstants.BUTTON_CLICK_EVENT_VOID,
            true
        ),
        createDashboardItem(
            titleId = R.string.transactions,
            iconId = R.drawable.dashboard_transaction,
            event = AppConstants.BUTTON_CLICK_EVENT_TRANSACTIONS,
            true
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
                .padding(MaterialTheme.dimens.DP_15_CompactMedium),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDemoMode = sharedViewModel.objPosConfig?.isDemoMode

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
                            imageResId = item.iconResId,
                            onClick = { onButtonClick(item.event, item.isPassword) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(MaterialTheme.dimens.DP_4_CompactMedium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(/*horizontal = MaterialTheme.dimens.DP_24_CompactMedium,*/),
                contentAlignment = Alignment.Center
            ) {
                CircularMenu(
                    menuOptions = listOf(context.resources.getString((R.string.cust_recp)), context.resources.getString((R.string.merchant_recp))),
                    onMenuOptionClick = { option ->
                        when (option) {
                            context.resources.getString((R.string.cust_recp)) -> {
                                viewModel.reprintLast(context,true)
                                isDialogVisible = true
                            }
                            context.resources.getString((R.string.merchant_recp)) -> {
                                viewModel.reprintLast(context)
                                isDialogVisible = true
                            }
                        }
                    },
                    onPrintClick = {}
                )
            }
            CustomDialogBuilder.ShowComposed()
        }
    }
}



