// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.amount.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnInfo.txnType
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel.AmountViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun AmountView(navHostController: NavHostController, viewModel: AmountViewModel = hiltViewModel()){

    var sharedViewModel= localSharedViewModel.current
    var isDialogVisible by remember { mutableStateOf(false) }
    Column {

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
                    text = when(txnType){
                        TxnType.REFUND -> stringResource(R.string.refund_amt)
                        TxnType.PREAUTH -> stringResource(R.string.auth_amt)
                        TxnType.AUTHCAP -> stringResource(id = R.string.authcap_amt)
                        TxnType.VOID -> stringResource(id = R.string.void_pur)
                        else -> stringResource(R.string.purchase_amt)
                    },
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                // Image View
                ImageView(
                    imageId = if(TxnInfo.txnType==TxnType.VOID || TxnInfo.txnType==TxnType.REFUND) R.drawable.void_amt else R.drawable.card,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = "",
                )

                OutlinedTextField(
                    value = viewModel.transAmount,
                    onValueChange = {viewModel.onAmountChange(it)},
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = stringResource(id = R.string.auth_amt),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium,textAlign = TextAlign.End),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {viewModel.onConfirm(navHostController, sharedViewModel)},
                    visualTransformation = createAmountTransformation(),
                    amount = false,
                )

                if (TxnInfo.txnType==TxnType.VOID) {
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
                        stringResource(id = R.string.card) + " ************6983",
                        stringResource(id = R.string.auth_code) + " 896356",
                        stringResource(id = R.string.no) + " 100034345364633",
                        stringResource(id = R.string.inc_no) + " INVC1234",
                        stringResource(id = R.string.pos_entry) + " Contact"
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

                if (TxnInfo.txnType == TxnType.AUTHCAP || TxnInfo.txnType == TxnType.REFUND) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_15_CompactMedium))

                    listOf(
                        stringResource(id = R.string.original_amount) + "20.00",
                        stringResource(id = R.string.date)
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
            firstButtonOnClick = { /*viewModel.onCancel(navHostController)*/isDialogVisible=true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController, sharedViewModel) }
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle("Cancel ?")
                .setSubtitle("Are you sure want to Cancel ?")
                .setSmallText("")
                .setShowCloseButton(false) // Can set to false if you don't want the close button
                .setCancelButtonText("Confirm")
                .setConfirmButtonText("Cancel")
                .setCancelable(true)
                .setAutoOff(false)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
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
        viewModel.onLoad(sharedViewModel)
    }

}


