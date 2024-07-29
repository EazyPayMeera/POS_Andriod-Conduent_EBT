package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface

@Composable
fun InvoiceView(navHostController: NavHostController) {
    var invoiceno by remember { mutableStateOf("") }
    val backStackEntry = navHostController.currentBackStackEntryAsState().value
    val isRefund = backStackEntry?.arguments?.getBoolean("isRefund") ?: false

    Column {
        CommonTopAppBar(
            title = if (isRefund) "Refund" else "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card, // Ensure this resource ID exists
            titleText = "Enter the Invoice Number",
            label = "Invoice Number",
            placeholder = "Invoice Number",
            value = invoiceno,
            onValueChange = { invoiceno = it },
            onDoneAction = { navHostController.navigate(AppNavigationItems.AmountScreen.route) },
            keyboardType = KeyboardType.Text // Default text keyboard
        )
    }
}




