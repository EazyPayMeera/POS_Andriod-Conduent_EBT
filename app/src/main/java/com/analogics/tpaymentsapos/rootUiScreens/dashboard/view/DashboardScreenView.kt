package com.analogics.tpaymentsapos.rootUiScreens.dashboard.view


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.rememberDrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.model.DashboardItemList
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel.DashboardViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppHeader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CardWithImageText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomDrawerContent
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule


@Composable
fun DashboardView(navHostController: NavHostController) {
    val dashboardViewModel: DashboardViewModel = viewModel()
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
    return listOf(
        DashboardItemList(
            stringResource(id = R.string.purchase),
            R.drawable.purchase,
            onClick = {
                dashboardViewModel.navigateTo(
                    navHostController,
                    AppNavigationItems.InvoiceScreen.route
                )
                TransactionState.isPurchase = true

            }),
        DashboardItemList(
            stringResource(id = R.string.refund),
            R.drawable.dashboard_refund,
            onClick = {
                dashboardViewModel.navigateTo(
                    navHostController,
                    AppNavigationItems.PasswordScreen.route
                )
                TransactionState.isRefund = true
            }),
        DashboardItemList(
            stringResource(id = R.string.pre_auth),
            R.drawable.dashboard_preauth,
            onClick = {
                dashboardViewModel.navigateTo(
                    navHostController,
                    AppNavigationItems.InvoiceScreen.route
                )
                TransactionState.isPreauth = true

            }),
        DashboardItemList(
            stringResource(id = R.string.auth_capture),
            R.drawable.dashboard_auth_capture,
            onClick = {
                dashboardViewModel.navigateTo(
                    navHostController,
                    AppNavigationItems.InvoiceScreen.route
                )
                TransactionState.isAuthcap = true
            }),
        DashboardItemList(
            stringResource(id = R.string.void_trans),
            R.drawable.dashboard_void,
            onClick = {
                dashboardViewModel.navigateTo(
                    navHostController,
                    AppNavigationItems.PasswordScreen.route
                )
                TransactionState.isVoid = true
            }),
        DashboardItemList(
            stringResource(id = R.string.transactions),
            R.drawable.dashboard_transaction,
            onClick = {
                dashboardViewModel.navigateTo(
                    navHostController,
                    AppNavigationItems.PasswordScreen.route
                )
                TransactionState.isTransaction = true

            })
    )
}

@Composable
fun TrainingView(
    navHostController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    dashboardItemLists: List<DashboardItemList>,
    onMenuItemClick: (String) -> Unit
) {
    val selectedButton = dashboardViewModel.selectedButton.value
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val hasSDKInit = remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Function to handle drawer open and close
    fun toggleDrawer(open: Boolean) {
        coroutineScope.launch {
            if (open) drawerState.open() else drawerState.close()
        }
    }

    fun initPaymentSDK(context : Context) {
        Timer("initSDK").schedule(500) {
            coroutineScope.launch {
                dashboardViewModel.initPaymentSDK(context, object : IResultProviderListener {
                    override fun onSuccess(result: Any?) {
                        if (result?.equals(true) == true)
                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        else
                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                    override fun onFailure(exception: Exception) {
                        Toast.makeText(context, R.string.emv_sdk_init_failure, Toast.LENGTH_SHORT)
                            .show()
                        Log.e("EMV_APP", exception.message.toString())
                    }
                })
            }
        }
        hasSDKInit.value = true
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
                    title = stringResource(id = R.string.training),
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
    if(!hasSDKInit.value)
        initPaymentSDK(context)
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
                fontSize = MaterialTheme.dimens.SP_20_CompactMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                    .align(Alignment.CenterHorizontally)
            )

            dashboardItemLists.chunked(2).forEach { rowConfigs ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium),
                    horizontalArrangement = Arrangement.SpaceEvenly,
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
                )
            }
        }
    }
}
