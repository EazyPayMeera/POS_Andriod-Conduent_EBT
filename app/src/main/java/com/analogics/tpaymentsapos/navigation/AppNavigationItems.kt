package com.analogics.tpaymentsapos.navigation

sealed class AppNavigationItems(val route:String) {
    data object  SplashScreen:AppNavigationItems(NavScreensEnum.SplashScreen.name)
    object  DashBoardScreen:AppNavigationItems(NavScreensEnum.DashBoardView.name)
    object  OnBoardingScreen:AppNavigationItems(NavScreensEnum.OnBoardingView.name)

}