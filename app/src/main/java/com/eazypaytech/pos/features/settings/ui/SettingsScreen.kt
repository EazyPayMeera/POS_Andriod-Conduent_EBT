package com.eazypaytech.pos.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.inputfields.OkButton
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.themes.dimens

@Composable
fun SettingsScreen(navHostController: NavHostController) {

    val viewModel: SettingsViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    val merchantNameLocation = remember {
        mutableStateOf(sharedViewModel.objPosConfig?.merchantNameLocation ?: "")
    }


    val merchantBankName = remember {
        mutableStateOf(sharedViewModel.objPosConfig?.merchantBankName ?: "")
    }

    val merchantType = remember {
        //mutableStateOf(sharedViewModel.objPosConfig?.merchantType ?: "")
        mutableStateOf(sharedViewModel.objPosConfig?.merchantType ?: "")
    }

    val fnsNumber = remember {
        mutableStateOf(sharedViewModel.objPosConfig?.fnsNumber ?: "")
    }

    val stateCode = remember {
        mutableStateOf(sharedViewModel.objPosConfig?.stateCode ?: "")
    }

    val countyCode = remember {
        mutableStateOf(sharedViewModel.objPosConfig?.countyCode ?: "")
    }

    val postalServiceCode = remember {
        mutableStateOf(sharedViewModel.objPosConfig?.postalServiceCode ?: "")
    }

    val configChanged = remember { mutableStateOf(false) }

    Column {

        CommonTopAppBar(
            title = "Merchant Configuration",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium
        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {

                LazyColumn {

                    item {

                        TextView(
                            text = "Merchant Details",
                            fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }


                    item {

                        ConfigRow(
                            label = "Merchant Name and Location",
                            value = merchantNameLocation.value,
                            onValueChange = {
                                merchantNameLocation.value = it
                                configChanged.value = true
                                viewModel.updateMerchantLocation(it)
                            }
                        )
                    }

                    item {

                        ConfigRow(
                            label = "Merchant Bank Name",
                            value = merchantBankName.value,
                            onValueChange = {
                                merchantBankName.value = it
                                configChanged.value = true
                                viewModel.updateMerchantBankName(it)
                            }
                        )
                    }

                    item {

                        ConfigRow(
                            label = "Merchant Type (MCC)",
                            value = merchantType.value,
                            onValueChange = {
                                merchantType.value = it
                                configChanged.value = true
                                viewModel.updateMerchantType(it)
                            },
                            keyboardType = KeyboardType.Number
                        )
                    }

                    item {

                        ConfigRow(
                            label = "FNS Number",
                            value = fnsNumber.value,
                            onValueChange = {
                                fnsNumber.value = it
                                configChanged.value = true
                                viewModel.updateFNSNumber(it)
                            }
                        )
                    }

                    item {
                        ConfigRow(
                            label = "State Code",
                            value = stateCode.value,
                            onValueChange = {
                                stateCode.value = it
                                configChanged.value = true
                                viewModel.updateStateCode(it)
                            }
                        )
                    }

                    item {
                        ConfigRow(
                            label = "County Code",
                            value = countyCode.value,
                            onValueChange = {
                                countyCode.value = it
                                configChanged.value = true
                                viewModel.updateCountyCode(it)
                            }
                        )
                    }

                    item {
                        ConfigRow(
                            label = "Postal Service Code",
                            value = postalServiceCode.value,
                            onValueChange = {
                                postalServiceCode.value = it
                                configChanged.value = true
                                viewModel.updatePostalServiceCode(it)
                            }
                        )
                    }

                    item {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.dimens.DP_24_CompactMedium),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            OkButton(
                                onClick = {

                                    viewModel.onSaveMerchantConfig(
                                        navHostController,
                                        sharedViewModel,
                                        merchantNameLocation.value,
                                        merchantBankName.value,
                                        merchantType.value,
                                        fnsNumber.value,
                                        stateCode.value,
                                        countyCode.value,
                                        postalServiceCode.value
                                    )

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
fun ConfigRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.DP_20_CompactMedium)
    ) {

        Text(
            text = label,
            fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
            ),
            singleLine = true
        )
    }
}


