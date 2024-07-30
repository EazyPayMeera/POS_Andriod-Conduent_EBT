package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState

@Composable
fun EnterEmailView(navHostController: NavHostController) {
    // Use 'remember' to store the state of the email input
    var email by remember { mutableStateOf("") }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid

    Column {
        CommonTopAppBar(
            title = if (isRefund) "Refund" else "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = "Enter Your Email-Id",
            label = "Email Address",
            placeholder = "Email",
            value = email, // Show the current email value
            onValueChange = { newValue ->
                email = newValue // Update the email state with the new input
            },
            onDoneAction = {
                // Navigate to the next screen, passing the entered email as an argument
                navHostController.navigate(AppNavigationItems.EmailScreen.createRoute(email))
            },
            isRefund = isRefund,
            isVoid = isVoid,
            keyboardType = KeyboardType.Text // Use the appropriate keyboard type for email input
        ) {
            // Additional content can be placed here if needed
        }
    }
}
