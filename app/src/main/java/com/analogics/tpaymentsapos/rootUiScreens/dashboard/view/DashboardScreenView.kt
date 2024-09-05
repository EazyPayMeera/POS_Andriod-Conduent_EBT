package com.analogics.tpaymentsapos.rootUiScreens.dashboard.view


import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.model.DashboardItemList
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel.DashboardViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppHeader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CardWithImageText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomDrawerContent
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.launch


@Composable
fun DashboardView(navHostController: NavHostController) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    TrainingView(
        navHostController = navHostController,
        dashboardViewModel,
        dashboardItemLists = dashboardItemListData(navHostController, dashboardViewModel)
    ) {}
}

@Composable
fun dashboardItemListData(
    navHostController: NavHostController,
    dashboardViewModel: DashboardViewModel
): List<DashboardItemList> {

    // Helper function to set the transaction state
    fun setTransactionType(txnType: TxnType) {TxnInfo.txnType = txnType }

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
            onClickState = {  }
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

    Authorisation.isEreceipt = false
    Authorisation.isMerchantReceipt = false
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
                    backgroundColor = Color.White,
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
                        dashboardItemLists = dashboardItemLists,
                        selectedButton = selectedButton,
                        onButtonClick = { text, onClick ->
                            dashboardViewModel.onButtonClick(text, onClick)
                        }
                    )
                }
            }
        )
    }

    /* Initialize Payment SDK */
    LaunchedEffect(Unit) {
        dashboardViewModel.initPaymentSDK(context, this)
    }
}


@Composable
fun DashboardContentSurface(
    dashboardItemLists: List<DashboardItemList>,
    selectedButton: String?,
    onButtonClick: (String, () -> Unit) -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.DP_15_CompactMedium),
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
            TextView(
                text = stringResource(id = R.string.training),
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                    .align(Alignment.CenterHorizontally)
            )

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
                            onClick = { onButtonClick(config.text, config.onClick) },
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
                    .padding(top = MaterialTheme.dimens.DP_15_CompactMedium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    onClick = { /* Handle print receipt click */ },
                    title = stringResource(id = R.string.print_last_receipt),
                    image = painterResource(id = R.drawable.ic_print)
                )
            }
        }
    }
}
