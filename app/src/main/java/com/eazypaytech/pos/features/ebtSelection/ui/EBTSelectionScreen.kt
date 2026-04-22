package com.eazypaytech.pos.features.ebtSelection.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.approved.ui.ApprovedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.BackgroundScreen
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.OkButton
import com.eazypaytech.pos.core.utils.getCurrentDateTime
import com.eazypaytech.pos.core.utils.removeNonDigits
import com.eazypaytech.pos.core.themes.dimens


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EBTSelectionView(navHostController: NavHostController) {

    val context = LocalContext.current
    val viewModel: ApprovedViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    fun setTransactionType(txnType: TxnType) {
        sharedViewModel.objRootAppPaymentDetail.id = removeNonDigits(getCurrentDateTime(AppConstants.UNIQUE_ID_DATE_TIME_FORMAT)).toLong()
        sharedViewModel.objRootAppPaymentDetail.txnType = txnType
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack()},
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
                            .padding(top = MaterialTheme.dimens.DP_180_CompactMedium)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        OkButton(
                            onClick = {
                                navHostController.navigate(AppNavigationItems.ManualCardScreen.route){
                                    popUpTo(AppNavigationItems.EBTSelScreen.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            title = stringResource(id = R.string.food_stamp),
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
                                navHostController.navigate(AppNavigationItems.CardScreen.route){
                                    popUpTo(AppNavigationItems.EBTSelScreen.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                                setTransactionType(TxnType.BALANCE_ENQUIRY_SNAP)
                            },
                            title = stringResource(id = R.string.bal_snap),
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
                                setTransactionType(TxnType.BALANCE_ENQUIRY_CASH)
                                navHostController.navigate(AppNavigationItems.CardScreen.route)
                            },
                            title = stringResource(id = R.string.bal_cash),
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

