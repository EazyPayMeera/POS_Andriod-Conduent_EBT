package com.eazypaytech.pos.core.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.model.emv.CardBrand
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.securityframework.database.entity.UserManagementEntity
import com.eazypaytech.pos.R
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.domain.model.Symbol
import com.eazypaytech.pos.core.utils.language.UiLanguage
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.google.gson.Gson
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow

/**
 * Calculates the final transaction amount by adding all charge components.
 *
 * @param transactionAmount Base transaction amount
 * @param tipAmount Tip amount added to transaction
 * @param vat VAT/tax applied to transaction
 * @param serviceCharge Additional service charge
 * @return Total computed amount (sum of all inputs)
 */
fun calculateTotalAmount(transactionAmount: Double, tipAmount: Double, vat: Double, serviceCharge: Double): Double {
    return transactionAmount + tipAmount + vat + serviceCharge
}

/**
 * Converts a formatted amount string into a Double value.
 *
 * The input string is cleaned and formatted before conversion.
 * If parsing fails, returns 0.00 as fallback.
 *
 * @param amount Input amount string (may contain symbols/formatting)
 * @param decimalPlaces Number of decimal places to consider
 * @return Parsed Double value or 0.00 if invalid
 */
fun transformToAmountDouble(amount: String, decimalPlaces: Int = 2): Double {
    return formatAmount(amount,decimalPlaces, Symbol(type = Symbol.Type.NONE),withSeparator=false).toDoubleOrNull()?:0.00
}

/**
 * Formats a Double amount into a string representation.
 *
 * Applies decimal precision, optional currency symbol,
 * and thousand separators based on configuration.
 *
 * @param amount Numeric amount to format
 * @param decimalPlaces Number of decimal places
 * @param symbol Optional currency symbol configuration
 * @param withSeparator Whether to include thousand separators
 * @return Formatted amount string
 */
fun formatAmount(amount: Double, decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String {
    return formatAmount("%.${decimalPlaces}f".format(amount),decimalPlaces,symbol,withSeparator)
}

/**
 * Formats a Double amount into a string representation.
 *
 * Applies decimal precision, optional currency symbol,
 * and thousand separators based on configuration.
 *
 * @param amount Numeric amount to format
 * @param decimalPlaces Number of decimal places
 * @param symbol Optional currency symbol configuration
 * @param withSeparator Whether to include thousand separators
 * @return Formatted amount string
 */
fun formatAmount(input: String, decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String {
    try {
        val amount  = removeNonDigits(input).take(12)
        val dAmount: Double = amount.toDoubleOrNull()?:0.00
        val currency = symbol?.get()?:""
        val separator : String = if(withSeparator) "," else ""
        val spaceChar : String = if(symbol?.noSpace==true || symbol?.type==Symbol.Type.NONE) "" else " "
        return when(symbol?.position) {
            Symbol.Position.START -> "$currency$spaceChar%${separator}.${decimalPlaces}f".format(Locale.ENGLISH, dAmount / 10.0.pow(decimalPlaces))
            Symbol.Position.END -> "%.${decimalPlaces}f$spaceChar$currency".format(Locale.ENGLISH, dAmount / 10.0.pow(decimalPlaces))
            else -> "%.${decimalPlaces}f".format(Locale.ENGLISH, dAmount / 10.0.pow(decimalPlaces))
        }
    } catch (e: Exception) {
        //AppLogger.e(AppLogger.MODULE.APP_UI, e.message.toString())
    }
    return ""
}

/**
 * Extension function to format a Double into a decimal string.
 *
 * Applies symbol formatting and decimal precision.
 * Uses default value 0.00 if null.
 *
 * @param decimalPlaces Number of decimal places
 * @param symbol Optional currency symbol configuration
 * @param withSeparator Whether to include thousand separators
 * @return Formatted string representation of amount
 */
fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

/**
 * Extension function to format a Double into currency-style string.
 *
 * Includes optional currency symbol and separators.
 *
 * @param decimalPlaces Number of decimal places
 * @param symbol Optional currency symbol configuration
 * @param withSeparator Whether to include thousand separators
 * @return Formatted amount string
 */
fun Double?.toAmountFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

/**
 * Extension function to format a String amount into currency format.
 *
 * Converts string to numeric format internally before formatting.
 *
 * @param decimalPlaces Number of decimal places
 * @param symbol Optional currency symbol configuration
 * @param withSeparator Whether to include thousand separators
 * @return Formatted amount string
 */
fun String?.toAmountFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String
{
    return formatAmount(this?:"0.00",decimalPlaces,symbol,withSeparator)
}

/**
 * Formats a Double value as percentage string.
 *
 * Appends percent symbol and applies formatting rules.
 *
 * @param decimalPlaces Number of decimal places
 * @param noSpace Whether to remove space before symbol
 * @param withSeparator Whether to include thousand separators
 * @return Percentage formatted string
 */
fun Double?.toPercentFormat(decimalPlaces: Int = 2, noSpace: Boolean = true, withSeparator: Boolean = true): String
{
    var symbol: Symbol?=Symbol(type = Symbol.Type.PERCENT, noSpace = noSpace, position = Symbol.Position.END)
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

/**
 * Multiplies two numeric values.
 *
 * @param amount Base amount
 * @param tip Multiplier value (e.g., tip percentage)
 * @return Result of multiplication
 */
fun multiplyValues(amount: Double, tip: Double): Double {
    return amount * tip
}

/**
 * Removes all non-numeric characters from a string.
 *
 * Useful for cleaning formatted currency inputs.
 *
 * @param input Raw string input
 * @return String containing only digits
 */
fun removeNonDigits(input: String): String {
    val re = Regex("[^0-9]")
    return re.replace(input, "")
}

/**
 * Creates a VisualTransformation for formatted amount input fields.
 *
 * Automatically formats input text using currency rules while typing.
 *
 * @param symbol Optional currency symbol configuration
 * @param decimalPlaces Decimal precision
 * @return VisualTransformation for Compose TextField
 */
fun createAmountTransformation(symbol: Symbol?=Symbol(),decimalPlaces: Int=2): VisualTransformation {
    return object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            // Format the text using your formatAmount function
            val formatted = formatAmount(text.text,symbol=symbol, decimalPlaces=decimalPlaces)

            // Define the offset mapping
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = text.length
            }

            // Return the TransformedText object
            return TransformedText(AnnotatedString(formatted), offsetMapping)
        }
    }
}

/**
 * Converts ObjRootAppPaymentDetails model to TxnEntity.
 *
 * Uses JSON serialization as an intermediate transformation layer.
 *
 * @param objRootAppPaymentDetails Source object
 * @return Converted TxnEntity object
 */
fun convertObjRootToTxnEntity(objRootAppPaymentDetails: ObjRootAppPaymentDetails): TxnEntity {
    val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON
    return Gson().fromJson(json, TxnEntity::class.java) // Convert JSON to TxnEntity
}

/**
 * Converts ObjRootAppPaymentDetails model to UserManagementEntity.
 *
 * Uses Gson-based serialization mapping.
 *
 * @param objRootAppPaymentDetails Source object
 * @return Converted UserManagementEntity object
 */
fun convertObjRootToUserManagementEntity(objRootAppPaymentDetails: ObjRootAppPaymentDetails): UserManagementEntity {
    val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON
    return Gson().fromJson(json, UserManagementEntity::class.java) // Convert JSON to TxnEntity
}

/**
 * Returns current system date and time in specified format.
 *
 * @param format Desired output date format
 * @return Formatted current date-time string
 */
fun getCurrentDateTime(format : String?=AppConstants.DEFAULT_DATE_TIME_FORMAT): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date())
}

/**
 * Converts a date-time string from one format to another.
 *
 * @param inputDateTime Input date-time string
 * @param inputFormat Format of input string
 * @param outputFormat Desired output format
 * @return Converted formatted date string or empty if error
 */
fun convertDateTime(inputDateTime: String?=null, inputFormat : String?=AppConstants.DEFAULT_DATE_TIME_FORMAT, outputFormat : String?=null): String {
    val idf = SimpleDateFormat(inputFormat, Locale.getDefault())
    val odf = SimpleDateFormat(outputFormat, Locale.getDefault())
    return try {
        val date = idf.parse(inputDateTime?:"")
        odf.format(date?:"")
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * Converts receipt-specific timestamp into readable format.
 *
 * Injects current year and supports AM/PM formatting adjustments.
 *
 * @param inputDateTime Raw receipt timestamp
 * @param inputFormat Input format (default EMV format)
 * @param outputFormat Output format
 * @return Formatted receipt date-time string or "-"
 */
fun convertReceiptDateTime(
    inputDateTime: String? = null,
    inputFormat: String? = AppConstants.DEFAULT_DATE_TIME_FORMAT,
    outputFormat: String? = null
): String {

    if (inputDateTime.isNullOrBlank() || outputFormat.isNullOrBlank()) {
        return "-"
    }

    return try {
        val calendar = Calendar.getInstance()

        val idf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val parsedDate = idf.parse(inputDateTime)

        if (parsedDate != null) {
            val tempCal = Calendar.getInstance()
            tempCal.time = parsedDate

            // 🔥 inject current year
            tempCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR))

            val odf = SimpleDateFormat(outputFormat, Locale.getDefault())
            var result = odf.format(tempCal.time)

            if (outputFormat.contains("a")) {
                result = result.lowercase()
            }

            result
        } else {
            "-"
        }

    } catch (e: Exception) {
        e.printStackTrace()
        inputDateTime
    }
}

/**
 * Navigates to a route and clears back stack up to start destination.
 *
 * @param route Destination route
 */
fun NavController.navigateAndClean(route: String) {
    navigate(route = route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
    graph.setStartDestination(route)
}

/**
 * Navigates to a route and clears navigation back stack.
 *
 * @param route Destination route
 */
fun NavHostController.navigateAndClean(route: String) {
    navigate(route = route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
    graph.setStartDestination(route)
}

/**
 * Maps EMV response code to internal transaction status.
 *
 * @param responseCode EMV response code
 * @return Corresponding TxnStatus
 */
fun emvStatusToTransStatus(responseCode: String?) : TxnStatus
{
    return when(responseCode) {
        "00" -> TxnStatus.APPROVED
        else -> TxnStatus.DECLINED
    }
}

/**
 * Converts EMV display message ID to string resource ID.
 *
 * @param displayMsgId EMV display message identifier
 * @return String resource ID or null if unmapped
 */
fun emvMsgIdToStringId(displayMsgId: EmvServiceResult.DisplayMsgId?) : Int?
{
    val id : Int? =
        when(displayMsgId) {
            EmvServiceResult.DisplayMsgId.DISPLAY_BALANCE -> R.string.amount
            EmvServiceResult.DisplayMsgId.CARD_INSERTED -> R.string.emv_msg_id_card_inserted
            EmvServiceResult.DisplayMsgId.CARD_TAPPED -> R.string.emv_msg_id_card_tapped
            EmvServiceResult.DisplayMsgId.CARD_SWIPED -> R.string.emv_msg_id_card_swiped
            EmvServiceResult.DisplayMsgId.CARD_READ_OK -> R.string.emv_msg_id_card_read_ok
            EmvServiceResult.DisplayMsgId.REMOVE_CARD -> R.string.emv_msg_id_remove_card
            EmvServiceResult.DisplayMsgId.USE_CONTACT_IC_CARD -> R.string.emv_msg_id_use_ic_card
            EmvServiceResult.DisplayMsgId.USE_MAG_STRIPE -> R.string.emv_msg_id_use_magstripe
            EmvServiceResult.DisplayMsgId.INSERT_SWIPE_OR_TRY_ANOTHER_CARD -> R.string.emv_msg_id_insert_swipe_try_another
            EmvServiceResult.DisplayMsgId.SEE_PHONE_AND_PRESENT_CARD_AGAIN -> R.string.emv_msg_id_see_phone
            EmvServiceResult.DisplayMsgId.NEED_SIGNATURE -> R.string.emv_msg_id_need_signature
            EmvServiceResult.DisplayMsgId.END_APPLICATION -> R.string.emv_msg_id_end_application
            EmvServiceResult.DisplayMsgId.TAP_CARD_AGAIN -> R.string.emv_msg_id_tap_again
            EmvServiceResult.DisplayMsgId.APP_BLOCKED -> R.string.emv_msg_id_app_blocked
            EmvServiceResult.DisplayMsgId.TERMINATED -> R.string.emv_msg_id_terminated
            EmvServiceResult.DisplayMsgId.ERR_CARD_READ -> R.string.emv_msg_id_err_card_read
            EmvServiceResult.DisplayMsgId.ERR_PROCESSING -> R.string.emv_msg_id_err_processing
            EmvServiceResult.DisplayMsgId.ERR_LOAD_CALLBACK -> R.string.emv_msg_id_err_load_callback
            EmvServiceResult.DisplayMsgId.ERR_ICS_PARAM_NOT_FOUND -> R.string.emv_msg_id_err_ics_param_not_found
            EmvServiceResult.DisplayMsgId.ERR_KERNEL -> R.string.emv_msg_id_err_kernel
            EmvServiceResult.DisplayMsgId.ERR_PIN_LENGTH -> R.string.emv_msg_id_err_pin_length
            EmvServiceResult.DisplayMsgId.ERR_MULTI_CARD -> R.string.emv_msg_id_err_multiple_cards
            EmvServiceResult.DisplayMsgId.ERR_CHECK_CARD -> R.string.emv_msg_id_err_check_card
            EmvServiceResult.DisplayMsgId.ERR_AID_PARAM_NOT_FIND -> R.string.emv_msg_id_err_aid_param_not_found
            EmvServiceResult.DisplayMsgId.ERR_CAPK_PARAM_NOT_FIND -> R.string.emv_msg_id_err_capk_param_not_found
            EmvServiceResult.DisplayMsgId.ERR_GET_KERNEL_DATA_FAILED -> R.string.emv_msg_id_err_get_kernel_data
            EmvServiceResult.DisplayMsgId.ERR_QPBOC_APPLICATION -> R.string.emv_msg_id_err_qpboc_app
            EmvServiceResult.DisplayMsgId.ERR_QPBOC_FDDA_FAILED -> R.string.emv_msg_id_err_qpboc_fdda_failed
            EmvServiceResult.DisplayMsgId.ERR_PURE_ELE_CASH_CARD_NOT_ALLOW_ONLINE_TRANS -> R.string.emv_msg_id_err_pure_cash_card_no_online
            EmvServiceResult.DisplayMsgId.PROCESSING_ONLINE -> R.string.emv_msg_id_processing_online
            EmvServiceResult.DisplayMsgId.APPROVED_ONLINE -> R.string.emv_msg_id_approved_online
            EmvServiceResult.DisplayMsgId.DECLINED_ONLINE -> R.string.emv_msg_id_declined_online
            EmvServiceResult.DisplayMsgId.APPROVED_OFFLINE -> R.string.emv_msg_id_approved_offline
            EmvServiceResult.DisplayMsgId.DECLINED_OFFLINE -> R.string.emv_msg_id_declined_offline
            EmvServiceResult.DisplayMsgId.CANCELED -> R.string.emv_msg_id_cancelled
            EmvServiceResult.DisplayMsgId.TIMEOUT -> R.string.emv_msg_id_timeout
            EmvServiceResult.DisplayMsgId.CARD_BLOCKED -> R.string.emv_msg_id_card_blocked
            EmvServiceResult.DisplayMsgId.NO_EMV_APPS -> R.string.emv_msg_id_no_emv_apps
            EmvServiceResult.DisplayMsgId.APP_SELECTION_FAILED -> R.string.emv_msg_id_app_selection_failed
            EmvServiceResult.DisplayMsgId.TRY_ANOTHER_INTERFACE -> R.string.emv_msg_id_try_another_interface
            EmvServiceResult.DisplayMsgId.INVALID_ICC_CARD -> R.string.emv_msg_id_invalid_icc_card
            EmvServiceResult.DisplayMsgId.RETRY -> R.string.emv_msg_id_retry
            EmvServiceResult.DisplayMsgId.CARD_REMOVED -> R.string.emv_msg_id_card_removed
            EmvServiceResult.DisplayMsgId.ISSUER_SCRIPT_UPDATE_SUCCESSFUL -> R.string.emv_msg_id_issuer_script_update_successful
            EmvServiceResult.DisplayMsgId.ISSUER_SCRIPT_UPDATE_FAILED -> R.string.emv_msg_id_issuer_script_update_failed
            EmvServiceResult.DisplayMsgId.MAX_CHIP_RETRY -> R.string.emv_msg_id_max_retry

            else -> null
        }

    return id
}

/**
 * Updates application locale at runtime.
 *
 * @param context Application context
 * @param language Target UI language
 */
fun setUiLanguage(context: Context, language: UiLanguage) {
    val config = context.resources.configuration
    val locale = Locale(language.languageCode)
    Locale.setDefault(locale)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

}



/**
 * Hides the software keyboard when invoked.
 *
 * Typically used in Compose side effects.
 */
@Composable
fun HideSoftKeyboard() {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
    }
}

/**
 * Returns icon resource based on transaction status and flags.
 *
 * Handles void, refund, capture, approved, declined states.
 *
 * @param objRootAppPaymentDetails Transaction object
 * @return Drawable resource ID
 */
@Composable
fun getTxnStatusIconId(objRootAppPaymentDetails : ObjRootAppPaymentDetails) : Int
{
    return if(objRootAppPaymentDetails.isVoided==true)
        R.drawable.approved
    else if(objRootAppPaymentDetails.isRefunded==true)
            R.drawable.refunded
    else if(objRootAppPaymentDetails.isCaptured==true)
        R.drawable.captured
    else if(objRootAppPaymentDetails.txnStatus == TxnStatus.APPROVED)
        when(objRootAppPaymentDetails.txnType) {
            TxnType.PURCHASE_CASHBACK -> R.drawable.approved
            else -> R.drawable.approved
        }
    else if(objRootAppPaymentDetails.txnStatus == TxnStatus.DECLINED)
        R.drawable.declined
    else
        R.drawable.error
}

/**
 * Maps transaction type to string resource ID.
 *
 * @param txnType Transaction type
 * @return String resource ID
 */
fun getTxnTypeStringId(txnType: TxnType?): Int {
    return when (txnType) {
        TxnType.PURCHASE_CASHBACK -> R.string.ebt_purchase_cashback
        TxnType.BALANCE_ENQUIRY_SNAP -> R.string.ebt_bal_inquiry
        TxnType.VOUCHER_CLEAR -> R.string.ebt_voucher_clear
        TxnType.VOUCHER_RETURN -> R.string.ebt_voucher_return
        TxnType.VOID_LAST -> R.string.ebt_void_last
        TxnType.FOOD_PURCHASE -> R.string.ebt_food_purchase
        TxnType.FOODSTAMP_RETURN -> R.string.ebt_foodstamp_return
        TxnType.E_VOUCHER -> R.string.ebt_e_voucher
        TxnType.CASH_PURCHASE -> R.string.ebt_cash_purchase
        TxnType.CASH_WITHDRAWAL -> R.string.receipt_txntype_cash_withdrawal
        TxnType.BALANCE_ENQUIRY_CASH -> R.string.receipt_txntype_balance_inquiry_cash
        else -> R.string.empty
    }
}

/**
 * Maps transaction status to string resource ID.
 *
 * @param txnStatus Transaction status
 * @return String resource ID
 */
fun getTxnStatusStringId(txnStatus: TxnStatus?) : Int
{
    return when(txnStatus) {
        TxnStatus.INITIATED -> R.string.status_initiated
        TxnStatus.APPROVED -> R.string.status_approved
        TxnStatus.DECLINED -> R.string.status_declined
        TxnStatus.ERROR -> R.string.status_error
        TxnStatus.TERMINATED -> R.string.status_terminated
        TxnStatus.REVERSED -> R.string.status_reversed
        TxnStatus.VOIDED -> R.string.status_voided
        TxnStatus.REFUNDED -> R.string.status_refunded
        TxnStatus.CAPTURED -> R.string.status_captured
        else -> R.string.empty
    }
}

/**
 * Returns string resource for balance inquiry type.
 *
 * @param txnType Transaction type
 * @return String resource ID
 */
fun getBalInquiryStringId(txnType: TxnType?): Int {
    return when (txnType) {
        TxnType.BALANCE_ENQUIRY_SNAP -> R.string.snap_bal_available
        TxnType.BALANCE_ENQUIRY_CASH -> R.string.cash_bal_available
        else -> R.string.empty
    }
}


/**
 * Converts string representation of card brand into enum.
 *
 * @return CardBrand enum value
 */
fun String.toCardBrand() : CardBrand
{
    return when(this)
    {
        CardBrand.VISA.name -> CardBrand.VISA
        CardBrand.MASTERCARD.name -> CardBrand.MASTERCARD
        CardBrand.AMEX.name -> CardBrand.AMEX
        CardBrand.DISCOVER.name -> CardBrand.DISCOVER
        CardBrand.DINERS.name -> CardBrand.DINERS
        CardBrand.JCB.name -> CardBrand.JCB
        CardBrand.UPI.name -> CardBrand.UPI
        CardBrand.PURE.name -> CardBrand.PURE
        CardBrand.RUPAY.name -> CardBrand.RUPAY
        CardBrand.MIR.name -> CardBrand.MIR
        else -> CardBrand.UNKNOWN
    }
}

/**
 * Generates a pseudo master password based on user + device + date.
 *
 * Uses hashing and digit extraction to generate 6-digit OTP-like value.
 *
 * @param user Username or identifier
 * @param sharedViewModel Shared configuration source
 * @return Generated 6-digit password string
 */
fun generateMasterPassword(user : String?, sharedViewModel: SharedViewModel) : String
{
    val tid = sharedViewModel.objPosConfig?.procId?:""
    val mid = sharedViewModel.objPosConfig?.merchantId?:""
    val deviceSN = sharedViewModel.objPosConfig?.deviceSN?.replace("-","")?:""

    // Step 1: Get today's date as a string (YYYYMMDD)
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = dateFormat.format(Date())

    // Step 2: Create a unique seed
    val seed = today + (user?:"") + tid + mid + deviceSN

    // Step 3: Generate SHA-256 hash from the seed
    val hash = simpleHash(simpleHash(seed))

    // Step 4: Convert hash to a 6-digit OTP
    val password = hash.filter { it.isDigit() }.take(6).padEnd(6, '0') // Take last 6 numeric digits & pad 0 if required

    Log.d("PASSWORD","OTP for user '$user' is '$password'")

    return password  // Pads with '0' if needed
}

/**
 * Generates a custom non-cryptographic hash from input string.
 *
 * Used internally for lightweight fingerprint generation.
 *
 * @param input Input string
 * @return Hex encoded hash string
 */
fun simpleHash(input: String): String {
    val hashParts = LongArray(10) { 0L }
    val prime1 = 0x01000193L
    val prime2 = 0x811C9DC5L

    for (i in input.indices) {
        val charCode = input[i].code.toLong()
        val index = (i * prime1 % 10).toInt()
        hashParts[index] = ((hashParts[index] xor (charCode * prime2)) and 0xFFFFFFFFL)
        hashParts[index] = ((hashParts[index] * prime1 + prime2) and 0xFFFFFFFFL)
    }

    for (i in hashParts.indices) {
        hashParts[i] = ((hashParts[i] xor ((hashParts[(i + 3) % 10] ushr 5) or (hashParts[(i + 7) % 10] shl 11))) and 0xFFFFFFFFL)
    }

    return hashParts.joinToString("") { "%08x".format(it) }
}

/**
 * Generates SHA-256 hash of input string.
 *
 * @param input Input string
 * @return Hex encoded SHA-256 hash
 */
fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) } // Convert bytes to hex string
}
