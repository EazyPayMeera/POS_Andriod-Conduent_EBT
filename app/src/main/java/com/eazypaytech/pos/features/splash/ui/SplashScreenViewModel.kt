package com.eazypaytech.pos.features.splash.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.networkservicecore.data.serviceutils.NetworkConstants
import com.analogics.networkservicecore.tms.repository.TmsRepository

import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.models.TmsConfigMapper
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.setUiLanguage
import com.eazypaytech.pos.core.utils.language.UiLanguage
import com.eazypaytech.pos.core.utils.language.toUiLanguage
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.posafrica.device.DeviceInfoProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private var dbRepository: TxnDBRepository, private val tmsRepository: TmsRepository, @ApplicationContext private val context: Context, private val deviceInfoProvider: DeviceInfoProvider) : ViewModel() {

    /**
     * Handles splash screen completion and app initialization flow.
     *
     * Behavior:
     * - Fetches existing POS configuration from local storage
     * - Retrieves device serial number and attempts to fetch TMS configuration
     * - Maps TMS response to POS config (if available)
     * - Merges TMS config with saved config while preserving critical flags
     * - Saves final configuration and updates network host settings
     * - Applies selected UI language
     * - Navigates user based on app state:
     *   - Onboarding (if not completed)
     *   - Activation (if not done)
     *   - Clerk creation (if no users exist)
     *   - Login (if not logged in)
     *   - Dashboard (if all conditions satisfied)
     *
     * @param navController Navigation controller for screen transitions
     * @param sharedViewModel Shared ViewModel holding POS configuration and app state
     */
    @SuppressLint("RestrictedApi")
    fun onSplashScreenFinished(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            val savedConfig = apiServiceRepository.getPosConfig()

            //  STEP 1: Fetch from TMS
            val sn = try {
                deviceInfoProvider.getSerialNumber(context)
            } catch (e: Exception) {
                "UNKNOWN"
            }
            Log.d("TMS", "Device SN: $sn")
            delay(300)
            val tmsMap = try {
                tmsRepository.fetchConfig(sn)
            } catch (e: Exception) {
                Log.e("TMS", "Error fetching TMS config", e)
                null
            }

            Log.d("TMS", "TMS MAP: $tmsMap")

            //  STEP 2: Convert to PosConfig (if TMS available)
            val tmsConfig = tmsMap?.let {
                TmsConfigMapper.mapToPosConfig(context, it)
            }
            Log.d("TMS_DEBUG", "TMS CONFIG: $tmsConfig")
            Log.d("TMS_DEBUG", "EMV JSON: ${tmsConfig?.emvConfigJson}")
            Log.d("TMS_DEBUG", "CAP KEYS: ${tmsConfig?.capKeysJson}")
            //  STEP 3: Fallback to existing config if TMS fails
            val finalConfig = if (tmsConfig != null && tmsMap?.isNotEmpty() == true) {
                tmsConfig
            } else {
                savedConfig
            }
            Log.d("TMS_DEBUG", "FINAL CONFIG USED: $finalConfig")
            Log.d("TMS_DEBUG", "FINAL EMV JSON: ${finalConfig.emvConfigJson}")
            Log.d("TMS_DEBUG", "FINAL CAP KEYS: ${finalConfig.capKeysJson}")
            //  STEP 4: Preserve existing flags (DO NOT TOUCH)
            //val finalConfig = tmsConfig ?: savedConfig
            finalConfig.isActivationDone = savedConfig.isActivationDone
            finalConfig.isLoggedIn = savedConfig.isLoggedIn
            finalConfig.isOnboardingComplete = savedConfig.isOnboardingComplete
            finalConfig.header1 = savedConfig.header1
            finalConfig.header2 = savedConfig.header2
            finalConfig.header3 = savedConfig.header3
            finalConfig.header4 = savedConfig.header4
            finalConfig.footer1 = savedConfig.footer1
            finalConfig.footer2 = savedConfig.footer2
            finalConfig.footer3 = savedConfig.footer3
            finalConfig.footer4 = savedConfig.footer4
            finalConfig.loginId = savedConfig.loginId
            finalConfig.terminalId = savedConfig.terminalId ?: tmsConfig?.terminalId
            finalConfig.merchantId = savedConfig.merchantId ?: tmsConfig?.merchantId
            finalConfig.procId = savedConfig.procId ?: tmsConfig?.procId
            finalConfig.merchantType = savedConfig.merchantType ?: tmsConfig?.merchantType
            finalConfig.countyCode = savedConfig.countyCode ?: tmsConfig?.countyCode
            finalConfig.fnsNumber = savedConfig.fnsNumber ?: tmsConfig?.fnsNumber
            finalConfig.merchantNameLocation = savedConfig.merchantNameLocation ?: tmsConfig?.merchantNameLocation
            finalConfig.merchantBankName = savedConfig.merchantBankName ?: tmsConfig?.merchantBankName
            finalConfig.stateCode = savedConfig.stateCode ?: tmsConfig?.stateCode
            finalConfig.isTapEnable = savedConfig.isTapEnable
            finalConfig.isEMVEnable = savedConfig.isEMVEnable
            finalConfig.postalServiceCode = savedConfig.postalServiceCode ?: tmsConfig?.postalServiceCode
            // EMV AIDConfig
            finalConfig.emvConfigJson = tmsConfig?.emvConfigJson ?: savedConfig.emvConfigJson
            try {
                finalConfig.emvConfigJson?.let {
                    org.json.JSONObject(it)
                    Log.d("TMS_DEBUG", "EMV JSON VALID ")
                }
            } catch (e: Exception) {
                Log.e("TMS_DEBUG", "EMV JSON INVALID ", e)
            }
            // EMV CAPKeys
            finalConfig.capKeysJson = tmsConfig?.capKeysJson ?: savedConfig.capKeysJson
            try {
                finalConfig.capKeysJson?.let {
                    org.json.JSONObject(it)
                    Log.d("TMS_DEBUG", "EMV CAP Keys JSON VALID ")
                }
            } catch (e: Exception) {
                Log.e("TMS_DEBUG", "EMV CAP Keys JSON INVALID ", e)
            }
            sharedViewModel.objPosConfig = finalConfig
            finalConfig.saveToPrefs()

            NetworkConstants.updateHost(
                baseUrl = finalConfig.baseUrl,
                port = finalConfig.port
            )

            /* Apply UI Language */
            setUiLanguage(
                navController.context,
                sharedViewModel.objPosConfig?.language?.toUiLanguage() ?: UiLanguage.ENGLISH
            )

            /* Check if onboarding carousel is shown */
            if (sharedViewModel.objPosConfig?.isOnboardingComplete != true) {
                delay(AppConstants.SPLASH_SCREEN_TIMEOUT_MS)
                navController.navigateAndClean(AppNavigationItems.OnBoardingScreen.route)
            }
            /* Check if Terminal Activation is done */
            else if(sharedViewModel.objPosConfig?.isActivationDone!=true)
                navController.navigateAndClean(AppNavigationItems.ActivationScreen.route)
            /* If admin is not added, then add it */
            else if(dbRepository.getUserCount()<1)
                navController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
            /* If not logged in, then prompt login */
            else if(sharedViewModel.objPosConfig?.isLoggedIn!=true)
                navController.navigateAndClean(AppNavigationItems.LoginScreen.route)
            /* If logged in, then navigate to dashboard */
            else
                navController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }
}