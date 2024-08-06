// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.login


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
            title = "Adjust",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = "Enter the Percentage",
            label = "Percentage",
            placeholder = "Enter Percentage",
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
            visualTransformation = createAmountTransformation() // Use the imported function
        )
    }
}
