// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel.AmountViewModel
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

    Column {

        CommonTopAppBar(
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
                    text = if (TxnInfo.txnType==TxnType.REFUND) stringResource(id = R.string.refund_amt) else if(TxnInfo.txnType==TxnType.PREAUTH) stringResource(
                        id = R.string.auth_amt
                    ) else stringResource(
                        id = R.string.purchase_amt
                    ),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                ImageView(
                    imageId = if(TxnInfo.txnType==TxnType.VOID || TxnInfo.txnType==TxnType.REFUND) R.drawable.void_amt else R.drawable.card,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                )

                OutlinedTextField(
                    value = viewModel.rawInput,
                    onValueChange = {viewModel.onAmountChange(it)},
                    placeholder = stringResource(id = R.string.auth_amt),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium,textAlign = TextAlign.End),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {viewModel.onConfirm(navHostController)},
                    visualTransformation = createAmountTransformation(),
                    amount = true
                )

                if (TxnInfo.txnType==TxnType.VOID) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    TextView(
                        text = viewModel.transactionDateTime,
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )

                    TextView(
                        text = stringResource(id = R.string.card),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )

                    TextView(
                        text = stringResource(id = R.string.auth_code),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )

                    TextView(
                        text = stringResource(id = R.string.no),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )

                    TextView(
                        text = stringResource(id = R.string.inc_no),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )
                    TextView(
                        text = stringResource(id = R.string.pos_entry),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                            .align(Alignment.Start)
                    )
                }
                if(TxnInfo.txnType==TxnType.AUTHCAP)
                {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_15_CompactMedium))
                    TextView(

                        text = stringResource(id = R.string.original_amount).format(viewModel.transactionDateTime),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )
                    TextView(
                        text = stringResource(id = R.string.date).format(viewModel.transactionDateTime),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
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

