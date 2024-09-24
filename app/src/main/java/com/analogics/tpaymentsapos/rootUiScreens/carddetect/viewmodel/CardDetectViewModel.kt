package com.analogics.tpaymentsapos.rootUiScreens.carddetect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

var updated_amt = ""

class CardDetectViewModel @Inject constructor() : ViewModel() {

    // Function to set the total amount and update the global variable
    fun setTotalAmount(amount: String) {
        updated_amt = amount // Update the global variable as well
    }

    // Function to handle the delay and navigation
    fun navigateAfterDelay(navHostController: NavHostController) {
        viewModelScope.launch {
            delay(2000) // Delay for 2 seconds (2000 milliseconds)
            navHostController.navigate(AppNavigationItems.PinScreen.route) // Navigate to the desired screen
        }
    }

//    fun insertTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails)=viewModelScope.launch{
//        val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON
//
//        dbRepository.insertTxn(Gson().fromJson(json, TxnEntity::class.java))
//        Log.d("password " +
//                "record insert suc",Gson().fromJson(json, TxnEntity::class.java).toString())
//
//    }
}

