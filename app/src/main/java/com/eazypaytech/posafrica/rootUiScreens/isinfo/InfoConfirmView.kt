// CashBackView.kt
package com.eazypaytech.posafrica.rootUiScreens.isinfo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OutlinedTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.createAmountTransformation
import com.eazypaytech.posafrica.ui.theme.dimens

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InfoConfirmView(navHostController: NavHostController, viewModel: InfoConfirmViewModel = hiltViewModel()){
    var isEditable by remember { mutableStateOf(false) }
    var sharedViewModel= localSharedViewModel.current
    Column {

        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                TextView(
                    text = stringResource(id = R.string.is_correct),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                ImageView(
                    imageId = R.drawable.void_amt,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = "",
                )
                val Amount = if(isEditable) viewModel.rawInput else viewModel.totalAmount.value ?:"0.00"
                OutlinedTextField(
                    value = Amount,
                    onValueChange = {if (isEditable) viewModel.onAmountChange(it)},
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = "",
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium,textAlign = TextAlign.End),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {viewModel.onConfirm(Amount,sharedViewModel,navHostController)},
                    visualTransformation = createAmountTransformation(),
                    readOnly = !isEditable,
                    trailingIcon = {Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.pencil),  // You can replace this with your vector image
                        contentDescription = "",
                        modifier = Modifier
                            .padding(end = MaterialTheme.dimens.DP_20_CompactMedium)
                            .size(MaterialTheme.dimens.DP_24_CompactMedium) // Set the icon size here
                            .clickable { isEditable = !isEditable },  // Toggle editable state on icon click
                        tint = MaterialTheme.colorScheme.primary
                    )}
                )


                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                TextView(
                    text = viewModel.transactionDateTime,
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                TextView(
                    text = stringResource(id = R.string.card) + " ************6983",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                    )

                TextView(
                    text = stringResource(id = R.string.auth_code) + " 896356",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                )

                TextView(
                    text = stringResource(id = R.string.ref_id) + " 100034345364633",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                )

                TextView(
                    text = stringResource(id = R.string.inc_no) + "INVC1234",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = stringResource(id = R.string.pos_entry) + "Contact",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                )
                FooterButtons(
                    firstButtonTitle = stringResource(id = R.string.cancel_btn),
                    firstButtonOnClick = { viewModel.onCancel(navHostController) },
                    secondButtonTitle = stringResource(id = R.string.confirm_btn),
                    secondButtonOnClick = { viewModel.onConfirm(
                        Amount,
                        sharedViewModel,
                        navHostController
                    ) }
                )

            }
        }

    }

    LaunchedEffect(Unit) {
        viewModel.getTotalAmountByInvoiceNo(sharedViewModel.objRootAppPaymentDetail.invoiceNo.toString())
        viewModel.getTransactionByInvoiceNo(sharedViewModel)
    }
}

