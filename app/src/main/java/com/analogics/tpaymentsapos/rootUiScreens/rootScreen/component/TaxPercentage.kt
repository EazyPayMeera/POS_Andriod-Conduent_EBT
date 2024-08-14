// AmountView.kt
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
fun TaxPercentageView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var taxpercentage by remember { mutableStateOf("0.00") }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.adjust),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = stringResource(id = R.string.enter_the_percentage),
            label = "",
            placeholder = stringResource(id = R.string.enter_the_percentage),
            value = rawInput,
            onValueChange = { newValue ->
                if (newValue.all { char -> char.isDigit() }) {
                    rawInput = newValue
                    taxpercentage = formatAmount(newValue)
                }
            },
            onDoneAction = {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(taxpercentage))
            },
            keyboardType = KeyboardType.Number,
            //visualTransformation = createAmountTransformation() // Use the imported function
        )
    }
}
