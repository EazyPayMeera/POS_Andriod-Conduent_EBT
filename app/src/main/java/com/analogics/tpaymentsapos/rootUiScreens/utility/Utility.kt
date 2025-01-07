
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
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
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getLogoBitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private val _lastTransactionList = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())
val lastTransactionList: StateFlow<List<ObjRootAppPaymentDetails>> = _lastTransactionList

suspend fun getPrinterStatus(objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener) {
    try {

        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )
        PrinterServiceRepository(paymentServiceTxnDetails).getStatus(iPrinterResultProviderListener)

    } catch (e: Exception) {
        Log.e(TAG, "Failed to get printer status: ${e.message}")
    }
}


suspend fun addLogo(context: Context, objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener, logoResId: Int)
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


// Print Receipt Function
fun CoroutineScope.printReceipt(
    logoResId: Int,
    sharedViewModel: SharedViewModel,
    context: Context,
    customer: Boolean = false,
    isSummary: Boolean = false,
    isDetail: Boolean = false,
    txnList: List<ObjRootAppPaymentDetails>?,
    objRootAppPaymentDetail: ObjRootAppPaymentDetails,
    lastTxn: Boolean
) {
    launch {
        getPrinterStatus(objRootAppPaymentDetail, object : IPrinterResultProviderListener {
            override fun onSuccess(result: Any?) {
                Log.d(TAG, "Printer status retrieved: $result")

                val subtitleText = when (result) {
                    -1 -> context.resources.getString(R.string.printer_out_of_paper)
                    else -> context.resources.getString(R.string.printer_busy)
                }

                if (result != 0) {
                    Log.d(TAG, "Printer status retrieved inside result not equal to zero: $result")
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.resources.getString(R.string.printer_error_title),
                        subtitle = subtitleText
                    )
                } else {
                    launch {
                        try {
                            initPrinter(
                                logoResId,
                                sharedViewModel,
                                context,
                                customer,
                                lastTxn,
                                isSummary,
                                isDetail,
                                txnList,
                                objRootAppPaymentDetail,
                                object : IPrinterResultProviderListener {
                                    override fun onSuccess(result: Any?) {
                                        Log.d(TAG, "Printer initialized successfully.")
                                    }

                                    override fun onFailure(exception: Exception) {
                                        Log.e(TAG, "Failed to initialize printer: ${exception.message}")
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
            }
        })
    }
}

suspend fun initPrinter(
    logoResId: Int,
    sharedViewModel: SharedViewModel,
    context: Context,
    customer: Boolean = false,
    lastTxn: Boolean = false,
    isSummary: Boolean = false,
    isDetail: Boolean = false,
    txnList: List<ObjRootAppPaymentDetails>?,
    objRootAppPaymentDetail: ObjRootAppPaymentDetails,
    iPrinterResultProviderListener: IPrinterResultProviderListener
) {
    try {
        Log.d(TAG, "Approved View Model to Printer Service Repository 1")
        CustomDialogBuilder.composePrintingDialog(
            title = context.resources.getString(R.string.printing),
            subtitle = context.resources.getString(R.string.plz_wait),
            onClose = {
                Log.d("Abort", "Abort Button Pressed")
                // Additional actions if needed
            }
        )

        // Retrieve the last transaction from _lastTransactionList
        val lastTransactionList = _lastTransactionList.value // This is a List<ObjRootAppPaymentDetails>
        val latestTransaction = if(lastTxn) lastTransactionList.lastOrNull() else objRootAppPaymentDetail // Get the last transaction or null if the list is empty

        // Check if the latest transaction is not null before proceeding
        latestTransaction.let { transaction ->
            // Convert the latest transaction to JSON string
            val requestDetails = PaymentServiceUtils.objectToJsonString(transaction)

            // Convert JSON string to PaymentServiceTxnDetails and handle nullable
            val paymentServiceTxnDetails =
                PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)

            if (paymentServiceTxnDetails == null) {
                Log.e(TAG, "Error: Failed to convert ObjRootAppPaymentDetails to PaymentServiceTxnDetails")
                return
            }

            // Proceed with non-nullable paymentServiceTxnDetails
            PrinterServiceRepository(paymentServiceTxnDetails).initPrinter(
                context,
                iPrinterResultProviderListener
            )

            if(isSummary)
            {
                addDetailedReceipt(
                    sharedViewModel,
                    context,
                    objRootAppPaymentDetail,
                    txnList,
                    object : IPrinterResultProviderListener {
                        override fun onSuccess(result: Any?) {
                            if(result == true)
                            {
                                CustomDialogBuilder.hideProgress()
                            }
                        }
                        override fun onFailure(exception: Exception) {

                        }
                    }
                )
            }
            else if(isDetail)
            {
                addSummaryDetails(context,sharedViewModel,objRootAppPaymentDetail,object : IPrinterResultProviderListener{
                    override fun onSuccess(result: Any?) {
                        if(result == true)
                        {
                            CustomDialogBuilder.hideProgress()
                        }
                    }
                    override fun onFailure(exception: Exception) {

                    }
                })
            }
            else {
                addReceiptDetails(
                    context,
                    sharedViewModel,
                    customer,
                    paymentServiceTxnDetails,
                    object : IPrinterResultProviderListener {
                        override fun onSuccess(result: Any?) {
                            if (result == true) {
                                Log.d("Abort", "Printing Successful")
                                CustomDialogBuilder.hideProgress()
                            }
                        }

                        override fun onFailure(exception: Exception) {
                            Log.e(TAG, "Failed to print receipt: ${exception.message}")
                        }
                    }
                )
            }
        } ?: Log.d(TAG, "No transactions available for printing.")
    } catch (e: Exception) {
        AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
    }
}


private suspend fun addReceiptDetails(
    context: Context,
    sharedViewModel: SharedViewModel,
    customer: Boolean,
    paymentServiceTxnDetails: PaymentServiceTxnDetails,
    iPrinterResultProviderListener: IPrinterResultProviderListener
) {
    val receiptBuilder = ReceiptBuilder()

    withContext(Dispatchers.IO) {
        // Generate the receipt
        val receipt = receiptBuilder.createReceipt(context, customer, sharedViewModel, paymentServiceTxnDetails)

        if (receipt != null) {
            val labelList = receipt.fields.map { it.label.toString() }
            val descriptionList = receipt.fields.map { it.description.toString() }
            val alignmentText = receipt.fields.map { field ->
                when (field.alignment) {
                    ReceiptBuilder.Alignment.LEFT -> 0
                    ReceiptBuilder.Alignment.CENTER -> 1
                    ReceiptBuilder.Alignment.RIGHT -> 2
                    ReceiptBuilder.Alignment.NONE -> -1
                    else -> 0 // Default to left alignment if no match
                }
            }
            val fontsize = receipt.fields.map { field ->
                when (field.fontsize) {
                    ReceiptBuilder.FontSize.Small -> 24
                    ReceiptBuilder.FontSize.Medium -> 28
                    ReceiptBuilder.FontSize.Big -> 32
                    else -> 1 // Default font size if no match
                }
            }
            val barcodeString = receipt.fields.find { it.label == "BARCODE" }?.value ?: ""

            val receiptDetails = receipt.fields.map { (label, value) ->
                if (value?.isEmpty() == true) {
                    "$label"
                } else {
                    "$label: $value"
                }
            } + receipt.items.mapIndexed { index, item ->
                "${index + 1}. ${item.name}              $${item.price}"
            }

            val alignment = receipt.fields.firstOrNull { it.label == "QR CODE" }?.let { field ->
                when (field.alignment) {
                    ReceiptBuilder.Alignment.LEFT -> 0
                    ReceiptBuilder.Alignment.CENTER -> 1
                    ReceiptBuilder.Alignment.RIGHT -> 2
                    else -> -1 // Default or error value
                }
            } ?: -1 // Default or error value if no QR CODE field is found

            // Prepare the printing format
            val format = Bundle().apply {
                putInt("align", alignment)
                putInt("width", 300)
                putInt("height", 100)
                putSerializable("barcode_type", BarcodeFormat.CODE_39)
            }

            PrinterServiceRepository(paymentServiceTxnDetails).printReceiptDetails(
                format,
                barcodeString,
                receiptDetails,
                descriptionList,
                alignmentText,
                fontsize,
                iPrinterResultProviderListener
            )
        } else {
            Log.d("ReceiptBuilder", "Failed to create receipt.")
        }
    }
}



suspend fun stopPrinting(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener) {
    try {

        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )
        PrinterServiceRepository(paymentServiceTxnDetails).stopPrinting(iPrinterResultProviderListener)

    } catch (e: Exception) {
        Log.e(TAG, "Failed to get printer status: ${e.message}")
    }
}

private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
    val gson = Gson()
    val json = gson.toJson(txnEntityList)
    val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
    return gson.fromJson(json, txnDataListType)
}

suspend fun fetchLastTransactions(
    sharedViewModel: SharedViewModel,
    context: Context,
    customer: Boolean = false,
    txnDBRepository: TxnDBRepository // Add this parameter
) {
    Log.d("Print Last Receipt", "Last Receipt Clicked")

    val latestTransaction = txnDBRepository.fetchLastTransaction()
    Log.d("Print Last Receipt", latestTransaction.toString())

    if (latestTransaction != null) {
        val txnDataList = convertTxnEntityListToTxnDataList(listOf(latestTransaction))
        withContext(Dispatchers.Main) {
            _lastTransactionList.value = txnDataList
            printReceipt(0, sharedViewModel, context, customer,false,false,null, sharedViewModel.objRootAppPaymentDetail,lastTxn = true)
        }
    } else {
        withContext(Dispatchers.Main) {
            Log.d("db data", "No transaction found.")
            CustomDialogBuilder.composeAlertDialog(
                title = context.resources.getString(R.string.printer_Alert),
                subtitle = context.resources.getString(R.string.printer_no_record)
            )
        }
    }
}


suspend fun addDetailedReceipt(
    sharedViewModel: SharedViewModel,
    context: Context,
    objRootAppPaymentDetail: ObjRootAppPaymentDetails,
    transactionList: List<ObjRootAppPaymentDetails>?, // Assuming this is the input type
    iPrinterResultProviderListener: IPrinterResultProviderListener
) {
    val receiptBuilder = ReceiptBuilder()
    withContext(Dispatchers.IO) {
        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )
        Log.d("PaymentServiceTxnDetails", "Details: $paymentServiceTxnDetails")
        val transactionDetailsList = transactionList?.map { paymentDetail ->
            ReceiptBuilder.TransactionDetails(
                TxnType = paymentDetail.txnType.toString(), // Replace with actual property
                Status = paymentDetail.txnStatus.toString(), // Replace with actual property
                InvoiceNo = paymentDetail.invoiceNo.toString(),
                AuthCode = paymentDetail.hostAuthCode.toString(),
                txnAmount = paymentDetail.txnAmount.toString(),
                ttlAmount = paymentDetail.ttlAmount.toString(),
                timedate = paymentDetail.dateTime.toString()
            )
        }
        val detailedReport = receiptBuilder.createDetailReport(context,sharedViewModel,paymentServiceTxnDetails, transactionDetailsList)
        val labelList: List<String> = detailedReport.detailFields.map { it.label }
        val valueList: List<String> = detailedReport.detailFields.map { it.quantity }
        val descriptionList: List<String> = detailedReport.detailFields.map { it.price }
        val fontsize: List<Int> = detailedReport.detailFields.map { field ->
            when (field.discount) { // Accessing the fourth element (the font size)
                ReceiptBuilder.FontSize.Small -> 24
                ReceiptBuilder.FontSize.Medium -> 28
                ReceiptBuilder.FontSize.Big -> 32
                else -> 24 // Default font size if no match
            }
        }
        PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
            labelList,
            valueList,
            descriptionList,
            fontsize,
            iPrinterResultProviderListener
        )
    }
}

suspend fun addSummaryDetails(
    context: Context,
    sharedViewModel: SharedViewModel,
    objRootAppPaymentDetail: ObjRootAppPaymentDetails,
    iPrinterResultProviderListener: IPrinterResultProviderListener
) {
    val receiptBuilder = ReceiptBuilder()
    withContext(Dispatchers.IO) {
        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )
        val summaryReport = receiptBuilder.createSummaryReport(context,sharedViewModel, paymentServiceTxnDetails)

        val labelList: List<String> = summaryReport.summaryFields.map { it.label }
        val valueList: List<String> = summaryReport.summaryFields.map { it.value }
        val descriptionList: List<String> = summaryReport.summaryFields.map { it.description }
        val fontsize: List<Int> = summaryReport.summaryFields.map { field ->
            when (field.fontsize) { // Accessing the fourth element (the font size)
                ReceiptBuilder.FontSize.Small -> 24
                ReceiptBuilder.FontSize.Medium -> 28
                ReceiptBuilder.FontSize.Big -> 32
                else -> 24 // Default font size if no match
            }
        }
        PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
            labelList,
            valueList,
            descriptionList,
            fontsize,
            iPrinterResultProviderListener
        )
    }
}




















