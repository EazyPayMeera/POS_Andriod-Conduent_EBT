package com.eazypaytech.posafrica.rootUiScreens.settings


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private var merchantName: String = ""
    private var merchantLocation: String = ""
    private var merchantBankName: String = ""
    private var merchantType: String = ""


    fun onLoad(sharedViewModel: SharedViewModel) {

        merchantName = sharedViewModel.objPosConfig?.merchantNameLocation ?: ""
        merchantBankName = sharedViewModel.objPosConfig?.merchantBankName ?: ""
        merchantType = sharedViewModel.objPosConfig?.merchantId ?: ""
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

    fun onSaveMerchantConfig(
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel,
        merchantNameLocation: String,
        merchantBankName: String,
        merchantType: String
    ) {
        sharedViewModel.objRootAppPaymentDetail.merchantNameLocation = merchantNameLocation
        sharedViewModel.objRootAppPaymentDetail.merchantType = merchantType
        sharedViewModel.objRootAppPaymentDetail.merchantBankName = merchantBankName
        sharedViewModel.objPosConfig?.apply {
            this.merchantNameLocation = merchantNameLocation
            this.merchantBankName = merchantBankName
            this.merchantType = merchantType

        }?.saveToPrefs()
        navHostController.popBackStack()
    }
}