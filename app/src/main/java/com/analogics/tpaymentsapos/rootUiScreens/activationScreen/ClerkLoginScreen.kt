package com.analogics.tpaymentsapos.rootUiScreens.activationScreen

import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activationScreen.viewModel.ClerkLoginViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppHeader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.InputTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun ClerkLoginScreen(navHostController: NavHostController, viewModel: ClerkLoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.login),
                onBackButtonClick = { /* Handle back button click if needed */ },
                isIcon1Visible = false,
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
                        text = stringResource(id = R.string.please_register_clerk),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        enabled = viewModel.isLoginEnabled.value,
                        inputValue = viewModel.userCredentials.value,
                        onChange = { viewModel.onEmailChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.username),
                        placeHolder = stringResource(id = R.string.placehldr_username),
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
                    InputTextField(
                        enabled = viewModel.isLoginEnabled.value,
                        inputValue = viewModel.cnfPwdCredentials.value,
                        onChange = { viewModel.onCnfPasswordChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.confirm_password),
                        placeHolder = stringResource(id = R.string.placehldr_Confirm_password),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number,
                        isPasswordField = true,
                        keyboardActions = KeyboardActions.Default.onDone,
                        onActionDone = {
                            viewModel.onLoginClick(navHostController,sharedViewModel)
                        }
                    )

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_17_CompactMedium)
                    ) {
                        val message = stringResource(id = R.string.cred_not_to_be_empty)
                        AppButton(
                            onClick = {
                                if (viewModel.isFormValid) {
                                    val updatedUserDetails = sharedViewModel.objRootAppPaymentDetail.objUserDetails?.copy(
                                        userId = viewModel.userCredentials.value
                                    )

                                    sharedViewModel.objRootAppPaymentDetail = sharedViewModel.objRootAppPaymentDetail.copy(
                                        objUserDetails = updatedUserDetails
                                    )

                                    viewModel.onLoginClick(navHostController,sharedViewModel)
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            title = stringResource(id = R.string.login),
                            enabled = viewModel.isLoginEnabled.value
                        )
                    }
                }
            }

            CustomDialogBuilder.ShowComposed()
        }
    )
}

