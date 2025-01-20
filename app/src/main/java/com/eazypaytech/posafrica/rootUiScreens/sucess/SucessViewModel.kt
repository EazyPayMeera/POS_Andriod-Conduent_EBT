package com.eazypaytech.posafrica.rootUiScreens.sucess

import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean

class SucessViewModel {



    fun onEreceipt(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
    }

    fun onHome(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

}