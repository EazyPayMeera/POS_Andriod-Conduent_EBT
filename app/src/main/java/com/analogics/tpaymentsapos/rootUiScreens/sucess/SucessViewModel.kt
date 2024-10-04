package com.analogics.tpaymentsapos.rootUiScreens.sucess

import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean

class SucessViewModel {



    fun onEreceipt(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
    }

    fun onHome(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

}