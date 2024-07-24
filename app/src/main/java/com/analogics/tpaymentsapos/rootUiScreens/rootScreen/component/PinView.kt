package com.analogics.tpaymentsapos.rootUiScreens.login


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface

@Composable
fun PinView(navHostController: NavHostController) {
    var invoiceno by remember { mutableStateOf("") }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.purchase), // Fetch title from strings.xml
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card, // Ensure this resource ID exists
            titleText = stringResource(id = R.string.enter_Pin), // Fetch title from strings.xml
            label = stringResource(id = R.string.pin), // Fetch title from strings.xml
            placeholder = stringResource(id = R.string.pin), // Fetch title from strings.xml
            value = invoiceno,
            onValueChange = {
                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                    invoiceno = it
                }
            },
            onDoneAction = { navHostController.navigate(AppNavigationItems.ApprovedScreen.route) },
            isPassword = true, // Use password input for PinView
            keyboardType = KeyboardType.Number // Numeric keyboard
        )
    }
}




