package com.analogics.tpaymentsapos.rootUiScreens.tid.view


import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.tid.viewModel.TidViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppHeader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.InputTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun TidScreen(navHostController: NavHostController, viewModel: TidViewModel = hiltViewModel()) {
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
                title = stringResource(id = R.string.activate),
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
                        text = stringResource(id = R.string.plz_enter_tid),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_20_CompactMedium)
                    )

                    InputTextField(
                        enabled = viewModel.isActivationEnabled.value,
                        inputValue = viewModel.tidInput.value,
                        onChange = { viewModel.onTidChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.tid),
                        placeHolder = stringResource(id = R.string.please_enter_tid),
                        icon = Icons.Outlined.Numbers,
                        keyboardType = KeyboardType.Number
                    )
                    InputTextField(
                        enabled = viewModel.isActivationEnabled.value,
                        inputValue = viewModel.midInput.value,
                        onChange = { viewModel.onMidChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(id = R.string.mid),
                        placeHolder = stringResource(id = R.string.please_enter_Mid),
                        icon = Icons.Outlined.Numbers,
                        keyboardType = KeyboardType.Number
                    )
                    Box(
                        modifier = Modifier.padding(top = MaterialTheme.dimens.DP_17_CompactMedium)
                    ) {
                        val message = stringResource(id = R.string.cred_not_to_be_empty_tid)
                        AppButton(
                            onClick = {
                                if (viewModel.isFormValid) {
                                    viewModel.onActivationClick(navHostController,sharedViewModel)
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            title = stringResource(id = R.string.activate),
                            enabled = viewModel.isActivationEnabled.value
                        )
                    }
                }
            }

            CustomDialogBuilder.ShowComposed()
        }
    )
}

