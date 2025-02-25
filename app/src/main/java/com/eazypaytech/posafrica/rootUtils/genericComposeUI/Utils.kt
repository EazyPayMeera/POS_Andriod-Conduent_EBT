

package com.eazypaytech.posafrica.rootUtils.genericComposeUI

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.logger.AppLogger
import com.eazypaytech.paymentservicecore.model.emv.CardBrand
import com.eazypaytech.paymentservicecore.model.emv.CardEntryMode
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult
import com.eazypaytech.paymentservicecore.models.Acquirer
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.securityframework.database.entity.UserManagementEntity
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootModel.Symbol
import com.eazypaytech.posafrica.rootModel.UiLanguage
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.pow

fun calculateTotalAmount(transactionAmount: Double, tipAmount: Double, vat: Double, serviceCharge: Double): Double {
    return transactionAmount + tipAmount + vat + serviceCharge
}

fun transformToAmountDouble(amount: String, decimalPlaces: Int = 2): Double {
    return formatAmount(amount,decimalPlaces, Symbol(type = Symbol.Type.NONE),withSeparator=false).toDoubleOrNull()?:0.00
}

fun formatAmount(amount: Double, decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String {
    return formatAmount("%.${decimalPlaces}f".format(amount),decimalPlaces,symbol,withSeparator)
}

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
        AppLogger.e(AppLogger.MODULE.APP_UI, e.message.toString())
    }
    return ""
}

fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

fun Double?.toAmountFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

fun String?.toAmountFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String
{
    return formatAmount(this?:"0.00",decimalPlaces,symbol,withSeparator)
}

fun Double?.toPercentFormat(decimalPlaces: Int = 2, noSpace: Boolean = true, withSeparator: Boolean = true): String
{
    var symbol: Symbol?=Symbol(type = Symbol.Type.PERCENT, noSpace = noSpace, position = Symbol.Position.END)
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

fun multiplyValues(amount: Double, tip: Double): Double {
    return amount * tip
}

fun removeNonDigits(input: String): String {
    val re = Regex("[^0-9]")
    return re.replace(input, "")
}

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


fun convertObjRootToTxnEntity(objRootAppPaymentDetails: ObjRootAppPaymentDetails): TxnEntity {
    val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON
    return Gson().fromJson(json, TxnEntity::class.java) // Convert JSON to TxnEntity
}

fun convertBatchToBatchEntity(batchEntity: BatchEntity): BatchEntity {
    val json = Gson().toJson(batchEntity) // Convert ObjRootAppPaymentDetails to JSON
    return Gson().fromJson(json, BatchEntity::class.java) // Convert JSON to TxnEntity
}

fun convertObjRootToUserManagementEntity(objRootAppPaymentDetails: ObjRootAppPaymentDetails): UserManagementEntity {
    val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON
    return Gson().fromJson(json, UserManagementEntity::class.java) // Convert JSON to TxnEntity
}


fun getCurrentDateTime(format : String?=AppConstants.DEFAULT_DATE_TIME_FORMAT): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date())
}

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

fun NavController.navigateAndClean(route: String) {
    navigate(route = route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
    graph.setStartDestination(route)
}

fun NavHostController.navigateAndClean(route: String) {
    navigate(route = route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
    graph.setStartDestination(route)
}

fun navigateToDashboard(navHostController: NavHostController) {
    navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
}



fun getBitmapBytes(bitmap: Bitmap): ByteArray? {
    var imageData: ByteArray? = null
    try {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        imageData = baos.toByteArray()
    } catch (e: Exception) {
        // TODO: handle exception
        e.printStackTrace()
        return null
    }
    return imageData
}

fun getLogoBitmap(context: Context, id: Int): Bitmap {
    val draw = context.resources.getDrawable(id) as BitmapDrawable
    val bitmap = draw.bitmap
    return bitmap
}

fun emvStatusToTransStatus(emvTransStatus: Any?) : TxnStatus
{
    return when(emvTransStatus) {
        EmvServiceResult.TransStatus.APPROVED_ONLINE, EmvServiceResult.TransStatus.APPROVED_OFFLINE -> TxnStatus.APPROVED
        EmvServiceResult.TransStatus.DECLINED_ONLINE,EmvServiceResult.TransStatus.DECLINED_OFFLINE -> TxnStatus.DECLINED
        EmvServiceResult.TransStatus.INITIATED -> TxnStatus.INITIATED
        EmvServiceResult.TransStatus.TERMINATED -> TxnStatus.TERMINATED
        else -> TxnStatus.ERROR
    }
}

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

            else -> null
        }

    return id
}

@Composable
fun getEmvMsgIdString(displayMsgId: EmvServiceResult.DisplayMsgId) : String
{
    val id : Int? = emvMsgIdToStringId(displayMsgId)
    return if(id!=null) stringResource(id) else ""
}

fun getIsoResponseCodeStringId(responseCode: String?) : Int?
{
    val id : Int? =
    when(responseCode) {
        BuilderConstants.ISO_RESP_CODE_APPROVED -> R.string.iso_resp_code_approved
        BuilderConstants.ISO_RESP_CODE_CALL_ISSUER -> R.string.iso_resp_code_call_issuer
        BuilderConstants.ISO_RESP_CODE_CALL_ISSUER_SPECIAL -> R.string.iso_resp_code_call_issuer_special
        BuilderConstants.ISO_RESP_CODE_INVALID_MERCHANT -> R.string.iso_resp_code_invalid_merchant
        BuilderConstants.ISO_RESP_CODE_DECLINED_PICKUP_CARD -> R.string.iso_resp_code_declined_pickup_card
        BuilderConstants.ISO_RESP_CODE_DO_NOT_HONOR -> R.string.iso_resp_code_do_not_honor
        BuilderConstants.ISO_RESP_CODE_ERROR_MERCHANT -> R.string.iso_resp_code_error_merchant
        BuilderConstants.ISO_RESP_CODE_PICKUP_SPECIAL -> R.string.iso_resp_code_pickup_special
        BuilderConstants.ISO_RESP_CODE_APPROVED_VERIFY_ID -> R.string.iso_resp_code_approved_verify_id
        BuilderConstants.ISO_RESP_CODE_APPROVED_PARTIAL -> R.string.iso_resp_code_approved_partial
        BuilderConstants.ISO_RESP_CODE_INVALID_TXN -> R.string.iso_resp_code_invalid_txn
        BuilderConstants.ISO_RESP_CODE_INVALID_AMOUNT -> R.string.iso_resp_code_invalid_amount
        BuilderConstants.ISO_RESP_CODE_DECLINED_INVALID_CARD -> R.string.iso_resp_code_declined_invalid_card
        BuilderConstants.ISO_RESP_CODE_DECLINED_INVALID_ISSUER -> R.string.iso_resp_code_declined_invalid_issuer
        BuilderConstants.ISO_RESP_CODE_DECLINED_CUSTOMER_CANCEL -> R.string.iso_resp_code_declined_customer_cancel
        BuilderConstants.ISO_RESP_CODE_DECLINED_REENTER_TXN -> R.string.iso_resp_code_declined_reenter_txn
        BuilderConstants.ISO_RESP_CODE_INVALID_RESPONSE -> R.string.iso_resp_code_invalid_response
        BuilderConstants.ISO_RESP_CODE_RETRY_NO_ACTION -> R.string.iso_resp_code_retry_no_action
        BuilderConstants.ISO_RESP_CODE_RETRY_SUSPECTED_MALFUNCTION -> R.string.iso_resp_code_retry_suspected_malfunction
        BuilderConstants.ISO_RESP_CODE_RETRY_UNABLE_LOCATE_RECORD -> R.string.iso_resp_code_retry_unable_locate_record
        BuilderConstants.ISO_RESP_CODE_RETRY_FILE_UPDATE_FIELD_ERROR -> R.string.iso_resp_code_retry_file_update_field_error
        BuilderConstants.ISO_RESP_CODE_RETRY_RECORD_ALREADY_EXISTS -> R.string.iso_resp_code_retry_record_already_exists
        BuilderConstants.ISO_RESP_CODE_RETRY_FILE_UPDATE_NOT_SUCCESSFUL -> R.string.iso_resp_code_retry_file_update_not_successful
        BuilderConstants.ISO_RESP_CODE_FORMAT_ERROR -> R.string.iso_resp_code_format_error
        BuilderConstants.ISO_RESP_CODE_RETRY_UNSUPPORTED_BANK -> R.string.iso_resp_code_retry_unsupported_bank
        BuilderConstants.ISO_RESP_CODE_RETRY_PARTIAL_REVERSAL -> R.string.iso_resp_code_retry_partial_reversal
        BuilderConstants.ISO_RESP_CODE_EXPIRED_CARD -> R.string.iso_resp_code_expired_card
        BuilderConstants.ISO_RESP_CODE_DECLINED_SUSPECTED_FRAUD -> R.string.iso_resp_code_declined_suspected_fraud
        BuilderConstants.ISO_RESP_CODE_RESTRICTED_CARD -> R.string.iso_resp_code_restricted_card
        BuilderConstants.ISO_RESP_CODE_EXCESS_PIN_TRIES -> R.string.iso_resp_code_excess_pin_tries
        BuilderConstants.ISO_RESP_CODE_DECLINED_NO_CREDIT_ACCOUNT -> R.string.iso_resp_code_declined_no_credit_account
        BuilderConstants.ISO_RESP_CODE_DECLINED_UNSUPPORTED_FUNCTION -> R.string.iso_resp_code_declined_unsupported_function
        BuilderConstants.ISO_RESP_CODE_PICKUP_LOST_CARD -> R.string.iso_resp_code_pickup_lost_card
        BuilderConstants.ISO_RESP_CODE_DECLINED_NO_UNIVERSAL_ACCOUNT -> R.string.iso_resp_code_declined_no_universal_account
        BuilderConstants.ISO_RESP_CODE_PICKUP_STOLEN_CARD -> R.string.iso_resp_code_pickup_stolen_card
        BuilderConstants.ISO_RESP_CODE_DECLINED_INSUFFICIENT_FUNDS -> R.string.iso_resp_code_declined_insufficient_funds
        BuilderConstants.ISO_RESP_CODE_NO_CHECKING_ACCOUNT -> R.string.iso_resp_code_no_checking_account
        BuilderConstants.ISO_RESP_CODE_NO_SAVINGS_ACCOUNT -> R.string.iso_resp_code_no_savings_account
        BuilderConstants.ISO_RESP_CODE_EXPIRED_CARD_CHECK -> R.string.iso_resp_code_expired_card_check
        BuilderConstants.ISO_RESP_CODE_INCORRECT_PIN -> R.string.iso_resp_code_incorrect_pin
        BuilderConstants.ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_CARDHOLDER -> R.string.iso_resp_code_declined_not_permitted_to_cardholder
        BuilderConstants.ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_TERMINAL -> R.string.iso_resp_code_declined_not_permitted_to_terminal
        BuilderConstants.ISO_RESP_CODE_DECLINED_SUSPECTED_FRAUD_ALT -> R.string.iso_resp_code_declined_suspected_fraud_alt
        BuilderConstants.ISO_RESP_CODE_CONTACT_ACQUIRER_DECLINE -> R.string.iso_resp_code_contact_acquirer_decline
        BuilderConstants.ISO_RESP_CODE_EXCEEDS_WITHDRAWAL_LIMIT -> R.string.iso_resp_code_exceeds_withdrawal_limit
        BuilderConstants.ISO_RESP_CODE_RESTRICTED_CARD_CAPTURE -> R.string.iso_resp_code_restricted_card_capture
        BuilderConstants.ISO_RESP_CODE_SECURITY_VIOLATION -> R.string.iso_resp_code_security_violation
        BuilderConstants.ISO_RESP_CODE_RETRY_AML_REQUIREMENT -> R.string.iso_resp_code_retry_aml_requirement
        BuilderConstants.ISO_RESP_CODE_EXCEEDS_WITHDRAWAL_FREQUENCY -> R.string.iso_resp_code_exceeds_withdrawal_frequency
        BuilderConstants.ISO_RESP_CODE_DECLINED_CONTACT_ACQUIRER -> R.string.iso_resp_code_declined_contact_acquirer
        BuilderConstants.ISO_RESP_CODE_PICKUP_HARD_CAPTURE -> R.string.iso_resp_code_pickup_hard_capture
        BuilderConstants.ISO_RESP_CODE_RETRY_ACQUIRER_TIMEOUT -> R.string.iso_resp_code_retry_acquirer_timeout
        BuilderConstants.ISO_RESP_CODE_RETRY_MOBILE_RECORD_NOT_FOUND -> R.string.iso_resp_code_retry_mobile_record_not_found
        BuilderConstants.ISO_RESP_CODE_RETRY_CONTACT_CARD_ISSUER -> R.string.iso_resp_code_retry_contact_card_issuer
        BuilderConstants.ISO_RESP_CODE_RETRY_DEEMED_ACCEPTANCE -> R.string.iso_resp_code_retry_deemed_acceptance
        BuilderConstants.ISO_RESP_CODE_RETRY_ISSUER_RISK_DECLINE -> R.string.iso_resp_code_retry_issuer_risk_decline
        BuilderConstants.ISO_RESP_CODE_EXCEEDED_PIN_TRIES -> R.string.iso_resp_code_exceeded_pin_tries
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_TO_ACCOUNT -> R.string.iso_resp_code_pvt_error_to_account
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_FROM_ACCOUNT -> R.string.iso_resp_code_pvt_error_from_account
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_GENERAL_ACCOUNT -> R.string.iso_resp_code_pvt_error_general_account
        BuilderConstants.ISO_RESP_CODE_TRANSACTION_REVERSED -> R.string.iso_resp_code_transaction_reversed
        BuilderConstants.ISO_RESP_CODE_DUPLICATE_BATCH -> R.string.iso_resp_code_duplicate_batch
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_DOMESTIC_DEBIT -> R.string.iso_resp_code_pvt_error_domestic_debit
        BuilderConstants.ISO_RESP_CODE_TIMEOUT_AT_ISSUER -> R.string.iso_resp_code_timeout_at_issuer
        BuilderConstants.ISO_RESP_CODE_HSM_KEY_ERROR -> R.string.iso_resp_code_hsm_key_error
        BuilderConstants.ISO_RESP_CODE_DECLINED_INVALID_AUTH_LIFE_CYCLE -> R.string.iso_resp_code_declined_invalid_auth_life_cycle
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_ZERO_AMOUNT -> R.string.iso_resp_code_pvt_error_zero_amount
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_PIN_VALIDATION -> R.string.iso_resp_code_pvt_error_pin_validation
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_NO_CASH_BACK -> R.string.iso_resp_code_pvt_error_no_cashback
        BuilderConstants.ISO_RESP_CODE_CRYPTOGRAPHIC_FAILURE -> R.string.iso_resp_code_cryptographic_failure
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_UNACCEPTABLE_PIN -> R.string.iso_resp_code_pvt_error_unacceptable_pin
        BuilderConstants.ISO_RESP_CODE_CUT_OFF_IN_PROCESS -> R.string.iso_resp_code_cut_off_in_process
        BuilderConstants.ISO_RESP_CODE_TRY_AFTER_5MIN_AUTH_SYS_INOPERATIVE -> R.string.iso_resp_code_try_after_5min_auth_sys_inoperative
        BuilderConstants.ISO_RESP_CODE_TRY_AFTER_5MIN_UNABLE_ROUTE -> R.string.iso_resp_code_try_after_5min_unable_route
        BuilderConstants.ISO_RESP_CODE_DECLINED_VIOLATION_OF_LAW -> R.string.iso_resp_code_declined_violation_of_law
        BuilderConstants.ISO_RESP_CODE_PVT_ERROR_DUPLICATE_TRANS -> R.string.iso_resp_code_pvt_error_duplicate_trans
        BuilderConstants.ISO_RESP_CODE_TOTALS_MISMATCH -> R.string.iso_resp_code_totals_mismatch
        BuilderConstants.ISO_RESP_CODE_SYSTEM_ERROR -> R.string.iso_resp_code_system_error
        BuilderConstants.ISO_RESP_CODE_CVV2_FAILURE -> R.string.iso_resp_code_cvv2_failure
        BuilderConstants.ISO_RESP_CODE_OFFLINE_APPROVED_1 -> R.string.iso_resp_code_offline_approved_1
        BuilderConstants.ISO_RESP_CODE_OFFLINE_APPROVED_3 -> R.string.iso_resp_code_offline_approved_3
        BuilderConstants.ISO_RESP_CODE_OFFLINE_DECLINED_1 -> R.string.iso_resp_code_offline_declined_1
        BuilderConstants.ISO_RESP_CODE_OFFLINE_DECLINED_3 -> R.string.iso_resp_code_offline_declined_3
        BuilderConstants.ISO_RESP_CODE_AAC_GENERATED_BY_CHIP -> R.string.iso_resp_code_aac_generated_by_chip
        BuilderConstants.ISO_RESP_CODE_REVERSAL_RESPONSE_CODE -> R.string.iso_resp_code_reversal_response_code
        else -> null
    }

    return id
}

fun getIsoResponseCodeString(context: Context, responseCode: String?) : String
{
    val id : Int? = getIsoResponseCodeStringId(responseCode)
    return if(id!=null) context.resources.getString(id) else ""
}

fun setUiLanguage(context: Context, language: UiLanguage) {
    val config = context.resources.configuration
    val locale = Locale(language.languageCode)
    Locale.setDefault(locale)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

}

fun getAcquirer(objRootAppPaymentDetail : ObjRootAppPaymentDetails?) : Acquirer
{
    return Acquirer.valueOf(objRootAppPaymentDetail?.acquirerName?:"")
}

fun String.splitByIntervals(intervals: List<Int>, charSequence : CharSequence = "-"): String {
    val result = mutableListOf<String>()
    var currentIndex = 0

    // Iterate through the defined intervals
    for (interval in intervals) {
        if (currentIndex >= this.length) break

        // Extract substring based on the current interval
        val part = this.substring(currentIndex, (currentIndex + interval).coerceAtMost(this.length))
        result.add(part)

        // Move the index forward by the current interval
        currentIndex += interval
    }

    // Add any remaining part of the string
    if (currentIndex < this.length) {
        result.add(this.substring(currentIndex))
    }

    return result.joinToString(charSequence)
}


@Composable
fun HideSoftKeyboard() {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
    }
}

@Composable
fun LoadingBar() {
    LinearProgressIndicator()
}

@Composable
fun getTxnStatusIconId(objRootAppPaymentDetails : ObjRootAppPaymentDetails) : Int
{
    return if(objRootAppPaymentDetails.isVoided==true)
        R.drawable.voided
    else if(objRootAppPaymentDetails.isRefunded==true)
            R.drawable.refunded
    else if(objRootAppPaymentDetails.isCaptured==true)
        R.drawable.captured
    else if(objRootAppPaymentDetails.txnStatus == TxnStatus.APPROVED)
        when(objRootAppPaymentDetails.txnType) {
            TxnType.PREAUTH -> R.drawable.authorized
            else -> R.drawable.approved
        }
    else if(objRootAppPaymentDetails.txnStatus == TxnStatus.DECLINED)
        R.drawable.declined
    else
        R.drawable.error
}

fun getTxnTypeStringId(txnType: TxnType?) : Int
{
    return when(txnType) {
        TxnType.PURCHASE -> R.string.purchase
        TxnType.REFUND -> R.string.refund
        TxnType.PREAUTH -> R.string.pre_auth
        TxnType.AUTHCAP -> R.string.auth_capture
        TxnType.VOID -> R.string.void_trans
        TxnType.TXNLIST -> R.string.transactions
        else -> R.string.empty
    }
}

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

fun getCardBrandStringId(cardBrand: CardBrand?) : Int
{
    return when(cardBrand) {
        CardBrand.VISA -> R.string.card_brand_visa
        CardBrand.MASTERCARD -> R.string.card_brand_mastercard
        CardBrand.AMEX -> R.string.card_brand_amex
        CardBrand.DISCOVER -> R.string.card_brand_discover
        CardBrand.DINERS -> R.string.card_brand_diners
        CardBrand.JCB -> R.string.card_brand_jcb
        CardBrand.UPI -> R.string.card_brand_upi
        CardBrand.PURE -> R.string.card_brand_pure
        CardBrand.RUPAY -> R.string.card_brand_rupay
        CardBrand.MIR -> R.string.card_brand_mir
        else -> R.string.card_brand_unknown
    }
}

fun getCardEntryStringId(cardEntryMode: CardEntryMode?) : Int
{
    return when(cardEntryMode) {
        CardEntryMode.CONTACT -> R.string.card_entry_mode_contact
        CardEntryMode.CONTACLESS -> R.string.card_entry_mode_contactless
        CardEntryMode.CONTACLESS_MAGSTRIPE -> R.string.card_entry_mode_contactless_magstripe
        CardEntryMode.MAGSTRIPE -> R.string.card_entry_mode_magstripe
        CardEntryMode.FALLBACK_MAGSTRIPE -> R.string.card_entry_mode_fallback_magstripe
        CardEntryMode.MANUAL -> R.string.card_entry_mode_manual
        CardEntryMode.QRCODE -> R.string.card_entry_mode_qrcode
        else -> R.string.card_entry_mode_unspecified
    }
}

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

fun generateMasterPassword(user : String?, sharedViewModel: SharedViewModel) : String
{
    val tid = sharedViewModel.objPosConfig?.terminalId?:""
    val mid = sharedViewModel.objPosConfig?.merchantId?:""
    val deviceSN = sharedViewModel.objPosConfig?.deviceSN?.replace("-","")?:""

    // Step 1: Get today's date as a string (YYYYMMDD)
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = dateFormat.format(Date())

    // Step 2: Create a unique seed
    val seed = today + (user?:"") + tid + mid + deviceSN

    // Step 3: Generate SHA-256 hash from the seed
    val hash = sha256(seed)

    // Step 4: Convert hash to a 6-digit OTP
    val password = hash.filter { it.isDigit() }.take(6).padEnd(6, '0') // Take last 6 numeric digits & pad 0 if required

    return password  // Pads with '0' if needed
}

fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) } // Convert bytes to hex string
}
