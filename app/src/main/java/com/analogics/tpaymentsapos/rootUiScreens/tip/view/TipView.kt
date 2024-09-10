package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel.TipViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun TipView(navHostController: NavHostController, viewModel: TipViewModel = hiltViewModel()) {

    /*viewModel.setTipAmount(updated_amt)*/

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
                    text = stringResource(id = R.string.enter_tip_amount),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                ImageView(
                    imageId = R.drawable.card, size = MaterialTheme.dimens.DP_60_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                )

                OutlinedTextField(
                    value = viewModel.tipamount,
                    onValueChange = {viewModel.onTipChange(it)},
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                    placeholder = stringResource(id = R.string.tip_amt),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {
                        viewModel.onConfirm(navHostController)
                    },
                    visualTransformation = createAmountTransformation()
                )

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
