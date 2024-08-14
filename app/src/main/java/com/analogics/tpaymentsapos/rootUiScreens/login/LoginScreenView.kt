package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.ITransactionResultListener
import com.analogics.paymentservicecore.models.TransactionData
import com.analogics.paymentservicecore.models.TransactionStatus.APPROVED
import com.analogics.paymentservicecore.repository.gatewayPayment.UIPaymentInfoProviderRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.InputTextField
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun LoginScreenView(navHostController: NavHostController?) { // Nullable NavHostController
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.login)) },
                backgroundColor = Color(0xFFFFFFFF)
            )
        },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                var emailCredentials by remember { mutableStateOf("") }
                var pwdCredentials by remember { mutableStateOf("") }
                val context = LocalContext.current
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
                        .padding(top = MaterialTheme.dimens.DP_40_CompactMedium)
                ) {
                    // Image above the "Please Login to continue" text
                    Image(
                        painter = painterResource(id = R.drawable.card_img), // Replace with your image resource
                        contentDescription = null, // Decorative image
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_55_CompactMedium) // Set the size of the image
                            .padding(bottom = MaterialTheme.dimens.DP_160_CompactMedium) // Adds bottom padding to the image
                    )

                    Text(
                        text = stringResource(id = R.string.plz_login),
                        fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                        color = Color.LightGray,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        inputValue = emailCredentials,
                        onChange = { emailCredentials = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "",
                        placeHolder = stringResource(id = R.string.username),
                        icon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Text
                    )

                    InputTextField(
                        inputValue = pwdCredentials,
                        onChange = { pwdCredentials = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "",
                        placeHolder = stringResource(id = R.string.password),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Text,
                        isPasswordField = true
                    )

                    // "Forgot Password?" clickable text
                    Text(
                        text = stringResource(id = R.string.forget_pswd),
                        color = Color(0xFFFFA500),
                        fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_24_CompactMedium)
                            .clickable {
                                navHostController?.navigate(AppNavigationItems.ForgetPasswordScreen.route)
                                // Use safe navigation with ?. to avoid crashes if navHostController is null
                            }
                    )

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_50_CompactMedium)
                    ) {
                        AppButton(
                            onClick = {
                                navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
                                /*
                                var iTransactionResult =
                                    object : ITransactionResultListener {
                                        override fun onSuccess(transactionData: TransactionData) {
                                            when (transactionData.status) {
                                                APPROVED -> navHostController?.navigate(AppNavigationItems.ApprovedScreen.route)
                                                else -> navHostController?.navigate(AppNavigationItems.DeclineScreen.route)
                                            }
                                        }

                                        override fun onFailure(exception: Exception) {

                                        }

                                    }
                                UIPaymentInfoProviderRepository.initPayment(
                                    context,
                                    iTransactionResult
                                )
                                 */
                            },
                            title = stringResource(id = R.string.login)
                        )
                    }
                }
            }
        }
    )
}
