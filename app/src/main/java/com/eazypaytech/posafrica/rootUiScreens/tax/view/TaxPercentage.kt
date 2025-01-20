package com.eazypaytech.posafrica.rootUiScreens.tax.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.Symbol
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.tax.viewmodel.TaxPercentageViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OutlinedTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.createAmountTransformation
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toPercentFormat
import com.eazypaytech.posafrica.ui.theme.dimens

@Composable
fun TaxPercentageView(navHostController: NavHostController,viewModel: TaxPercentageViewModel = hiltViewModel()) {

    var isDialogVisible by remember { mutableStateOf(false) }
    var sharedViewModel = localSharedViewModel.current

    @Composable
    fun getPrompt(): String {
        return when (viewModel.taxType) {
            TaxPercentageViewModel.TaxType.VAT -> stringResource(id = R.string.tax_percent_change_prompt_vat)
        }
    }

    @Composable
    fun getCurrentTaxValue(): String {
        return stringResource(id = R.string.tax_current_value) + " : " + when (viewModel.taxType) {
            TaxPercentageViewModel.TaxType.VAT -> sharedViewModel.objPosConfig?.vatPercent.toPercentFormat()
        }
    }

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
                    text = getPrompt(),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(top = MaterialTheme.dimens.DP_10_CompactMedium),
                    textAlign = TextAlign.Center
                )

                // Title Text
                TextView(
                    text = getCurrentTaxValue(),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium),
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
                    onDoneAction = {viewModel.onConfirm(navHostController, sharedViewModel)},
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
            secondButtonOnClick = { viewModel.onConfirm(navHostController, sharedViewModel) }
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle(stringResource(id = R.string.cancel_dialogue))
                .setSubtitle(stringResource(id = R.string.dialogue_cancel_request))
                .setSmallText("")
                .setShowCloseButton(false) // Can set to false if you don't want the close button
                .setCancelButtonText(stringResource(id = R.string.yes))
                .setConfirmButtonText(stringResource(id = R.string.cancel_no))
                .setCancelable(true)
                .setAutoOff(false)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
                }
                .setOnConfirmAction {
                    navHostController.navigate(AppNavigationItems.TaxPercentageScreen.route)
                }
                .setShowButtons(true)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })

        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(navHostController, sharedViewModel)
    }
}
