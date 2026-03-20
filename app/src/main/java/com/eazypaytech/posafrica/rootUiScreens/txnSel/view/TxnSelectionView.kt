package com.eazypaytech.posafrica.rootUiScreens.txnSel.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.approved.viewmodel.ApprovedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.BackgroundScreen
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OkButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.removeNonDigits
import com.eazypaytech.posafrica.ui.theme.dimens


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TxnSelectionView(navHostController: NavHostController) {

    val context = LocalContext.current
    val viewModel: ApprovedViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    fun setTransactionType(txnType: TxnType) {
        sharedViewModel.objRootAppPaymentDetail.id = removeNonDigits(getCurrentDateTime(AppConstants.UNIQUE_ID_DATE_TIME_FORMAT)).toLong()
        sharedViewModel.objRootAppPaymentDetail.txnType = txnType
        Log.d("TRANSACTION_TYPE", "Txn Type Selected: ${sharedViewModel.objRootAppPaymentDetail.txnType}")
    }
    Column {
        CommonTopAppBar(
            title = "EBT Purchase",
            onBackButtonClick = { navHostController.popBackStack()},
            showBackIcon = true
        )

        // Outer Surface with background color, padding, and rounded corners
        BackgroundScreen(
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the content inside the inner Surface
                    .fillMaxSize(), // Fill the entire available space
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_24_CompactMedium)) // Blank space
                if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER)
                {
                    Box(
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_160_CompactMedium)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        OkButton(
                            onClick = {
                                navHostController.navigate(AppNavigationItems.AmountScreen.route)
                            },
                            title = stringResource(id = R.string.summary_purchase),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_15_CompactMedium)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        OkButton(
                            onClick = {
                                navHostController.navigate(AppNavigationItems.AmountScreen.route)
                            },
                            title = stringResource(id = R.string.sel_return),
                        )
                    }

                }
                else {
                    Box(
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_180_CompactMedium)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        OkButton(
                            onClick = {
                                setTransactionType(TxnType.FOOD_PURCHASE)
                                navHostController.navigate(AppNavigationItems.AmountScreen.route)
                            },
                            title = stringResource(id = R.string.food),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_21_CompactMedium)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        OkButton(
                            onClick = {
                                setTransactionType(TxnType.CASH_PURCHASE)
                                navHostController.navigate(AppNavigationItems.AmountScreen.route)
                            },
                            title = stringResource(id = R.string.cash),
                        )
                    }
                }

                CustomDialogBuilder.ShowComposed()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(context,sharedViewModel)
    }
}

