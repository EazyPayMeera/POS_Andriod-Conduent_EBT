package com.eazypaytech.posafrica.rootUiScreens.receiptdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReceiptDetailsViewModel(
    initialHeaders: List<String> = listOf("", "", "", ""),
    initialFooters: List<String> = listOf("", "", "", "")
) : ViewModel() {
    // Define separate MutableStateFlow for each header
    private val header1 = MutableStateFlow(initialHeaders.getOrElse(0) { "" })
    private val header2 = MutableStateFlow(initialHeaders.getOrElse(1) { "" })
    private val header3 = MutableStateFlow(initialHeaders.getOrElse(2) { "" })
    private val header4 = MutableStateFlow(initialHeaders.getOrElse(3) { "" })

    // Define separate MutableStateFlow for each footer
    private val footer1 = MutableStateFlow(initialFooters.getOrElse(0) { "" })
    private val footer2 = MutableStateFlow(initialFooters.getOrElse(1) { "" })
    private val footer3 = MutableStateFlow(initialFooters.getOrElse(2) { "" })
    private val footer4 = MutableStateFlow(initialFooters.getOrElse(3) { "" })

    // Expose the StateFlows as immutable
    val headerStateFlows: List<StateFlow<String>> = listOf(header1, header2, header3, header4)
    val footerStateFlows: List<StateFlow<String>> = listOf(footer1, footer2, footer3, footer4)

    // Update functions for headers
    fun updateHeader(index: Int, newValue: String) {
        when (index) {
            0 -> header1.value = newValue
            1 -> header2.value = newValue
            2 -> header3.value = newValue
            3 -> header4.value = newValue
        }
    }

    // Update functions for footers
    fun updateFooter(index: Int, newValue: String) {
        when (index) {
            0 -> footer1.value = newValue
            1 -> footer2.value = newValue
            2 -> footer3.value = newValue
            3 -> footer4.value = newValue
        }
    }

    fun onLoad(sharedViewModel: SharedViewModel) {
        /* Headers */
        sharedViewModel.objPosConfig?.header1?.let { header1.value = it  }
        sharedViewModel.objPosConfig?.header2?.let { header2.value = it  }
        sharedViewModel.objPosConfig?.header3?.let { header3.value = it  }
        sharedViewModel.objPosConfig?.header4?.let { header4.value = it  }
        /* Footers */
        sharedViewModel.objPosConfig?.footer1?.let { footer1.value = it  }
        sharedViewModel.objPosConfig?.footer2?.let { footer2.value = it  }
        sharedViewModel.objPosConfig?.footer3?.let { footer3.value = it  }
        sharedViewModel.objPosConfig?.footer4?.let { footer4.value = it  }
    }

    fun onSave(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objPosConfig?.apply {
            header1 = headerStateFlows[0].value
            header2 = headerStateFlows[1].value
            header3 = headerStateFlows[2].value
            header4 = headerStateFlows[3].value
            footer1 = footerStateFlows[0].value
            footer2 = footerStateFlows[1].value
            footer3 = footerStateFlows[2].value
            footer4 = footerStateFlows[3].value
        }?.saveToPrefs()
    }
}
