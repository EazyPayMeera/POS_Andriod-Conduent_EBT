package com.analogics.tpaymentsapos.rootUiScreens.login.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.InputTextField
import com.analogics.tpaymentsapos.ui.theme.dimens

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.analogics.tpaymentsapos.rootUiScreens.login.viewModel.LoginViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppHeader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView

@Composable
fun LoginScreenView(navHostController: NavHostController?) {
    val viewModel: LoginViewModel = viewModel()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.login),
                onBackButtonClick = { /* Handle back button click if needed */ },
                icon1 = R.drawable.baseline_arrow_back_24,
                onIcon1Click = {  },
                backgroundColor = Color.White,
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
                        size = MaterialTheme.dimens.DP_40_CompactMedium     )

                    TextView(
                        text = stringResource(id = R.string.plz_login),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        inputValue = viewModel.emailCredentials.value,
                        onChange = { viewModel.onEmailChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.username),
                        placeHolder = stringResource(id = R.string.placehldr_username),
                        icon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Uri
                    )

                    InputTextField(
                        inputValue = viewModel.pwdCredentials.value,
                        onChange = { viewModel.onPasswordChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.password),
                        placeHolder = stringResource(id = R.string.placehldr_password),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Uri,
                        isPasswordField = true,
                        keyboardActions = KeyboardActions.Default.onDone,
                        onActionDone = {
                            viewModel.onLoginClick(navHostController,context)
                        }
                    )

                    // "Forgot Password?" clickable text
                    TextView(
                        text = stringResource(id = R.string.forget_pswd),
                        color = Color(0xFFFFA500),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_24_CompactMedium)
                            .clickable {
                                navHostController?.navigate(AppNavigationItems.ForgetPasswordScreen.route)
                            }
                    )

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_50_CompactMedium)
                    ) {
                        AppButton(
                            onClick = {
                                if (viewModel.isFormValid) {
                                    viewModel.onLoginClick(navHostController,context)
                                } else {
                                    Toast.makeText(context, "Email And password not empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            title = stringResource(id = R.string.login)
                        )
                    }
                }
            }
        }
    )
}
