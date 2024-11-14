

package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.securityframework.database.entity.UserManagementEntity
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootModel.Symbol
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.pow

fun calculateTotalAmount(transactionAmount: Double, tipAmount: Double, sgstAmount: Double, igstAmount: Double): Double {
    return transactionAmount + tipAmount + sgstAmount + igstAmount
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

fun calculateTip(amount: Double, tip: Double): Double {
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

@RequiresApi(Build.VERSION_CODES.O)
fun convertLocalDateTimeToString(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT1)
    return dateTime.format(formatter)
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

fun emvCardCheckStatusToMsgId(cardCheckStatus: EmvServiceResult.CardCheckStatus) : EmvServiceResult.DisplayMsgId
{
    return when(cardCheckStatus) {
        EmvServiceResult.CardCheckStatus.CARD_INSERTED -> EmvServiceResult.DisplayMsgId.CARD_INSERTED
        EmvServiceResult.CardCheckStatus.CARD_SWIPED -> EmvServiceResult.DisplayMsgId.CARD_SWIPED
        EmvServiceResult.CardCheckStatus.CARD_TAPPED -> EmvServiceResult.DisplayMsgId.CARD_TAPPED
        else -> EmvServiceResult.DisplayMsgId.NONE
    }
}

@Composable
fun getEmvMsgIdString(displayMsgId: EmvServiceResult.DisplayMsgId) : String
{
    val id : Int? =
        when(displayMsgId) {
            EmvServiceResult.DisplayMsgId.DISPLAY_BALANCE -> R.string.amount
            EmvServiceResult.DisplayMsgId.NONE -> null
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
        }

    return if(id!=null) stringResource(id) else ""
}

fun getIsoResponseCodeString(context: Context, responseCode: String?) : String
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
            else -> R.string.iso_resp_code_unknown_error
        }

    return if(id!=null) context.resources.getString(id) else ""
}