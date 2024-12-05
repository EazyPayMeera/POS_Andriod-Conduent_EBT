
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
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
    objRootAppPaymentDetail: ObjRootAppPaymentDetails,
    dbRepository: TxnDBRepository // If needed
) {
    launch {
        getPrinterStatus(objRootAppPaymentDetail, object : IPrinterResultProviderListener {
            override fun onSuccess(result: Any?) {
                val subtitleText = when (result) {
                    -1 -> context.resources.getString(R.string.printer_out_of_paper)
                    else -> context.resources.getString(R.string.printer_busy)
                }

                if (result != 0) {
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.resources.getString(R.string.printer_error_title),
                        subtitle = subtitleText
                    )
                } else {
                    launch {
                        try {
                            initPrinter(
                                logoResId, sharedViewModel, context, customer,
                                objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                                    override fun onSuccess(result: Any?) {
                                        Log.d(TAG, "Printer initialized successfully.")
                                    }

                                    override fun onFailure(exception: Exception) {
                                        Log.e(TAG, "Failed to initialize printer: ${exception.message}")
                                    }
                                }
                            )
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

// Init Printer Function
fun CoroutineScope.initPrinter(
    logoResId: Int,
    sharedViewModel: SharedViewModel,
    context: Context,
    customer: Boolean = false,
    objRootAppPaymentDetail: ObjRootAppPaymentDetails,
    iPrinterResultProviderListener: IPrinterResultProviderListener
) {
    launch {
        try {
            CustomDialogBuilder.composePrintingDialog(
                title = context.resources.getString(R.string.printing),
                subtitle = context.resources.getString(R.string.plz_wait),
                onClose = {
                    launch { stopPrinting(objRootAppPaymentDetail, iPrinterResultProviderListener) }
                }
            )

            val requestDetails =
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)

            PrinterServiceRepository(
                PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)
            ).initPrinter(context, iPrinterResultProviderListener)

            addLogo(context, objRootAppPaymentDetail, iPrinterResultProviderListener, logoResId)

            addReceiptDetails(sharedViewModel, context, customer, objRootAppPaymentDetail, object : IPrinterResultProviderListener {

                override fun onSuccess(result: Any?) {
                    Log.d(TAG, "Receipt printed successfully! Result: $result")
                    if (result == true) {
                        Log.d(TAG, "Receipt printed successfully! Result: $result")
                        CoroutineScope(Dispatchers.Main).launch {
                            CustomDialogBuilder.hideProgress()
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Failed to add receipt details: ${exception.message}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error during initPrinter: ${e.message}")
        }
    }
}

// Other helper functions like stopPrinting or addReceiptDetails can remain here if shared.



suspend fun addReceiptDetails(sharedViewModel: SharedViewModel, context: Context,customer: Boolean = false, objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener) {
    // Create an instance of ReceiptBuilder
    val receiptBuilder = ReceiptBuilder()

    // Switch to IO context for background processing
    withContext(Dispatchers.IO) {
        // Create the receipt using payment details
        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )

        // Generate the receipt
        val receipt = receiptBuilder.createReceipt(context,customer, sharedViewModel,paymentServiceTxnDetails)
        val labelList: List<String> = receipt.fields.map { it.label.toString() }
        val descriptionList: List<String> = receipt.fields.map { it.description.toString() }
        val aligment: List<String> = receipt.fields.map { it.alignment.toString() }
        // Proceed if the receipt was created successfully
        if (receipt != null) {
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

            val alignmentText: List<Int> = receipt.fields.map { field ->
                when (field.alignment) {
                    ReceiptBuilder.Alignment.LEFT -> 0
                    ReceiptBuilder.Alignment.CENTER -> 1
                    ReceiptBuilder.Alignment.RIGHT -> 2
                    ReceiptBuilder.Alignment.NONE -> -1
                    else -> 0 // Default to left alignment if no match
                }
            }

            // Extract the alignment for the barcode
            val alignment: Int = receipt.fields.firstOrNull { it.label == "QR CODE" }?.let { field ->
                when (field.alignment) {
                    ReceiptBuilder.Alignment.LEFT -> 0
                    ReceiptBuilder.Alignment.CENTER -> 1
                    ReceiptBuilder.Alignment.RIGHT -> 2
                    else -> -1 // Default or error value
                }
            } ?: -1 // Default or error value if no barcode field is found

            val fontsize: List<Int> = receipt.fields.map { field ->
                when (field.fontsize) {
                    ReceiptBuilder.FontSize.Small -> 24
                    ReceiptBuilder.FontSize.Medium -> 28
                    ReceiptBuilder.FontSize.Big -> 32
                    else -> 1 // Default to left alignment if no match
                }
            }

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
            printReceipt(0, sharedViewModel, context, customer, sharedViewModel.objRootAppPaymentDetail, txnDBRepository)
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















