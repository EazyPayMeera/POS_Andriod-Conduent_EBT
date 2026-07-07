package com.eazypaytech.pos.features.dashboard.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.data.model.emv.TermConfig
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.miscellaneous.PrinterUtils
import com.eazypaytech.pos.core.utils.miscellaneous.readAsset
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var apiServiceRepository: ApiServiceRepository, private var emvServiceRepository: EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {

    private val _isAdmin = MutableStateFlow<Boolean>(false)
    val isAdmin : StateFlow<Boolean> = _isAdmin
    var sharedViewModel : SharedViewModel? = null

    /**
     * Clears current transaction data
     * and refreshes admin status.
     */
    fun clearTransData(sharedViewModel: SharedViewModel) {
        sharedViewModel.clearTransData()
        checkIfAdmin(sharedViewModel)
    }

    /**
     * Reprints the last transaction receipt.
     *
     * Flow:
     * - Fetch last transaction from DB
     * - Print receipt if found
     * - Show error dialog if no record exists
     */
    fun reprintLast(
        context: Context,
        sharedViewModel: SharedViewModel,
        isCustomer: Boolean = false
    ) {
        viewModelScope.launch {
            txnDBRepository.fetchLastTransaction()?.let {
                PrinterUtils.printReceipt(context, sharedViewModel, PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it)?: ObjRootAppPaymentDetails(),isCustomer)
            }?:let {
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = context.resources.getString(R.string.printer_Alert),
                    subtitle = context.resources.getString(R.string.printer_no_record)
                )
            }
        }
    }

    /**
     * Generates and sets next invoice number
     * based on last stored invoice.
     */
    fun setInvoiceNumber(sharedViewModel: SharedViewModel)
    {
        viewModelScope.launch{
            txnDBRepository.getLastInvoiceNumber().let {
                sharedViewModel.objRootAppPaymentDetail.invoiceNo = (it+1).toString()
            }
        }
    }

    /**
     * Initializes Payment SDK if not already initialized.
     *
     * Flow:
     * - Checks initialization flag
     * - Loads EMV configs and CAP keys
     * - Calls SDK init API
     * - Updates config based on success/failure
     */

    fun initPaymentSDK(context: Context, sharedViewModel: SharedViewModel) {
        if(sharedViewModel.objPosConfig?.isPaymentSDKInit!=true) {
            viewModelScope.launch {
                //  STEP 1: Get config from TMS
                val posConfig = sharedViewModel.objPosConfig

                //  STEP 2: Extract EMV + CAPK JSON
                val emvJson = posConfig?.emvConfigJson
                val capkJson = posConfig?.capKeysJson

                //  STEP 3: Decide source (TMS OR fallback)
                val finalAidConfig = try {
                    if (!emvJson.isNullOrEmpty()) {
                        // Validate JSON
                        org.json.JSONObject(emvJson)
                        emvJson   // valid JSON → use TMS
                    } else {
                        readAsset(context, AppConstants.DEFAULT_EMV_CONFIG_FILE_PATH)
                    }
                } catch (e: Exception) {
                    Log.e("TMS", "Invalid EMV JSON → falling back to asset", e)
                    readAsset(context, AppConstants.DEFAULT_EMV_CONFIG_FILE_PATH)
                }

                val finalCapKeys = try {
                    if (!capkJson.isNullOrEmpty()) {
                        org.json.JSONObject(capkJson)
                        capkJson
                    } else {
                        readAsset(context, AppConstants.DEFAULT_EMV_CAP_KEY_FILE_PATH)
                    }
                } catch (e: Exception) {
                    Log.e("TMS", "Invalid CAPK JSON → falling back", e)
                    readAsset(context, AppConstants.DEFAULT_EMV_CAP_KEY_FILE_PATH)
                }
                Log.d("TMS_FINAL", "AID CONFIG: $finalAidConfig")
                Log.d("TMS_FINAL", "CAP KEYS: $finalCapKeys")
                emvServiceRepository.initPaymentSDK(
                    termConfig = TermConfig(
                        terminalIdentifier = sharedViewModel.objPosConfig?.procId,
                        merchantIdentifier = sharedViewModel.objPosConfig?.merchantId,
                    ),
                    //aidConfig = readAsset(context, AppConstants.DEFAULT_EMV_CONFIG_FILE_PATH),
                    //capKeys = readAsset(context, AppConstants.DEFAULT_EMV_CAP_KEY_FILE_PATH),
                    aidConfig = finalAidConfig,
                    capKeys = finalCapKeys,
                    iEmvServiceResponseListener =  object :
                        IEmvServiceResponseListener {
                    override fun onEmvServiceResponse(response: Any) {
                        if (response is EmvServiceResult.InitResult && response.status == EmvServiceResult.InitStatus.SUCCESS) {
                                    sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = true }
                                        ?.saveToPrefs()
                                    /*CustomDialogBuilder.composeAlertDialog(
                                        title = context.resources.getString(
                                            R.string.emv_sdk_init_title
                                        ),
                                        subtitle = context.resources.getString(R.string.emv_sdk_init_success)
                                    )*/
                            Log.d("Initialization", "Payment SDK Initialized")
                        }
                        else {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                            //CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.emv_sdk_init_title), subtitle = context.resources.getString(R.string.emv_sdk_init_failure))
                            Log.d("Initialization", "Payment SDK Initialization Failed")
                       }
                    }

                    override fun onEmvServiceDisplayMessage(
                        displayMsgId: EmvServiceResult.DisplayMsgId
                    ) {
                        //CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
                    }
                })
            }
        }
    }

    /**
     * Handles device reactivation flow.
     *
     * Flow:
     * - Blocks if batch is open
     * - Restricts non-admin users
     * - Shows confirmation dialog
     * - Resets activation flags and navigates to activation screen
     */
    fun onReactivate(navHostController : NavHostController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            if (txnDBRepository.isBatchOpen()) {
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = navHostController.context.resources.getString(
                        R.string.restricted
                    ),
                    subtitle = navHostController.context.resources.getString(R.string.batch_open)
                )
            }
            else if (!isAdmin.value) {
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = navHostController.context.resources.getString(
                        R.string.restricted
                    ),
                    subtitle = navHostController.context.resources.getString(R.string.for_admin)
                )
            }
            else{
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = navHostController.context.getString(R.string.reactivate_device),
                    message = navHostController.context.getString(R.string.confirm_reactivate_device),
                    okBtnText = navHostController.context.getString(R.string.yes),
                    onOkClick = {
                        sharedViewModel.objPosConfig?.apply { isActivationDone = false; isLoggedIn = false; isOnboardingComplete = false; isPaymentSDKInit = false }
                            ?.saveToPrefs()
                        navHostController.navigateAndClean(AppNavigationItems.ActivationScreen.route)
                    },
                    cancelBtnText = navHostController.context.getString(R.string.cancel_no),
                )
            }
        }
    }

    /**
     * Checks if current user is admin
     * and updates admin state.
     */
    fun checkIfAdmin(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig?.loginId?.let {
                txnDBRepository.isAdmin(it).let {
                    _isAdmin.value = it
                }
            }
        }
    }

    fun deleteOldTransactions() {
        viewModelScope.launch {
            try {
                txnDBRepository.deleteOldTransactions()
                Log.d("DB_DEBUG", "Old transactions deleted successfully")
            } catch (e: Exception) {
                Log.e("DB_DEBUG", "Failed to delete old transactions: ${e.message}")
            }
        }
    }
}