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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Image
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
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

        GenericCard(
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(14.dp)
            ) {
                TextView(
                    text = stringResource(id = R.string.enter_email),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Image(
                    imageId = R.drawable.card, size = 60.dp,
                    shape = RectangleShape, // Example shape, can be any Shape
                    alignment = Alignment.Center,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { newValue -> email = newValue },
                    placeholder = stringResource(id = R.string.email),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    keyboardType = KeyboardType.Text,
                    onDoneAction = {

                    },
                    isPassword = true
                ) // Set this to true for password fields)

            }

        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.confirm_btn),
            firstButtonOnClick = { navHostController.navigate(AppNavigationItems.AmountScreen.route) },
            secondButtonTitle = stringResource(id = R.string.cancel_btn),
            secondButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) }
        )
    }
}
