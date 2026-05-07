// CashBackScreen.kt
package com.eazypaytech.pos.features.amount.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.FooterButtons
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.inputfields.OutlinedTextField
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.utils.createAmountTransformation
import com.eazypaytech.pos.core.ui.components.inputfields.getTransTypeAmountTitle
import com.eazypaytech.pos.core.themes.dimens


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AmountScreen(navHostController: NavHostController, viewModel: AmountViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current
    var isDialogVisible by remember { mutableStateOf(false) }

    // Collect StateFlows as Compose state so the UI recomposes when values change
    val origTotalAmount by viewModel.origTotalAmount.collectAsState()
    val origDateTime by viewModel.origDateTime.collectAsState()
    val rrn by viewModel.rrn.collectAsState()
    val stan by viewModel.stan.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Top App Bar
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Main Content
        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                // Title Text
                TextView(
                    text = if (sharedViewModel.objPosConfig?.isCashback == true)
                        stringResource(R.string.cashback_amt)
                    else if (sharedViewModel.objRootAppPaymentDetail.isPurchase == true)
                        stringResource(R.string.ebt_purchase_amount)
                    else if (sharedViewModel.objRootAppPaymentDetail.isReturn == true)
                        stringResource(R.string.ebt_return_amount)
                    else
                        getTransTypeAmountTitle(),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                // Image View
                ImageView(
                    imageId = if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.VOID_LAST
                        || sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.FOODSTAMP_RETURN)
                        R.drawable.void_amt
                    else
                        R.drawable.card,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = "",
                )
                // Amount input field
                OutlinedTextField(
                    value = viewModel.transAmount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = stringResource(id = R.string.auth_amt),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.dimens.SP_28_CompactMedium,
                        textAlign = TextAlign.End
                    ),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = { viewModel.onConfirm(navHostController, sharedViewModel) },
                    visualTransformation = createAmountTransformation(),
                    amount = false,
                    readOnly = viewModel.isReadOnly
                )

                if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.VOID_LAST) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    TextView(
                        text = "",
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                            .align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    listOf(
                        stringResource(id = R.string.card) + " " + (sharedViewModel.objRootAppPaymentDetail.cardBrand?.plus(" ") ?: "EBT"),
                        stringResource(id = R.string.pos_entry) + " " + (sharedViewModel.objRootAppPaymentDetail.cardEntryMode ?: "-"),
                        stringResource(id = R.string.receipt_rrn) + " " + (sharedViewModel.objRootAppPaymentDetail.rrn ?: "-"),
                        stringResource(id = R.string.stan) + " " + (sharedViewModel.objRootAppPaymentDetail.stan?.padStart(6,'0') ?: "-"),
                        stringResource(id = R.string.approval_code) + " " + (sharedViewModel.objRootAppPaymentDetail.hostAuthCode?.padStart(6,'0') ?: "-")
                    ).forEach {
                        TextView(
                            text = it,
                            fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                                .align(Alignment.Start)
                        )
                    }
                }

                if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.VOID_LAST) {
//                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_15_CompactMedium))

                    // Use collected state values — UI will recompose when these update after DB fetch
                    listOf(
                        stringResource(id = R.string.original_amount) + " " + (origTotalAmount ?: ""),
                        stringResource(id = R.string.date) + " " + (origDateTime ?: "")
                    ).forEach {
                        TextView(
                            text = it,
                            fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                                .align(Alignment.Start)
                        )
                    }
                }
            }
        }

        // Footer Buttons
        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { isDialogVisible = true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController, sharedViewModel) },
            closeKeypadOnSecondButton = true
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle(stringResource(id = R.string.cancel_dialogue))
                .setSubtitle(stringResource(id = R.string.dialogue_cancel_request))
                .setSmallText("")
                .setShowCloseButton(false)
                .setCancelButtonText(stringResource(id = R.string.cancel_no))
                .setConfirmButtonText(stringResource(id = R.string.yes))
                .setCancelable(true)
                .setAutoOff(false)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary)
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    navHostController.navigate(AppNavigationItems.AmountScreen.route)
                }
                .setOnConfirmAction {
                    navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
                }
                .setShowButtons(true)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })
        }
    }

    LaunchedEffect(Unit) {
//        if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.VOID_LAST) {
//            viewModel.fetchLastTransaction(navHostController, context, sharedViewModel)
//        }
        viewModel.onLoad(sharedViewModel)
    }

    CustomDialogBuilder.ShowComposed()
}
