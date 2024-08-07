package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PreauthTypeSelectionSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState

@Composable
fun PreauthView(navHostController: NavHostController) {
    val isRefund = TransactionState.isRefund
    val isNewauth = Authorisation.isNewauth
    val isAuthcap = Authorisation.isAuthcap

    Column {
        CommonTopAppBar(
            title = "Pre-Auth",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Call the new composable function
        PreauthTypeSelectionSurface(
            title = "Enter the Pre-Auth Type",
            imageResourceId = R.drawable.card,
            firstButtonText = "New-Authorisation",
            secondButtonText = "Auth-Capture",
            onFirstButtonClick = {
                Authorisation.isNewauth = true
                navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
            },
            onSecondButtonClick = {
                Authorisation.isAuthcap = true
                navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
            }
        )
    }
}
