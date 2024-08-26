package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel.TaxPercentageViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun TaxPercentageView(navHostController: NavHostController) {
    // Get the ViewModel
    val taxPercentageViewModel: TaxPercentageViewModel = viewModel()

    // States from the ViewModel
    val rawInput by remember { mutableStateOf(taxPercentageViewModel.rawInput) }
    val taxpercentage by remember { mutableStateOf(taxPercentageViewModel.taxpercentage) }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.adjust),
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
                    text = stringResource(id = R.string.enter_the_percentage),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
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
                    value = rawInput,
                    onValueChange = { newValue ->
                        taxPercentageViewModel.onRawInputChange(newValue)
                    },
                    placeholder = stringResource(id = R.string.enter_the_percentage),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {
                        taxPercentageViewModel.onDoneAction(navHostController)
                    },
                    visualTransformation = createAmountTransformation(),
                    isPassword = true
                )

            }
        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { taxPercentageViewModel.onDoneAction(navHostController) }
        )
    }
}
