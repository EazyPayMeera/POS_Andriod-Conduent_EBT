package com.analogics.tpaymentsapos.rootUiScreens.login

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
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.Symbol
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel.TaxPercentageViewModel
import com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel.updated_tax
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun TaxPercentageView(navHostController: NavHostController,viewModel: TaxPercentageViewModel = hiltViewModel()) {

    val updated_tax = updated_tax
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
                    text = stringResource(id = R.string.enter_the_percentage),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(top = MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                // Title Text
                TextView(
                    text = "SGST : " + updated_tax,
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                // Image View
                ImageView(
                    imageId = R.drawable.card,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = "",
                )

                OutlinedTextField(
                    value = viewModel.taxPercent,
                    onValueChange = {viewModel.onTaxChange(it)},
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = "",
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium,textAlign = TextAlign.Center),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {viewModel.onConfirm(navHostController)},
                    visualTransformation = createAmountTransformation(Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END)),
                    amount = false,
                )

            }
        }

        // Footer Buttons
        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { /*viewModel.onCancel(navHostController)*/isDialogVisible = true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController) }
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle("Are you sure want to Cancel ?")
                .setSubtitle("")
                .setSmallText("")
                .setShowCloseButton(true) // Can set to false if you don't want the close button
                .setCancelable(true)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    navHostController.navigate(AppNavigationItems.TaxPercentageScreen.route)
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
}
