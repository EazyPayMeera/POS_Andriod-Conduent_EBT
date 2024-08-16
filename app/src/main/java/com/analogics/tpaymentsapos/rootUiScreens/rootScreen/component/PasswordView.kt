

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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ConfirmationButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Image
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SmallSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun PasswordView(navHostController: NavHostController) {

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = Authorisation.isNewauth
    val isAuthcap = Authorisation.isAuthcap

    var invoiceno by remember { mutableStateOf("") }

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
                text = stringResource(id = R.string.enter_password),
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
                value = invoiceno,
                onValueChange = { newValue -> invoiceno = newValue },
                placeholder = stringResource(id = R.string.enter_Pin),
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                keyboardType = KeyboardType.Password,
                onDoneAction = {
                    // Define what happens when "Done" is pressed
                    // For example, you can move to the next field or submit the form
                },
                isPassword = true, // Set this to true for password fields
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
                onClick = { navHostController.navigate(AppNavigationItems.InvoiceScreen.route) },
                title = stringResource(id = R.string.confirm_btn)
            )
            ConfirmationButton(
                onClick = { navHostController?.navigate(AppNavigationItems.TrainingScreen.route) },
                title = stringResource(id = R.string.cancel_btn)
            )
        }
/*        CustomSurface(
            imageResourceId = R.drawable.card, // Ensure this resource ID exists
            titleText = stringResource(id = R.string.enter_password),
            label = "",
            placeholder = stringResource(id = R.string.password),
            value = invoiceno,
            onValueChange = { invoiceno = it },
            onDoneAction = { navHostController.navigate(AppNavigationItems.InvoiceScreen.route) },
            keyboardType = KeyboardType.Text // Default text keyboard
        )*/
    }
}

@Preview
@Composable

fun PasswordLayout()
{
    var invoiceno by remember { mutableStateOf("") }
    GenericCard (
        modifier = Modifier.padding(16.dp)
    ){
        Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally,modifier = Modifier.padding(16.dp)) {
            TextField(text = stringResource(id = R.string.enter_password), fontSize = MaterialTheme.dimens.SP_17_CompactMedium,            color = Color.Black,
                fontWeight = FontWeight.Bold,1,Modifier.padding(16.dp), textAlign = TextAlign.Center)
            Image(imageId = R.drawable.card,size = 60.dp,
                shape = RectangleShape, // Example shape, can be any Shape
                alignment = Alignment.Center,)

            OutlinedTextField(value = invoiceno,
                onValueChange = { newValue -> invoiceno = newValue },
                placeholder = stringResource(id = R.string.enter_Pin),
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                keyboardType = KeyboardType.Password,
                onDoneAction = {
                    // Define what happens when "Done" is pressed
                    // For example, you can move to the next field or submit the form
                },
                isPassword = true) // Set this to true for password fields)

        }

    }
}


