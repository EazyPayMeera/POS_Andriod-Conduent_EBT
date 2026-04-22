package com.eazypaytech.pos.features.activation.ui

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.AppHeader
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.inputfields.InputTextField
import com.eazypaytech.pos.core.ui.components.inputfields.LoginButton
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.themes.dimens


@Composable
fun ActivationScreen(navHostController: NavHostController, viewModel: ActivationViewModel = hiltViewModel()) {
    // Get current Android context
    val context = LocalContext.current
    // Shared ViewModel used across screens
    val sharedViewModel = localSharedViewModel.current

    // Main screen layout with TopBar
    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.act_title),
                onBackButtonClick = { /* Handle back button click if needed */ },
                isIcon1Visible = false,
                isIcon2Visible = false
            )
        },
        content = { padding ->
            // Root container
            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                // Main vertical layout
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
                        .padding(top = MaterialTheme.dimens.DP_40_CompactMedium)
                ) {
                    // Decorative unlock image
                    ImageView(
                        imageId =  R.drawable.unlock, // Decorative image
                        size = MaterialTheme.dimens.DP_40_CompactMedium,
                        contentDescription = ""
                    )
                    // Activation instruction text
                    TextView(
                        text = stringResource(id = R.string.act_prompt_activate),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )
                    // PROC ID input field
                    InputTextField(
                        enabled = viewModel.isActivationBtnEnabled.value,
                        inputValue = viewModel.procIdInput.value,
                        onChange = { viewModel.onProcIdChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.procid),
                        placeHolder = stringResource(id = R.string.act_label_enter_ProcId),
                        icon = Icons.Outlined.Numbers,
                        keyboardType = KeyboardType.Ascii
                    )
                    // TID input field
                    InputTextField(
                        enabled = viewModel.isActivationBtnEnabled.value,
                        inputValue = viewModel.tidInput.value,
                        onChange = { viewModel.onTidChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.tid),
                        placeHolder = stringResource(id = R.string.act_label_enter_tid),
                        icon = Icons.Outlined.Numbers,
                        keyboardType = KeyboardType.Ascii
                    )
                    // MID input field
                    InputTextField(
                        enabled = viewModel.isActivationBtnEnabled.value,
                        inputValue = viewModel.midInput.value,
                        onChange = { viewModel.onMidChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.mid),
                        placeHolder = stringResource(id = R.string.act_label_enter_Mid),
                        icon = Icons.Outlined.Numbers,
                        keyboardType = KeyboardType.Ascii
                    )
                    // Activation button container
                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_17_CompactMedium)
                    ) {
                        LoginButton(
                            onClick = {
                                // Validate form before proceeding
                                if (viewModel.isFormValid) {
                                    // Trigger activation API/process
                                    viewModel.onActivationClick(navHostController,sharedViewModel)
                                } else {
                                    // Show validation error message
                                    viewModel.onInvalidFormData(context)
                                }
                            },
                            title = stringResource(id = R.string.act_title),
                            enabled = viewModel.isActivationBtnEnabled.value // Button enable/disable
                        )
                    }
                }
            }
            // Custom dialog handler (loading, error, success, etc.)
            CustomDialogBuilder.ShowComposed()
        }
    )

    // Runs once when screen is launched
    LaunchedEffect(Unit) {
        // Initialize screen data
        viewModel.onLoad(sharedViewModel)
        // Copy config file to external storage (if required)
        viewModel.copyConfigToExternal(context)
        // Read Master KEK (Key Encryption Key)
        val master = viewModel.readMasterKEK(context,sharedViewModel)
    }
}

