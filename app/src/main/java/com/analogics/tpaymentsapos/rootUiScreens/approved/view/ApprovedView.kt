package com.analogics.tpaymentsapos.rootUiScreens.login


import android.os.Build
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel.ApprovedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CircularMenu
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toAmountFormat
import com.analogics.tpaymentsapos.ui.theme.dimens


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovedView(navHostController: NavHostController) {
    val context = LocalContext.current
    //val viewModel: ApprovedViewModel = viewModel { ApprovedViewModel(context) }
    val viewModel: ApprovedViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope

    val sharedViewModel = localSharedViewModel.current

    //viewModel.updateTxnData(sharedViewModel.objRootAppPaymentDetail)

    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
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
                    text = sharedViewModel.objRootAppPaymentDetail.txnStatus.toString(),
                    fontSize = MaterialTheme.dimens.SP_29_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))
                sharedViewModel.objRootAppPaymentDetail.txnType.takeIf { it != TxnType.VOID }?.let {
                    Text(
                        text = sharedViewModel.objRootAppPaymentDetail.ttlAmount.toAmountFormat(),
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
                    imageId = if(sharedViewModel.objRootAppPaymentDetail.txnStatus == TxnStatus.APPROVED) R.drawable.approve else R.drawable.decline,
                    size = MaterialTheme.dimens.DP_126_CompactMedium,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_40_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_33_CompactMedium))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                    contentAlignment = Alignment.Center
                ) {
                    CircularMenu(
                        onMenuOptionClick = { option ->
                            when (option) {
                                context.resources.getString((R.string.cust_recp)) -> {
                                    viewModel.printReceipt(R.drawable.master_mono,sharedViewModel,context, true,sharedViewModel.objRootAppPaymentDetail)
                                }
                                context.resources.getString((R.string.merchant_recp)) -> {
                                    viewModel.printReceipt(
                                        R.drawable.master_mono,
                                        sharedViewModel,
                                        context,
                                        objRootAppPaymentDetail = sharedViewModel.objRootAppPaymentDetail
                                    )
                                }

                            }
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_50_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                        },
                        title = stringResource(id = R.string.done),
                    )
                }

                CustomDialogBuilder.ShowComposed()
            }
        }
    }
}








