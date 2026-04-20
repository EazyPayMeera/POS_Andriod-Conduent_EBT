package com.eazypaytech.pos.features.changepassword.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.AppButton
import com.eazypaytech.pos.core.ui.components.inputfields.AppHeader
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.inputfields.InputTextField
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.themes.dimens

@Composable
fun ChangePasswordScreen(navHostController: NavHostController?, viewModel: ChangePasswordViewModel = hiltViewModel()) {
    //val viewModel: LoginViewModel = hiltViewModel()
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.change_password),
                onBackButtonClick = { /*navHostController?.popBackStack()*/ },
                icon1 = R.drawable.baseline_arrow_back_24,
                onIcon1Click = { navHostController?.popBackStack() },
                backgroundColor = MaterialTheme.colorScheme.onPrimary,
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
                        text = stringResource(id = R.string.plz_ent_new_pass),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        inputValue = viewModel.currentPassword.value,
                        onChange = { viewModel.onCurrentChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.label_current_pwd),
                        placeHolder = stringResource(id = R.string.placehldr_current_pwd),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number,
                        isPasswordField = true,
                        keyboardActions = KeyboardActions.Default.onDone
                    )

                    InputTextField(
                        inputValue = viewModel.newPassword.value,
                        onChange = { viewModel.onNewChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.label_new_pwd),
                        placeHolder = stringResource(id = R.string.placehldr_new_pwd),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number,
                        isPasswordField = true,
                        keyboardActions = KeyboardActions.Default.onDone
                    )

                    InputTextField(
                        inputValue = viewModel.confirmPassword.value,
                        onChange = { viewModel.onConfirmChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.label_confirm_pwd),
                        placeHolder = stringResource(id = R.string.placehldr_Confirm_password),
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number,
                        isPasswordField = true,
                        keyboardActions = KeyboardActions.Default.onDone
                    )

                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_50_CompactMedium)
                    ) {
                        AppButton(
                            onClick = {
                                    viewModel.onChangePasswordClick(navHostController,sharedViewModel)
                            },
                            title = stringResource(id = R.string.change_password)
                        )
                    }
                }
            }
        }
    )
    CustomDialogBuilder.ShowComposed()
}
