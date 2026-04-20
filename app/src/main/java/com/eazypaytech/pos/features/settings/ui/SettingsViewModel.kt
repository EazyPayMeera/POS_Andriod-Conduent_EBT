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


    fun onLoad(sharedViewModel: SharedViewModel) {

        merchantName = sharedViewModel.objPosConfig?.merchantNameLocation ?: ""
        merchantBankName = sharedViewModel.objPosConfig?.merchantBankName ?: ""
        //merchantType = sharedViewModel.objPosConfig?.merchantId ?: ""
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

    fun updateMerchantName(value: String) {
        merchantName = value
    }

    fun updateMerchantLocation(value: String) {
        merchantLocation = value
    }

    fun updateMerchantBankName(value: String) {
        merchantBankName = value
    }

    fun updateMerchantType(value: String) {
        merchantType = value
    }

    fun updateFNSNumber(value: String) {
        fnsNumber = value
    }

    fun updatePostalServiceCode(value: String) {
        postalServiceCode = value
    }

    fun updateStateCode(value: String) {
        stateCode = value
    }

    fun updateCountyCode(value: String) {
        countyCode = value
    }



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