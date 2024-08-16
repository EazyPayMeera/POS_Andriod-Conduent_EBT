package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ConfirmationButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Image
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SmallSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun EnterEmailView(navHostController: NavHostController) {
    // Use 'remember' to store the state of the email input
    var email by remember { mutableStateOf("") }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    Column {
        CommonTopAppBar(
            title = when {
                isRefund -> stringResource(R.string.refund)
                isVoid -> stringResource(R.string.void_trans)
                isPreauth -> stringResource(R.string.pre_auth)
                else -> stringResource(R.string.purchase)
            },
            onBackButtonClick = { navHostController.popBackStack() }
        )

        SmallSurface(
            modifier = Modifier,
            isRefund = isRefund,
            isVoid = isVoid,
            isAuthcap = isAuthcap,
        ) {
            // Your custom content goes here
            TextField(
                text = stringResource(id = R.string.enter_email),
                fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Image(
                imageId = R.drawable.card,
                size = 60.dp,
                shape = RectangleShape, // Example shape, can be any Shape
                alignment = Alignment.Center, // Example alignment
                modifier = Modifier.padding(bottom = 5.dp) // Add bottom padding here
            )

            // Call the CustomOutlinedTextField
            OutlinedTextField(
                value = email,
                onValueChange = { newValue -> email = newValue },
                placeholder = stringResource(id = R.string.invoice_no),
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                keyboardType = KeyboardType.Text,
                onDoneAction = {
                    // Define what happens when "Done" is pressed
                    // For example, you can move to the next field or submit the form
                },
                isPassword = false, // Set this to true for password fields
                modifier = Modifier.padding(16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_24_CompactMedium), // Adjust padding as needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfirmationButton(
                onClick = { navHostController.navigate(AppNavigationItems.EmailScreen.createRoute(email)) },
                title = stringResource(id = R.string.confirm_btn)
            )
            ConfirmationButton(
                onClick = { navHostController?.navigate(AppNavigationItems.TrainingScreen.route) },
                title = stringResource(id = R.string.cancel_btn)
            )
        }
        /*CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = stringResource(id = R.string.enter_email),
            label = "",
            placeholder = stringResource(id = R.string.email),
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
        }*/
    }
}
