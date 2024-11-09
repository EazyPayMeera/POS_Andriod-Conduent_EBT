package com.analogics.tpaymentsapos.navigation

sealed class AppNavigationItems(val route: String) {
 object SplashScreen : AppNavigationItems(NavScreensEnum.SplashScreen.name)
 object DashBoardScreen : AppNavigationItems(NavScreensEnum.DashBoardView.name)
 object OnBoardingScreen : AppNavigationItems(NavScreensEnum.OnBoardingView.name)
 object LoginScreen : AppNavigationItems(NavScreensEnum.LoginScreen.name)
 object ForgetPasswordScreen : AppNavigationItems(NavScreensEnum.ForgetPasswordView.name)
 object PleaseWaitScreen : AppNavigationItems(NavScreensEnum.PleaseWaitView.name)
 object AmountScreen : AppNavigationItems(NavScreensEnum.AmountView.name)
 object CardScreen : AppNavigationItems(NavScreensEnum.CardView.name)
 object ConfirmationScreen : AppNavigationItems(NavScreensEnum.ConfirmationView.name)

 object InvoiceScreen : AppNavigationItems(NavScreensEnum.InvoiceView.name)
 object TipScreen : AppNavigationItems(NavScreensEnum.TipView.name)
 object PasswordScreen : AppNavigationItems(NavScreensEnum.PasswordView.name)
 object RefundAmtScreen : AppNavigationItems(NavScreensEnum.RefundAmtView.name)
 object SettingsScreen : AppNavigationItems(NavScreensEnum.SettingsView.name)
 object LanguageScreen : AppNavigationItems(NavScreensEnum.LanguageView.name)
 object ConfigurationScreen : AppNavigationItems(NavScreensEnum.ConfigurationView.name)
 object ConfirmShiftScreen : AppNavigationItems(NavScreensEnum.ConfirmShiftView.name)
 object TaxPercentageScreen : AppNavigationItems(NavScreensEnum.TaxPercentageView.name)
 object TipPercentageScreen : AppNavigationItems(NavScreensEnum.TipPercentageView.name)
 object PreauthScreen : AppNavigationItems(NavScreensEnum.PreauthView.name)
 object EnterEmailScreen : AppNavigationItems(NavScreensEnum.EnterEmailView.name)
 object InfoConfirmScreen : AppNavigationItems(NavScreensEnum.InfoConfirmView.name)
 object TxnListScreen:AppNavigationItems(NavScreensEnum.TxnListView.name)
 object SucessScreen:AppNavigationItems(NavScreensEnum.SucessView.name)
 object ChangePasswordScreen:AppNavigationItems(NavScreensEnum.ChangePasswordView.name)
 object BarcodeScreen:AppNavigationItems(NavScreensEnum.BarcodeView.name)
 object CardDetectScreen:AppNavigationItems(NavScreensEnum.CardDetectView.name)
 object TransactionDetailsScreen:AppNavigationItems(NavScreensEnum.TransactionDetailsView.name)
 object ReceiptDetailsScreen:AppNavigationItems(NavScreensEnum.ReceiptDetailsView.name)
 object AddClerkScreen:AppNavigationItems(NavScreensEnum.AddClerkScreen.name)
 object ActivationScreen:AppNavigationItems(NavScreensEnum.ActivationScreen.name)
 object UserManagementScreen:AppNavigationItems(NavScreensEnum.UserManagementView.name)

 object PinScreen : AppNavigationItems(NavScreensEnum.PinView.name)

 object EmailScreen : AppNavigationItems("email_screen/{email}") {
  fun createRoute(email: String) = "email_screen/$email"
 }

 object ApprovedScreen : AppNavigationItems("approved_screen/{totalAmount}") {
  fun createRoute(totalAmount: String) = "approved_screen/$totalAmount"
 }

 object DeclineScreen : AppNavigationItems("decline_screen/{totalAmount}") {
  fun createRoute(totalAmount: String) = "decline_screen/$totalAmount"
 }

}
