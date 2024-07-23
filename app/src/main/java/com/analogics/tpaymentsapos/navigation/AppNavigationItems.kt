package com.analogics.tpaymentsapos.navigation

sealed class AppNavigationItems(val route:String) {
   data  object  SplashScreen:AppNavigationItems(NavScreensEnum.SplashScreen.name)
    object  DashBoardScreen:AppNavigationItems(NavScreensEnum.DashBoardView.name)
    object  OnBoardingScreen:AppNavigationItems(NavScreensEnum.OnBoardingView.name)
    object  LoginScreen:AppNavigationItems(NavScreensEnum.LoginScreen.name)
    object  ForgetPasswordScreen:AppNavigationItems(NavScreensEnum.ForgetPasswordView.name)
    object  PleaseWaitScreen:AppNavigationItems(NavScreensEnum.PleaseWaitView.name)
    object  TrainingScreen:AppNavigationItems(NavScreensEnum.TrainingView.name)
    object  AmountScreen:AppNavigationItems(NavScreensEnum.AmountView.name)
    object  InvoiceScreen:AppNavigationItems(NavScreensEnum.InvoiceView.name)
    //object  ConfirmationScreen:AppNavigationItems(NavScreensEnum.ConfirmationView.name)
    object  TipScreen:AppNavigationItems(NavScreensEnum.TipView.name)
    //object  CardScreen:AppNavigationItems(NavScreensEnum.CardView.name)
    object CardScreen : AppNavigationItems("card_screen/{totalAmount}") {
     fun createRoute(totalAmount: String) = "card_screen/$totalAmount"
    }
    object CardDetectScreen : AppNavigationItems("card_detect_screen/{totalAmount}") {
    fun createRoute(totalAmount: String) = "card_detect_screen/$totalAmount"
    }
    object  PinScreen:AppNavigationItems(NavScreensEnum.PinView.name)
    object ApprovedScreen : AppNavigationItems("approved_screen/{totalAmount}") {
        fun createRoute(totalAmount: String) = "approved_screen/$totalAmount"
    }
    object ConfirmationScreen : AppNavigationItems("confirmation_screen/{amount}") {
    fun createRoute(amount: String) = "confirmation_screen/$amount"


 }
}