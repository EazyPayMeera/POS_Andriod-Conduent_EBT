package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PreauthTypeSelectionSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState

@Composable
fun PreauthView(navHostController: NavHostController) {
    val isRefund = TransactionState.isRefund

    Column {
        CommonTopAppBar(
            title = if (isRefund) "Refund" else "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Call the new composable function
        PreauthTypeSelectionSurface(
            title = "Enter the Pre-Auth Type",
            imageResourceId = R.drawable.card,
            firstButtonText = "New-Authorisation",
            secondButtonText = "Auth-Capture",
            onFirstButtonClick = {
                navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
            },
            onSecondButtonClick = {
                // Handle Auth-Capture click
            }
        )
    }
}
