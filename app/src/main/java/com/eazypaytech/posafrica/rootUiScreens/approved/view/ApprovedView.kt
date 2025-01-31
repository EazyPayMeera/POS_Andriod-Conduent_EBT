package com.eazypaytech.posafrica.rootUiScreens.approved.view


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.approved.viewmodel.ApprovedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.BackgroundScreen
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CircularMenu
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OkButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getTxnStatusIconId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getTxnStatusStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toAmountFormat
import com.eazypaytech.posafrica.ui.theme.dimens


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovedView(navHostController: NavHostController) {
    Log.d("Approved Screen","Inside Approved Screen")
    val context = LocalContext.current
    val viewModel: ApprovedViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current
    var txnRecord = remember { sharedViewModel.objRootAppPaymentDetail }
    val hasDbRecord = viewModel.hasDbRecord.collectAsState().value

    Column {
        CommonTopAppBar(
            onBackButtonClick = { },
            showBackIcon = false
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

                TextView(
                    text = stringResource(id = getTxnStatusStringId(txnRecord.txnStatus)),
                    fontSize = MaterialTheme.dimens.SP_29_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))
                txnRecord.txnType.takeIf { it != TxnType.VOID }?.let {
                    Text(
                        text = txnRecord.ttlAmount.toAmountFormat(),
                        fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(MaterialTheme.dimens.DP_33_CompactMedium) // Fixed height
                    )
                } ?: Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_33_CompactMedium)) // Spacer when Text is not shown
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium))
                ImageView(
                    imageId = getTxnStatusIconId(txnRecord),
                    size = MaterialTheme.dimens.DP_120_CompactMedium,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_40_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_33_CompactMedium))

                if(hasDbRecord==true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularMenu(
                            menuOptions = listOf(
                                context.resources.getString((R.string.cust_recp)),
                                context.resources.getString((R.string.merchant_recp))
                            ),
                            onMenuOptionClick = { option ->
                                when (option) {
                                    context.resources.getString((R.string.cust_recp)) -> {
                                        viewModel.printReceipt(
                                            context,
                                            sharedViewModel,
                                            true
                                        )
                                    }

                                    context.resources.getString((R.string.merchant_recp)) -> {
                                        viewModel.printReceipt(
                                            context,
                                            sharedViewModel,
                                            false
                                        )
                                    }
                                }
                            },
                            onPrintClick = {}
                        )
                    }
                }
                else
                {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_70_CompactMedium))
                }

                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_50_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            viewModel.onDone(navHostController)
                        },
                        title = stringResource(id = R.string.done),
                    )
                }

                CustomDialogBuilder.ShowComposed()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(context,sharedViewModel)
    }
}








