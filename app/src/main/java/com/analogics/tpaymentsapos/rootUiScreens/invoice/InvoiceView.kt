package com.analogics.tpaymentsapos.rootUiScreens.invoice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.login.InvoiceViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun InvoiceView(navHostController: NavHostController) {
    val context = LocalContext.current
    val viewModel: InvoiceViewModel = hiltViewModel()
    var sharedViewModel= localSharedViewModel.current
    // Collect the state from ViewModel
    val invoiceno by viewModel.invoiceno.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isDialogVisible by remember { mutableStateOf(false) }

    // Define the request code
    val CAMERA_REQUEST_CODE = 100

    // Permission check
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            (context as? Activity) ?: return, // Cast context to Activity
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

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
                // Initialize the scanner if permissions are granted
                coroutineScope.launch {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        viewModel.initScanner(context, object : IScannerResultProviderListener {
                            override fun onSuccess(result: Any?) {
                                if (result == "SUCCESS")
                                    Log.d(TAG, "Initialization of scanner is Successful")
                                else
                                    Log.d(TAG, "Initialization of scanner is Failed : $result" )
                            }

                            override fun onFailure(exception: Exception) {
                                Log.e(TAG, "Scanner initialization failed on Failure: ${exception.message}")
                            }
                        })
                    }
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
                        viewModel.onConfirm(navHostController, sharedViewModel)
                    },
                    isPassword = false,
                    trailingIcon = if (sharedViewModel.objRootAppPaymentDetail.txnType in listOf(
                            TxnType.REFUND, TxnType.VOID, TxnType.AUTHCAP)) {
                        {
                            Icon(
                                imageVector = Icons.Default.QrCode,  // Your QR code icon
                                contentDescription = null, // Provide a content description if needed
                                modifier = Modifier
                                    .clickable {
                                        openScanner(context, viewModel)
                                    },  // Toggle editable state on icon click
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null // No trailing icon if the condition is false
                )
            }
        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { /*viewModel.navigateToTrainingScreen(navHostController)*/ isDialogVisible=true},
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.navigateToAmountScreen(navHostController,sharedViewModel) }
        )
    }


    if (isDialogVisible) {
        CustomDialogBuilder.create()
            .setTitle("Are you sure want to Cancel ?")
            .setSubtitle("")
            .setSmallText("")
            .setShowCloseButton(true) // Can set to false if you don't want the close button
            .setCancelable(true)
            .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
            .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
            .setShowProgressIndicator(false)
            .setOnCancelAction {
                navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
            }
            .setOnConfirmAction {
                navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
            }
            .setShowButtons(true)
            .setNavAction {
                navHostController.popBackStack()
            }
            .buildDialog(onClose = { isDialogVisible = false })

    }


}


fun openScanner(context: Context, viewModel: InvoiceViewModel) {
    val coroutineScope = CoroutineScope(Dispatchers.Main) // Use an appropriate coroutine context

    coroutineScope.launch {
        viewModel.startScanner(
            context,
            Bundle().apply {
                putString("camera_facing", "back") // Ensure back camera is used
            },
            object : IScannerResultProviderListener {
                override fun onSuccess(result: Any?) {
                    if (result is String) {
                        Log.d(TAG, "Scanner result: $result")
                        viewModel.updateInvoiceNo(result)
                    } else {
                        Log.d(TAG, "Scanner failed to return a string result")
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Scanner initialization failed: ${exception.message}")
                }
            }
        )
    }
}
