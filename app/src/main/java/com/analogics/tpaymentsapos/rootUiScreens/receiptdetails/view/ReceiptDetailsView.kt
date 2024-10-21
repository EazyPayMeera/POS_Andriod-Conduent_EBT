package com.analogics.tpaymentsapos.rootUiScreens.receiptdetails.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.receiptdetails.viewmodel.ReceiptDetailsViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens
import com.analogics.tpaymentsapos.R

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

    Log.d("ReceiptDetailsView", "Header 1 Value: ${sharedViewModel.objPosConfig?.header1}")

    Column {
        CommonTopAppBar(title = stringResource(id = R.string.receipt_details),onBackButtonClick = { navHostController.popBackStack() })

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                /*TextView(
                    text = "Headers & Footers",
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )*/

                // LazyColumn to display headers and footers
                LazyColumn {
                    // Iterate over headers and footers
                    val headerFooterPairs = headerValues + footerValues
                    items(headerFooterPairs.size) { index ->
                        val isHeader = index < headerValues.size
                        val currentValue = if (isHeader) headerValues[index] else footerValues[index - headerValues.size]
                        val label = if (isHeader) "Header ${index + 1}" else "Footer  ${index - headerValues.size + 1}"

                        HeaderFooterRow(
                            label = label,
                            value = currentValue.value,
                            onValueChange = { newValue ->
                                configChanged.value = true
                                currentValue.value = newValue
                                if (isHeader) {
                                    viewModel.updateHeader(index, newValue)
                                } else {
                                    viewModel.updateFooter(index - headerValues.size, newValue)
                                }
                            }
                        )
                    }

                    // Center align the OK button
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
fun HeaderFooterRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(MaterialTheme.dimens.DP_20_CompactMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        /*TextView(
            text = label,
            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
            color = MaterialTheme.colorScheme.tertiary,
            maxLines = 1,
            //modifier = Modifier.padding(top = 15.dp),
            textAlign = TextAlign.Start,

        )*/

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                //.padding(MaterialTheme.dimens.DP_5_CompactMedium)
                //.height(MaterialTheme.dimens.DP_50_CompactMedium)
                .fillMaxWidth().fillMaxHeight(),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = MaterialTheme.dimens.SP_21_CompactMedium, fontWeight = FontWeight.Normal),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
            ),
            placeholder = {Text(label,modifier = Modifier.fillMaxWidth() ,textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSecondary)},
            singleLine = true
        )
    }
}
