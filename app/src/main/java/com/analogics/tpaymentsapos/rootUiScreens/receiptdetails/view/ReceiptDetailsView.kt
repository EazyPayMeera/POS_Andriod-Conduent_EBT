package com.analogics.tpaymentsapos.rootUiScreens.receiptdetails.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.receiptdetails.viewmodel.ReceiptDetailsViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun ReceiptDetailsView(navHostController: NavHostController) {
    val viewModel: ReceiptDetailsViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    // Create states for headers and footers
    val headerValues = remember {
        listOf(
            mutableStateOf(sharedViewModel.objPosConfig?.header1 ?: ""),
            mutableStateOf(sharedViewModel.objPosConfig?.header2 ?: ""),
            mutableStateOf(sharedViewModel.objPosConfig?.header3 ?: ""),
            mutableStateOf(sharedViewModel.objPosConfig?.header4 ?: "")
        )
    }

    val footerValues = remember {
        listOf(
            mutableStateOf(sharedViewModel.objPosConfig?.footer1 ?: ""),
            mutableStateOf(sharedViewModel.objPosConfig?.footer2 ?: ""),
            mutableStateOf(sharedViewModel.objPosConfig?.footer3 ?: ""),
            mutableStateOf(sharedViewModel.objPosConfig?.footer4 ?: "")
        )
    }

    val configChanged = remember { mutableStateOf(false) }

    Column {
        // Top AppBar
        CommonTopAppBar(
            title = stringResource(id = R.string.receipt_details),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Card for content
        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                // LazyColumn to list header and footer fields
                LazyColumn {
                    // Header Text
                    item {
                        TextView(
                            text = stringResource(id = R.string.header),
                            fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                                .fillMaxWidth(), // Ensure it fills the available width
                            textAlign = TextAlign.Center // Ensure the text is centered
                        )
                    }

                    // Header Fields (4 fields for Header)
                    items(headerValues.size) { index ->
                        val headerValue = headerValues[index]
                        val label = "Header ${index + 1}"

                        HeaderFooterRow(
                            label = label,
                            value = headerValue.value,
                            onValueChange = { newValue ->
                                configChanged.value = true
                                headerValue.value = newValue
                                viewModel.updateHeader(index, newValue)
                            },
                            placeholderResId = when (index) {
                                0 -> R.string.header1_placeholder
                                1 -> R.string.header2_placeholder
                                2 -> R.string.header3_placeholder
                                3 -> R.string.header4_placeholder
                                else -> R.string.default_placeholder
                            }
                        )
                    }

                    // Footer Label (Centered)
                    item {
                        TextView(
                            text = stringResource(id = R.string.footer),
                            fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                                .fillMaxWidth(), // Ensure it fills the available width
                            textAlign = TextAlign.Center // Ensure the text is centered
                        )
                    }

                    // Footer Fields (4 fields for Footer)
                    items(footerValues.size) { index ->
                        val footerValue = footerValues[index]
                        val label = "Footer ${index + 1}"

                        HeaderFooterRow(
                            label = label,
                            value = footerValue.value,
                            onValueChange = { newValue ->
                                configChanged.value = true
                                footerValue.value = newValue
                                viewModel.updateFooter(index, newValue)
                            },
                            placeholderResId = when (index) {
                                0 -> R.string.footer1_placeholder
                                1 -> R.string.footer2_placeholder
                                2 -> R.string.footer3_placeholder
                                3 -> R.string.footer4_placeholder
                                else -> R.string.default_placeholder
                            }
                        )
                    }

                    // Save button (centered)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.dimens.DP_24_CompactMedium),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OkButton(
                                onClick = {
                                    viewModel.onSave(navHostController, sharedViewModel)
                                    configChanged.value = false
                                },
                                title = stringResource(id = R.string.save_btn),
                                enabled = configChanged.value
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(sharedViewModel)
    }
}


@Composable
fun HeaderFooterRow(label: String, value: String, onValueChange: (String) -> Unit, placeholderResId: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(MaterialTheme.dimens.DP_20_CompactMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
            ),
            placeholder = {
                Text(
                    text = stringResource(id = placeholderResId),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            singleLine = true
        )
    }
}
