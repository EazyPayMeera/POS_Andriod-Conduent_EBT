// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel.AmountViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState.isAuthcap
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState.isPreauth
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState.isRefund
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState.isVoid
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun AmountView(navHostController: NavHostController, viewModel: AmountViewModel = viewModel()){

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
            modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                TextView(
                    text = stringResource(id = R.string.auth_amt),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                ImageView(
                    imageId = if(isVoid || isRefund) R.drawable.void_amt else R.drawable.card,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                )

                OutlinedTextField(
                    value = viewModel.rawInput,
                    onValueChange = {viewModel.onAmountChange(it)},
                    placeholder = stringResource(id = R.string.auth_amt),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {viewModel.onConfirm(navHostController)},
                    visualTransformation = createAmountTransformation()
                )

                if (isVoid) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    TextView(
                        text = viewModel.transactionDateTime,
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )

                    TextView(
                        text = "Card:",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )

                    TextView(
                        text = "Auth Code:",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )

                    TextView(
                        text = "No.:",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )

                    TextView(
                        text = "Invoice Number:",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )
                    TextView(
                        text = "POS Entry:",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )
                }
                if(isAuthcap)
                {
                    Spacer(modifier = Modifier.height(10.dp))

                    TextView(
                        text = "Original Amount: ${viewModel.transactionDateTime}",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    TextView(
                        text = "Txn Date: ${viewModel.transactionDateTime}",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { viewModel.onCancel(navHostController) },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController) }
        )
    }
}

