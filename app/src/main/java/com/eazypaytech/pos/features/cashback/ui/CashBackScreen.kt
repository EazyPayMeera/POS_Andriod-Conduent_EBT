// CashBackScreen.kt
package com.eazypaytech.pos.features.amount.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.cashback.ui.CashBackViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.FooterButtons
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.inputfields.OutlinedTextField
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.utils.createAmountTransformation
import com.eazypaytech.pos.core.themes.dimens


@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CashBackScreen(navHostController: NavHostController, viewModel: CashBackViewModel = hiltViewModel()){

    var sharedViewModel= localSharedViewModel.current
    var isDialogVisible by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.cashback_amt),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                ImageView(
                    imageId = if(sharedViewModel.objRootAppPaymentDetail.txnType==TxnType.VOID_LAST || sharedViewModel.objRootAppPaymentDetail.txnType==TxnType.FOODSTAMP_RETURN) R.drawable.void_amt else R.drawable.card,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = "",
                )

                OutlinedTextField(
                    value = viewModel.cashBackAmount,
                    onValueChange = {viewModel.onCashBackAmountChange(it)},
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = stringResource(id = R.string.auth_amt),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium,textAlign = TextAlign.End),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {viewModel.onConfirm(navHostController, sharedViewModel)},
                    visualTransformation = createAmountTransformation(),
                    amount = false
                )


            }
        }


        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { /*viewModel.onCancel(navHostController)*/isDialogVisible=true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController, sharedViewModel) },
            closeKeypadOnSecondButton = true
        )


    }


    CustomDialogBuilder.ShowComposed()

}


