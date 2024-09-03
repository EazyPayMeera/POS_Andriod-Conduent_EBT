package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ScannerButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun InvoiceView(navHostController: NavHostController) {
    // Get ViewModel instance
    val viewModel: InvoiceViewModel = hiltViewModel()

    // Collect the state from ViewModel
    val invoiceno by viewModel.invoiceno.collectAsState()

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = TransactionState.isAuthcap


    Column {
        CommonTopAppBar(
            title = when {
                isRefund -> stringResource(R.string.refund)
                isVoid -> stringResource(R.string.void_trans)
                isPreauth -> stringResource(R.string.pre_auth)
                else -> stringResource(R.string.purchase)
            },
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {

                TextView(
                    text = stringResource(id = R.string.enter_invoice),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                ImageView(
                    imageId = R.drawable.invoice, size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape, // Example shape, can be any Shape
                    alignment = Alignment.Center,
                )

                OutlinedTextField(
                    value = invoiceno,
                    onValueChange = { newValue -> viewModel.updateInvoiceNo(newValue) },
                    placeholder = stringResource(id = R.string.invoice_no),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium),
                    keyboardType = KeyboardType.Uri,
                    onDoneAction = {

                        viewModel.navigateToAmountScreen(navHostController)
                    },
                    isPassword = false
                )

                if(isRefund || isVoid || isAuthcap)
                {
                    TextView(
                        text = stringResource(id = R.string.or),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        1,
                        Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                        textAlign = TextAlign.Center
                    )

                    ScannerButton(
                        text = stringResource(id = R.string.scan_qr),
                        onClick = {
                            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)},
                        backgroundColor = Color(0xFFEDEDED),
                        contentColor = Color.Black,
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
                    )
                }

            }

        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { viewModel.navigateToTrainingScreen(navHostController) },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.navigateToAmountScreen(navHostController) }
        )
    }
}
