package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount

@Composable
fun TipView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var tipAmount by remember { mutableStateOf("0.00") }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.purchase),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = stringResource(id = R.string.enter_tip_amount),
            label = "",
            placeholder = stringResource(id = R.string.enter_tip_amount),
            value = rawInput,
            onValueChange = { newValue ->
                if (newValue.all { char -> char.isDigit() }) {
                    rawInput = newValue
                    tipAmount = formatAmount(newValue)
                }
            },
            onDoneAction = {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(tipAmount))
            },
            keyboardType = KeyboardType.Number,
            visualTransformation = createAmountTransformation() // Use the imported function
        )
    }
}