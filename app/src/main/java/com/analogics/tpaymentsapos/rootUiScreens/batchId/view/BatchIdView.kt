package com.analogics.tpaymentsapos.rootUiScreens.batchId.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.batchId.viewModel.BatchIdViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun BatchIdView(navHostController: NavHostController) {
    val viewModel: BatchIdViewModel = hiltViewModel()
    var sharedViewModel= localSharedViewModel.current

    var batchId by remember { mutableStateOf(sharedViewModel.objPosConfig?.batchId.toString()) }


    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {

                TextView(
                    text = stringResource(id = R.string.batchScreen_BatchIdNo),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                ImageView(
                    imageId = R.drawable.batch,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = ""
                )

                OutlinedTextField(
                    value = batchId, // Use the mutable state here
                    onValueChange = { newValue ->
                        if (!viewModel.isBatchOpen.value) { // Check if the random number is empty
                            batchId = newValue.take(AppConstants.LYRA_MAX_INVOICE_LENGTH) // Update the mutable state when the text changes
                            viewModel.updateBatchId(batchId, sharedViewModel)
                        }
                    },
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = stringResource(id = R.string.batchScreen_BatchId),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.dimens.SP_25_CompactMedium
                    ),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {
                        viewModel.onConfirm(sharedViewModel)
                    },
                    isPassword = false,
                    trailingIcon = null
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.dimens.DP_120_CompactMedium,
                    end = MaterialTheme.dimens.DP_120_CompactMedium,
                    top = MaterialTheme.dimens.DP_24_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_24_CompactMedium
                ),
            horizontalArrangement = Arrangement.Center
        ) {
            OkButton(
                onClick = {
                    Log.d("Click on save Button","OK")
                    viewModel.onConfirm(sharedViewModel)
                },
                title = stringResource(id = R.string.save_btn),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.isBatchOpen()
    }


}

