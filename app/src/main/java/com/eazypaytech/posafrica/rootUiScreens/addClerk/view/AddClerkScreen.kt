package com.eazypaytech.posafrica.rootUiScreens.addClerk.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootModel.UserType
import com.eazypaytech.posafrica.rootUiScreens.addClerk.viewModel.AddClerkViewModel
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.AppButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.AppHeader
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.InputTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.ui.theme.dimens


@Composable
fun AddClerkScreen(navHostController: NavHostController, viewModel: AddClerkViewModel = hiltViewModel()) {
    val sharedViewModel = localSharedViewModel.current
    val focusRequester = remember { FocusRequester() }

    @Composable
    fun displayUserType()
    {
        val radioOptions = mapOf<UserType,String>(UserType.ADMIN to stringResource(id = R.string.clerk_type_admin), UserType.CLERK to stringResource(id = R.string.clerk_type_clerk))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.DP_10_CompactMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text =stringResource(id = R.string.clerk_type),
                fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Left,
            )
            radioOptions.forEach { (type, text) ->
                Column(
                    Modifier
                        .selectable(
                            selected = viewModel.userType.value == type,
                            onClick = { viewModel.userType.value  = type },
                            enabled = !(type == UserType.CLERK && viewModel.allowClerks.value != true)
                        )
                        /*.padding(horizontal = MaterialTheme.dimens.DP_10_CompactMedium)*/,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically)
                    {
                        RadioButton(
                            selected = viewModel.userType.value == type,
                            onClick = { viewModel.userType.value  = type },
                            enabled = !(type == UserType.CLERK && viewModel.allowClerks.value != true)
                        )
                        Text(
                            text = text,
                            fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Left
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.clerk_register_title),
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
                        text = stringResource(id = R.string.clerk_register_clerk_prompt),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        enabled = viewModel.isRegisterBtnEnabled.value,
                        inputValue = viewModel.userCredentials.value,
                        onChange = { viewModel.onEmailChange(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        label = stringResource(id = R.string.username),
                        placeHolder = stringResource(id = R.string.placehldr_username),
                        icon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Uri
                    )

                    InputTextField(
                        enabled = viewModel.isRegisterBtnEnabled.value,
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
                            viewModel.onRegisterClick(navHostController,sharedViewModel)
                        }
                    )
                    InputTextField(
                        enabled = viewModel.isRegisterBtnEnabled.value,
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
                            viewModel.onRegisterClick(navHostController,sharedViewModel)
                        }
                    )

                    displayUserType()

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_17_CompactMedium)
                    ) {
                        AppButton(
                            onClick = {
                                viewModel.onRegisterClick(navHostController,sharedViewModel)
                            },
                            title = stringResource(id = R.string.clerk_register_btn),
                            enabled = viewModel.isRegisterBtnEnabled.value
                        )
                    }

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_17_CompactMedium)
                    ) {
                        AppButton(
                            onClick = {
                                viewModel.onDoneClick(navHostController,sharedViewModel)
                            },
                            title = stringResource(id = R.string.clerk_register_done_btn),
                            enabled = viewModel.isDoneBtnEnabled.value
                        )
                    }
                }
            }

            CustomDialogBuilder.ShowComposed()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.onLoad(focusRequester)
    }
}

