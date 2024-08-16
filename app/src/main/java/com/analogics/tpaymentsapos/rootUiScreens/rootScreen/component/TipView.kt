package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ConfirmationButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Image
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SmallSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun TipView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var tipAmount by remember { mutableStateOf("0.00") }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.purchase),
            onBackButtonClick = { navHostController.popBackStack() }
        )
        SmallSurface(
            modifier = Modifier,
        ) {
            // Your custom content goes here
            TextField(
                text = stringResource(id = R.string.enter_Pin),
                fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Image(
                imageId = R.drawable.card,
                size = 70.dp,
                shape = CircleShape, // Example shape, can be any Shape
                alignment = Alignment.Center, // Example alignment
                modifier = Modifier.padding(bottom = 10.dp) // Add bottom padding here
            )

            OutlinedTextField(
                value = rawInput,
                onValueChange = { newValue ->
                    // Update rawInput and formattedAmount only if the new value is valid
                    if (newValue.all { char -> char.isDigit() || char == '.' }) {
                        rawInput = newValue
                        tipAmount = formatAmount(newValue)
                    }
                },
                placeholder = stringResource(id = R.string.enter_tip_amount),
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                keyboardType = KeyboardType.Number, // Use number keyboard for numeric inputs
                onDoneAction = {
                    // Handle the done action, e.g., hide the keyboard
                },
                visualTransformation = createAmountTransformation(), // Apply custom visual transformation
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth() // Adjust width as needed
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_24_CompactMedium), // Adjust padding as needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfirmationButton(
                onClick = { navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(tipAmount)) },
                title = stringResource(id = R.string.confirm_btn)
            )
            ConfirmationButton(
                onClick = { navHostController?.navigate(AppNavigationItems.TrainingScreen.route) },
                title = stringResource(id = R.string.cancel_btn)
            )
        }
        /*CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = stringResource(id = R.string.enter_tip_amount),
            label = "",
            placeholder = stringResource(id = R.string.enter_tip_amount),
            value = rawInput,
            onValueChange = { newValue ->
                if (newValue.all { char -> char.isDigit() }) {
                    rawInput = newValue
                    tipAmount = formatAmount(newValue)
                }
            },
            onDoneAction = {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(tipAmount))
            },
            keyboardType = KeyboardType.Number,
            visualTransformation = createAmountTransformation() // Use the imported function
        )*/
    }
}