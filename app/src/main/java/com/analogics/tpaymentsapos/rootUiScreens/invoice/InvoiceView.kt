package com.analogics.tpaymentsapos.rootUiScreens.invoice

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.login.InvoiceViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun InvoiceView(navHostController: NavHostController) {
    val context = LocalContext.current
    // Get ViewModel instance
    val viewModel: InvoiceViewModel = hiltViewModel()

    // Collect the state from ViewModel
    val invoiceno by viewModel.invoiceno.collectAsState()

    val coroutineScope = rememberCoroutineScope()

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

                coroutineScope.launch {
                    viewModel.initScanner(context, object :
                        IScannerResultProviderListener {
                        override fun onSuccess(result: Any?) {
                            if (result?.equals(true) == true)
                                Log.d(TAG, "Initialization of printer is Successful")
                            else
                                Log.d(TAG, "Initialization of printer is Failed")
                        }
                        override fun onFailure(exception: Exception) {
                            // No action needed, failure is handled elsewhere
                        }
                    })
                }

                TextView(
                    text = stringResource(id = R.string.enter_invoice),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                ImageView(
                    imageId = R.drawable.invoice,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = ""
                )

                OutlinedTextField(
                    value = invoiceno,
                    onValueChange = { newValue -> viewModel.updateInvoiceNo(newValue) },
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = stringResource(id = R.string.invoice_no),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.dimens.SP_25_CompactMedium
                    ),
                    keyboardType = KeyboardType.Uri,
                    onDoneAction = {
                        viewModel.navigateToAmountScreen(navHostController)
                    },
                    isPassword = false
                )

                if (TxnInfo.txnType in listOf(TxnType.REFUND, TxnType.VOID, TxnType.AUTHCAP)) {

                    TextView(
                        text = "",
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                        textAlign = TextAlign.Center
                    )

                    ImageView(
                        imageId = R.drawable.scannerd,
                        size = MaterialTheme.dimens.DP_70_CompactMedium,
                        shape = RectangleShape,
                        alignment = Alignment.Center,
                        modifier = Modifier.clickable {

                        },
                        contentDescription = ""
                    )
                }
            }
        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { viewModel.navigateToTrainingScreen(navHostController) },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.navigateToAmountScreen(navHostController) }
        )
    }
}


