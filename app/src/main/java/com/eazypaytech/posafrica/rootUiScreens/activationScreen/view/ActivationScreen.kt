package com.eazypaytech.posafrica.rootUiScreens.activationScreen.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.activationScreen.viewModel.ActivationViewModel
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.AppHeader
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.InputTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.LoginButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.ui.theme.dimens


@Composable
fun ActivationScreen(navHostController: NavHostController, viewModel: ActivationViewModel = hiltViewModel()) {
//
//    val apiKey = "your_api_key"
//    val apiSecret = "your_api_secret"
//
//    QRCodeView(apiKey = apiKey, apiSecret = apiSecret)
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.act_title),
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
                        .padding(top = MaterialTheme.dimens.DP_125_CompactMedium)
                ) {
                    ImageView(
                        imageId =  R.drawable.unlock, // Decorative image
                        size = MaterialTheme.dimens.DP_60_CompactMedium,
                        contentDescription = "",
                    )

                    TextView(
                        text = stringResource(id = R.string.act_prompt_activate),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 25.dp, horizontal = MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        enabled = viewModel.isActivationBtnEnabled.value,
                        inputValue = viewModel.procIdInput.value,
                        onChange = { viewModel.onTidChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.proc_id),
                        placeHolder = stringResource(id = R.string.act_label_enter_Proc_id),
                        icon = Icons.Outlined.Numbers,
                        keyboardType = KeyboardType.Ascii
                    )
                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_17_CompactMedium)
                    ) {
                        LoginButton(
                            onClick = {
                                if (viewModel.isFormValid) {
                                    viewModel.onActivationClick(navHostController,sharedViewModel)
                                } else {
                                    viewModel.onInvalidFormData(context)
                                }
                            },
                            title = stringResource(id = R.string.act_title),
                            enabled = viewModel.isActivationBtnEnabled.value
                        )
                    }
                }
            }

            CustomDialogBuilder.ShowComposed()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.onLoad(sharedViewModel)
    }
}

