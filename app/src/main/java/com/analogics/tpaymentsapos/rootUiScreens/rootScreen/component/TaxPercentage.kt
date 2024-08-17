// AmountView.kt
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Image
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun TaxPercentageView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var taxpercentage by remember { mutableStateOf("0.00") }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.adjust),
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
                    text = stringResource(id = R.string.enter_the_percentage),
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
                    value = rawInput,
                    onValueChange = { newValue ->
                        // Update rawInput and formattedAmount only if the new value is valid
                        if (newValue.all { char -> char.isDigit() || char == '.' }) {
                            rawInput = newValue
                            taxpercentage = formatAmount(newValue)
                        }
                    },
                    placeholder = stringResource(id = R.string.enter_the_percentage),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {

                    },
                    visualTransformation = createAmountTransformation(),
                    isPassword = true
                ) // Set this to true for password fields)

            }

        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.confirm_btn),
            firstButtonOnClick = { navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(taxpercentage)) },
            secondButtonTitle = stringResource(id = R.string.cancel_btn),
            secondButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) }
        )
    }
}
