package com.eazypaytech.posafrica.rootUiScreens.password.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.password.viewmodel.PasswordValidation
import com.eazypaytech.posafrica.rootUiScreens.password.viewmodel.PasswordViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OutlinedTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.ui.theme.dimens


@Composable
fun PasswordView(navHostController: NavHostController, showPrompt: Boolean? = true, onResult: (Boolean) -> Unit = {}) {
    // Get ViewModel instance
    val context = LocalContext.current
    val viewModel: PasswordViewModel = hiltViewModel()
    var sharedViewModel = localSharedViewModel.current

    // Collect the state from ViewModel
    val password by viewModel.password.collectAsState()

    if(showPrompt == true) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column {
                    CommonTopAppBar(
                        onBackButtonClick = { viewModel.onCancel() }
                    )
                    GenericCard(
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
                        ) {
                            TextView(
                                text = stringResource(id = R.string.enter_password),
                                fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                1,
                                Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                                textAlign = TextAlign.Center
                            )
                            ImageView(
                                imageId = R.drawable.open_lock,
                                size = MaterialTheme.dimens.DP_33_CompactMedium,
                                shape = RectangleShape, // Example shape, can be any Shape
                                alignment = Alignment.Center,
                                contentDescription = "",
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { newValue ->
                                    viewModel.updatePassword(
                                        newValue
                                    )
                                },
                                shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                                placeholder = stringResource(id = R.string.enter_password),
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.dimens.SP_28_CompactMedium
                                ),
                                keyboardType = KeyboardType.NumberPassword,
                                onDoneAction = {
                                    viewModel.onVerifyPassword(
                                        sharedViewModel,
                                        context,
                                        password
                                    )
                                },
                                isPassword = true,
                            )

                        }

                    }

                    FooterButtons(
                        firstButtonTitle = stringResource(id = R.string.cancel_btn),
                        firstButtonOnClick = { viewModel.onCancel() },
                        secondButtonTitle = stringResource(id = R.string.confirm_btn),
                        secondButtonOnClick = {
                            viewModel.onVerifyPassword(
                                sharedViewModel,
                                context,
                                password
                            )
                        },
                        closeKeypadOnFirstButton = true,
                        closeKeypadOnSecondButton = true,
                    )

                    CustomDialogBuilder.ShowComposed()
                }
            }
        }

        LaunchedEffect(navHostController.context) {
            viewModel.event.collect { event ->
                when (event) {
                    is PasswordValidation.Result -> {
                        onResult(event.isValid)
                    }
                    else -> null
                }
            }
        }
    }
}

object PasswordUtil {
    @Composable
    fun PromptUserPassword(navHostController: NavHostController, showPrompt : Boolean, onResult: (Boolean) -> Unit = {}) {
        PasswordView(navHostController, showPrompt) {
            onResult(it)
        }
    }
}
