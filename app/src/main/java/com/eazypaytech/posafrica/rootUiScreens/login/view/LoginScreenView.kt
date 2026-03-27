package com.eazypaytech.posafrica.rootUiScreens.login.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.BuildConfig
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.login.viewModel.LoginViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.AppHeader
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.InputTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.LoginButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.ui.theme.dimens

@Composable
fun LoginScreenView(navHostController: NavHostController?, viewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.login),
                onBackButtonClick = { /* Handle back button click if needed */ },
                isIcon1Visible = false,
                //backgroundColor = MaterialTheme.colorScheme.onPrimary,
                isIcon2Visible = false
            )
        },
        content = { padding ->
            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
                        .padding(top = MaterialTheme.dimens.DP_40_CompactMedium)
                ) {
                    ImageView(
                        imageId =  R.drawable.unlock, // Decorative image
                        size = MaterialTheme.dimens.DP_40_CompactMedium,
                        contentDescription = ""
                    )

                    TextView(
                        text = stringResource(id = R.string.plz_login),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        enabled = viewModel.isLoginEnabled.value,
                        inputValue = viewModel.emailCredentials.value,
                        onChange = { viewModel.onEmailChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER || sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.CASH_WITHDRAWAL) stringResource(id = R.string.supervisor_id) else stringResource(id = R.string.username),
                        placeHolder = if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER || sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.CASH_WITHDRAWAL) stringResource(id = R.string.supervisor_username) else stringResource(id = R.string.placehldr_username),
                        icon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Uri
                    )

                    InputTextField(
                        enabled = viewModel.isLoginEnabled.value,
                        inputValue = viewModel.pwdCredentials.value,
                        onChange = { viewModel.onPasswordChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.password),
                        placeHolder = stringResource(id = R.string.placehldr_password),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number,
                        isPasswordField = true,
                        keyboardActions = KeyboardActions.Default.onDone,
                        onActionDone = {
                                viewModel.onLoginClick(navHostController,sharedViewModel)
                        }
                    )

                    // "Forgot Password?" clickable text
                    TextView(
                        text = stringResource(id = R.string.forget_pswd),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_11_CompactMedium)
                            .clickable {
                                navHostController?.navigate(AppNavigationItems.ForgetPasswordScreen.route)
                            }
                    )

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_11_CompactMedium)
                    ) {
                        val message = stringResource(id = R.string.cred_not_to_be_empty)
                        LoginButton(
                            onClick = {
                                if (viewModel.isFormValid) {
                                        viewModel.onLoginClick(navHostController,sharedViewModel)
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            title = stringResource(id = R.string.login),
                            enabled = viewModel.isLoginEnabled.value
                        )

                        Text(
                            text = stringResource(id = R.string.login_version) + BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(MaterialTheme.dimens.DP_11_CompactMedium)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    ) {

                    }
                }
            }

            CustomDialogBuilder.ShowComposed()
        }
    )
}
