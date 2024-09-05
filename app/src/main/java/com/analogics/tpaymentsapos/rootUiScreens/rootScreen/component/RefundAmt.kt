// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount


@Composable
fun RefundAmtView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var formattedAmount by remember { mutableStateOf("0.00") }

    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = stringResource(id = R.string.refund_amt),
            label = "",
            placeholder = stringResource(id = R.string.refund_amt),
            value = rawInput,
            onValueChange = { newValue ->
                if (newValue.all { char -> char.isDigit() }) {
                    rawInput = newValue
                    formattedAmount = formatAmount(newValue)
                }
            },
            onDoneAction = {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(formattedAmount))
            },
            keyboardType = KeyboardType.Number,
            //visualTransformation = createAmountTransformation() // Use the imported function

        )
    }
}
