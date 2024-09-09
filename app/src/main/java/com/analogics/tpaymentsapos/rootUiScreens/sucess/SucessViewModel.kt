package com.analogics.tpaymentsapos.rootUiScreens.sucess

import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems

class SucessViewModel {



    fun onEreceipt(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
    }

    fun onHome(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }

}