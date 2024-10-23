package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.emv.TermConfig
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.utility.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getBitmapBytes
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getLogoBitmap
import com.analogics.tpaymentsapos.rootUtils.miscellaneous.readAsset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var emvServiceRepository:EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton

    var LatestTransaction: TxnEntity? = null
    private val _lastTransactionList = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())
    val lastTransactionList: StateFlow<List<ObjRootAppPaymentDetails>> = _lastTransactionList



    fun onButtonClick(text: String, onClick: () -> Unit, sharedViewModel: SharedViewModel) {
        _selectedButton.value = text
        onClick()
        val currentDateTime = getCurrentDateTime()
        val formattedDate = currentDateTime.substring(0, 10).replace("-", "") // Extracts "20241005"

        sharedViewModel.objPosConfig?.apply { batchId = formattedDate }?.saveToPrefs()
    }

    fun navigateTo(navHostController: NavHostController, route: String) {
        navHostController.navigate(route)
    }

    fun clearTransData(sharedViewModel: SharedViewModel) {
        _selectedButton.value = false.toString()
        sharedViewModel.clearTransData()
    }



    @OptIn(DelicateCoroutinesApi::class)
    fun printReceipt(
        logoResId: Int,
        sharedViewModel: SharedViewModel,
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    ) {
        viewModelScope.launch {
            // Call getPrinterStatus with a callback
            getPrinterStatus(objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    Log.d(TAG, "Printer status retrieved: $result")

                    val subtitleText = when (result) {
                        -1 -> context.resources.getString(R.string.printer_out_of_paper) // Example error for result -1
                        else -> context.resources.getString(R.string.printer_busy) // Default error for other cases
                    }

                    if (result != 0) {
                        Log.d(TAG, "Printer status retrieved inside result not equal to zero: $result")
                        CustomDialogBuilder.composeAlertDialog(
                            title = context.resources.getString(R.string.printer_error_title),
                            subtitle = subtitleText // Dynamic subtitle based on result
                        )
                    } else {
                        // If the printer status is OK, call initPrinter
                        launch { // Start a new coroutine to call initPrinter
                            try {
                                initPrinter(logoResId,sharedViewModel,objRootAppPaymentDetail,context, object : IPrinterResultProviderListener {
                                    override fun onSuccess(result: Any?) {
                                        Log.d(TAG, "Printer initialized successfully.")
                                    }

                                    override fun onFailure(exception: Exception) {
                                        Log.e(TAG, "Failed to initialize printer: ${exception.message}")
                                        // Handle failure for printer initialization here
                                    }
                                })
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during printer initialization: ${e.message}")
                            }
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Failed to get printer status: ${exception.message}")
                    // Handle failure for getting printer status here
                }
            })
        }
    }


    suspend fun initPrinter(
        logoResId: Int,
        sharedViewModel: SharedViewModel,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        context: Context,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Approved View Model to Printer Service Repository 1")
                CustomDialogBuilder.composeProgressDialog(
                    title = context.resources.getString(R.string.printing),
                    subtitle = context.resources.getString(R.string.plz_wait)
                )
                // Retrieve the last transaction from _lastTransactionList
                val lastTransactionList = _lastTransactionList.value // This is a List<ObjRootAppPaymentDetails>
                val latestTransaction = lastTransactionList.lastOrNull() // Get the last transaction or null if the list is empty

                // Check if the latest transaction is not null before proceeding
                latestTransaction?.let {
                    // Convert the latest transaction to JSON string
                    val requestDetails = PaymentServiceUtils.objectToJsonString(it)

                    // Pass the transaction details to the PrinterServiceRepository
                    PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails))
                        .initPrinter(context, iPrinterResultProviderListener)

                    addLogo(context,objRootAppPaymentDetail,iPrinterResultProviderListener,logoResId)
                    addReceiptDetails(context,sharedViewModel,object : IPrinterResultProviderListener {
                        override fun onSuccess(result: Any?) {
                            if(result == true)
                            {
                                CustomDialogBuilder.hideProgress()
                            }
                        }
                        override fun onFailure(exception: Exception) {

                        }
                    })

                    Log.d(TAG, "Approved View Model to Printer Service Repository 2 ${PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)}")
                } ?: Log.d(TAG, "No transactions available for printing.")
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    suspend fun getPrinterStatus(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener) {
        try {

            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )
            PrinterServiceRepository(paymentServiceTxnDetails).getStatus(iPrinterResultProviderListener)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get printer status: ${e.message}")
        }
    }

    suspend fun addReceiptDetails(context: Context,sharedViewModel: SharedViewModel,iPrinterResultProviderListener: IPrinterResultProviderListener) {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Switch to IO context for background processing
        withContext(Dispatchers.IO) {
            // Retrieve the last transaction from _lastTransactionList
            val lastTransactionList = _lastTransactionList.value
            val latestTransaction = lastTransactionList.lastOrNull() // Get the last transaction

            // Check if the latest transaction is available
            if (latestTransaction != null) {
                // Create the receipt using payment details
                val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                    PaymentServiceUtils.objectToJsonString(latestTransaction) // Use the latest transaction
                )

                // Generate the receipt
                val receipt = receiptBuilder.createReceipt(context,sharedViewModel,paymentServiceTxnDetails)
                val labelList: List<String> = receipt.fields.map { it.label.toString() }
                val descriptionList: List<String> = receipt.fields.map { it.description.toString() }
                val aligment: List<String> = receipt.fields.map { it.alignment.toString() }
                // Proceed if the receipt was created successfully
                if (receipt != null) {
                    // Extract the barcode string
                    val barcodeString = receipt.fields.find { it.label == "BARCODE" }?.value ?: ""

                    // Prepare receipt details for printing
                    val receiptDetails = receipt.fields.map { (label, value) ->
                        if (value?.isEmpty() == true) {
                            "$label"
                        } else {
                            "$label: $value"
                        }
                    } + receipt.items.mapIndexed { index, item ->
                        "${index + 1}. ${item.name}              $${item.price}"
                    }

                    // Extract alignment information for each field
                    val alignmentText: List<Int> = receipt.fields.map { field ->
                        when (field.alignment) {
                            ReceiptBuilder.Alignment.LEFT -> 0
                            ReceiptBuilder.Alignment.CENTER -> 1
                            ReceiptBuilder.Alignment.RIGHT -> 2
                            ReceiptBuilder.Alignment.NONE -> -1
                            else -> 0 // Default to left alignment if no match
                        }
                    }

                    // Extract alignment for the barcode
                    val alignment: Int = receipt.fields.firstOrNull { it.label == "QR CODE" }?.let { field ->
                        when (field.alignment) {
                            ReceiptBuilder.Alignment.LEFT -> 0
                            ReceiptBuilder.Alignment.CENTER -> 1
                            ReceiptBuilder.Alignment.RIGHT -> 2
                            else -> -1 // Default or error value
                        }
                    } ?: -1 // Default or error value if no barcode field is found

                    // Prepare the printing format
                    val format = Bundle().apply {
                        putInt("align", alignment) // Default alignment for text
                        putInt("width", 300)
                        putInt("height", 100)
                        putSerializable("barcode_type", BarcodeFormat.CODE_39)
                    }

                    // Pass the receipt details to the PrinterServiceRepository
                    PrinterServiceRepository(paymentServiceTxnDetails).printReceiptDetails(
                        format,
                        barcodeString,
                        receiptDetails,
                        descriptionList,
                        alignmentText,
                        iPrinterResultProviderListener
                    )
                } else {
                    Log.d("ReceiptBuilder", "Failed to create receipt.")
                }
            } else {
                Log.d("TAG", "No transactions available for receipt printing.")
            }
        }
    }

    suspend fun addLogo(context: Context, objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener,logoResId: Int)
    {
        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )
        val logoBitmap = getLogoBitmap(context, logoResId)

        // Convert the bitmap to ByteArray
        val imageData = getBitmapBytes(logoBitmap)

        // Ensure the imageData is not null
        if (imageData != null) {
            // Prepare the format Bundle for the printer
            val format = Bundle().apply {
                putInt("align", 1)  // Example alignment: Center
                putInt("width", 100)  // Width of the image
                putInt("height", 100)  // Height of the image
            }

            // Call the addImage function with format and image data
            PrinterServiceRepository(paymentServiceTxnDetails).printImage(format,imageData,iPrinterResultProviderListener)
        } else {
            // Handle the case where the image data is null
            Log.e("ImageError", "Failed to get image bytes")
        }
    }


    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }

    fun fetchLastTransactions() {
        viewModelScope.launch {
            val latestTransaction = txnDBRepository.fetchLastTransaction()
            Log.d("db data", latestTransaction.toString())

            latestTransaction?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(listOf(it)) // Wrap it in a list
                _lastTransactionList.value = txnDataList
                Log.d("latest txn data", _lastTransactionList.value.toString())
            } ?: Log.d("db data", "No transaction found.")
        }
    }

    fun initPaymentSDK(context: Context, sharedViewModel: SharedViewModel) {
        if(sharedViewModel.objPosConfig?.isPaymentSDKInit!=true) {
            viewModelScope.launch {
                emvServiceRepository.initPaymentSDK(
                    termConfig = TermConfig(
                        terminalIdentifier = sharedViewModel.objPosConfig?.terminalId,
                        merchantIdentifier = sharedViewModel.objPosConfig?.merchantId
                    ),
                    aidConfig = readAsset(context, AppConstants.DEFAULT_EMV_CONFIG_FILE_PATH),
                    capKeys = readAsset(context, AppConstants.DEFAULT_EMV_CAP_KEY_FILE_PATH),
                    iEmvServiceResponseListener =  object :
                    IEmvServiceResponseListener {
                    override fun onEmvServiceResponse(result: Any) {
                        if (result == true) {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = true }?.saveToPrefs()
                            CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.emv_sdk_init_title), subtitle = context.resources.getString(R.string.emv_sdk_init_success))
                        }
                        else {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                            CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.emv_sdk_init_title), subtitle = context.resources.getString(R.string.emv_sdk_init_failure))
                       }
                    }

                    override fun onEmvServiceDisplayProgress(
                        show: Boolean,
                        title: String?,
                        subTitle: String?,
                        message: String?
                    ) {
                        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
                    }
                })
            }
        }
    }
}
