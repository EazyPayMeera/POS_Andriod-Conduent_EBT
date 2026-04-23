package com.eazypaytech.pos.features.settings.ui


import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.pos.features.activity.ui.SharedViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private var merchantName: String = ""
    private var merchantLocation: String = ""
    private var merchantBankName: String = ""
    private var fnsNumber: String = ""
    private var postalServiceCode = ""
    private var stateCode: String = ""
    private var countyCode: String = ""
    private var merchantType: String = ""

    /**
     * Loads merchant configuration from POS config.
     *
     * Behavior:
     * - Reads merchant details from stored configuration
     * - Updates local UI state variables
     * - Syncs values to transaction object for further processing
     *
     * @param sharedViewModel Shared ViewModel containing POS config and txn data
     */
    fun onLoad(sharedViewModel: SharedViewModel) {

        merchantName = sharedViewModel.objPosConfig?.merchantNameLocation ?: ""
        merchantBankName = sharedViewModel.objPosConfig?.merchantBankName ?: ""
        merchantType = sharedViewModel.objPosConfig?.merchantCategoryCode ?: ""
        fnsNumber = sharedViewModel.objPosConfig?.fnsNumber  ?: ""
        stateCode = sharedViewModel.objPosConfig?.stateCode  ?: ""
        countyCode = sharedViewModel.objPosConfig?.countyCode  ?: ""
        postalServiceCode = sharedViewModel.objPosConfig?.postalServiceCode  ?: ""

        //Save to objRootAppPaymentDetail
        sharedViewModel.objRootAppPaymentDetail.merchantNameLocation = merchantName
        sharedViewModel.objRootAppPaymentDetail.merchantType = merchantType
        sharedViewModel.objRootAppPaymentDetail.merchantBankName = merchantBankName
        sharedViewModel.objRootAppPaymentDetail.fnsNumber = fnsNumber
        sharedViewModel.objRootAppPaymentDetail.stateCode = stateCode
        sharedViewModel.objRootAppPaymentDetail.countyCode = countyCode
        sharedViewModel.objRootAppPaymentDetail.postalServiceCode = postalServiceCode
    }

    /**
     * Updates merchant location value.
     *
     * @param value Merchant location string
     */
    fun updateMerchantLocation(value: String) {
        merchantLocation = value
    }

    /**
     * Updates merchant bank name.
     *
     * @param value Merchant bank name
     */
    fun updateMerchantBankName(value: String) {
        merchantBankName = value
    }

    /**
     * Updates merchant type/category.
     *
     * @param value Merchant category code
     */
    fun updateMerchantType(value: String) {
        merchantType = value
    }

    /**
     * Updates FNS (Food and Nutrition Service) number.
     *
     * @param value FNS number
     */
    fun updateFNSNumber(value: String) {
        fnsNumber = value
    }

    /**
     * Updates postal service code.
     *
     * @param value Postal service code
     */
    fun updatePostalServiceCode(value: String) {
        postalServiceCode = value
    }

    /**
     * Updates state code.
     *
     * @param value State code
     */
    fun updateStateCode(value: String) {
        stateCode = value
    }

    /**
     * Updates county code.
     *
     * @param value County code
     */
    fun updateCountyCode(value: String) {
        countyCode = value
    }

    /**
     * Saves merchant configuration.
     *
     * Behavior:
     * - Updates transaction object with latest merchant details
     * - Persists updated configuration to shared preferences
     * - Navigates back to previous screen
     *
     * @param navHostController Navigation controller for back navigation
     * @param sharedViewModel Shared ViewModel containing config and txn data
     * @param merchantNameLocation Merchant name/location
     * @param merchantBankName Merchant bank name
     * @param merchantType Merchant category/type
     * @param fnsNumber FNS number
     * @param stateCode State code
     * @param countyCode County code
     * @param postalServiceCode Postal service code
     */
    fun onSaveMerchantConfig(
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel,
        merchantNameLocation: String,
        merchantBankName: String,
        merchantType: String,
        fnsNumber: String,
        stateCode: String,
        countyCode: String,
        postalServiceCode: String
    ) {

        sharedViewModel.objRootAppPaymentDetail.merchantNameLocation = merchantNameLocation
        sharedViewModel.objRootAppPaymentDetail.merchantType = merchantType
        sharedViewModel.objRootAppPaymentDetail.merchantBankName = merchantBankName
        sharedViewModel.objRootAppPaymentDetail.fnsNumber = fnsNumber
        sharedViewModel.objRootAppPaymentDetail.stateCode = stateCode
        sharedViewModel.objRootAppPaymentDetail.countyCode = countyCode
        sharedViewModel.objRootAppPaymentDetail.postalServiceCode = postalServiceCode
        sharedViewModel.objPosConfig?.apply {
            this.merchantNameLocation = merchantNameLocation
            this.merchantBankName = merchantBankName
            this.merchantType = merchantType
            this.fnsNumber = fnsNumber
            this.stateCode = stateCode
            this.countyCode = countyCode
            this.postalServiceCode = postalServiceCode
        }?.saveToPrefs()
        navHostController.popBackStack()
    }

}