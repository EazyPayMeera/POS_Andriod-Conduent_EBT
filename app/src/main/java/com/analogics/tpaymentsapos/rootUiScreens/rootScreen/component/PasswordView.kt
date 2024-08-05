

package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState

@Composable
fun PasswordView(navHostController: NavHostController) {

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = Authorisation.isNewauth
    val isAuthcap = Authorisation.isAuthcap

    var invoiceno by remember { mutableStateOf("") }

    Column {
        CommonTopAppBar(
            title = if (isRefund) "Refund" else if (isVoid) "Void" else if (isPreauth) "Pre-Auth" else "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card, // Ensure this resource ID exists
            titleText = "Enter Your Password",
            label = "Password",
            placeholder = "Password",
            value = invoiceno,
            onValueChange = { invoiceno = it },
            onDoneAction = { navHostController.navigate(AppNavigationItems.InvoiceScreen.route) },
            keyboardType = KeyboardType.Text // Default text keyboard
        )
    }
}




