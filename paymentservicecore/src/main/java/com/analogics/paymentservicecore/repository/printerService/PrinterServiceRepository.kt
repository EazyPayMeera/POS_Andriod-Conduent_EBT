import android.content.Context
import android.util.Log
import com.analogics.paymentservicecore.listeners.requestListener.PrinterRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.tpaymentcore.handler.PrinterHandler
import com.analogics.tpaymentcore.listener.IPrinterHandlerListener
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor() : PrinterRequestListener, IPrinterHandlerListener {

    private val TAG = "PrinterServiceRepo"

    lateinit var iPrinterResultProviderListener: IPrinterResultProviderListener
    lateinit var iPrinterHandlerListener: IPrinterHandlerListener

    override suspend fun initPrinter(
        context: Context,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        Log.d(TAG, "Initializing printer...")
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            PrinterHandler.initPrinter(context, this) // Pass this as the listener
            Log.d(TAG, "Printer initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize printer: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override fun onPrinterRespHandler(uiData: String) {
        Log.d(TAG, "Received printer response: $uiData")
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS") {
            Log.d(TAG, "Printer response is SUCCESS.")
            iPrinterResultProviderListener.onSuccess(true)
        } else {
            Log.d(TAG, "Printer response is FAILURE.")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }
}
