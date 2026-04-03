package com.eazypaytech.posafrica.core.navigation.routes

import com.eazypaytech.posafrica.core.navigation.utilities.NavScreensEnum

sealed class AppNavigationItems(val route: String) {
 object SplashScreen : AppNavigationItems(NavScreensEnum.SplashScreen.name)
 object DashBoardScreen : AppNavigationItems(NavScreensEnum.DashBoardScreen.name)
 object OnBoardingScreen : AppNavigationItems(NavScreensEnum.OnBoardingScreen.name)
 object LoginScreen : AppNavigationItems(NavScreensEnum.LoginScreen.name)
 object ForgetPasswordScreen : AppNavigationItems(NavScreensEnum.ForgetPasswordView.name)
 object PleaseWaitScreen : AppNavigationItems(NavScreensEnum.PleaseWaitScreen.name)
 object AmountScreen : AppNavigationItems(NavScreensEnum.AmountScreen.name)
 object CardScreen : AppNavigationItems(NavScreensEnum.CardScreen.name)
 object ConfirmationScreen : AppNavigationItems(NavScreensEnum.ConfirmationScreen.name)
 object CashBackScreen : AppNavigationItems(NavScreensEnum.CashBackScreen.name)
 object ManualCardScreen : AppNavigationItems(NavScreensEnum.ManualCardScreen.name)
 object InvoiceScreen : AppNavigationItems(NavScreensEnum.InvoiceView.name)
 object TipScreen : AppNavigationItems(NavScreensEnum.TipScreen.name)
 object ServiceChargeScreen : AppNavigationItems(NavScreensEnum.ServiceChargeScreen.name)
 object PasswordScreen : AppNavigationItems(NavScreensEnum.PasswordScreen.name)
 object RefundAmtScreen : AppNavigationItems(NavScreensEnum.RefundAmtScreen.name)
 object SettingsScreen : AppNavigationItems(NavScreensEnum.SettingsScreen.name)
 object LanguageScreen : AppNavigationItems(NavScreensEnum.LanguageScreen.name)
 object ConfigurationScreen : AppNavigationItems(NavScreensEnum.ConfigurationScreen.name)
 object ConfirmShiftScreen : AppNavigationItems(NavScreensEnum.ConfirmShiftScreen.name)
 object TaxPercentageScreen : AppNavigationItems(NavScreensEnum.TaxPercentageScreen.name)
 object TipPercentageScreen : AppNavigationItems(NavScreensEnum.TipPercentageScreen.name)
 object ServiceChargePercentageScreen : AppNavigationItems(NavScreensEnum.ServiceChargePercentageScreen.name)
 object PreauthScreen : AppNavigationItems(NavScreensEnum.PreauthScreen.name)
 object EnterEmailScreen : AppNavigationItems(NavScreensEnum.EnterEmailScreen.name)
 object InfoConfirmScreen : AppNavigationItems(NavScreensEnum.InfoConfirmScreen.name)
 object TxnListScreen:AppNavigationItems(NavScreensEnum.TxnListScreen.name)
 object SucessScreen:AppNavigationItems(NavScreensEnum.SucessScreen.name)
 object ChangePasswordScreen:AppNavigationItems(NavScreensEnum.ChangePasswordScreen.name)
 object BarcodeScreen:AppNavigationItems(NavScreensEnum.BarcodeScreen.name)
 object CardDetectScreen:AppNavigationItems(NavScreensEnum.CardDetectScreen.name)
 object TransactionDetailsScreen:AppNavigationItems(NavScreensEnum.TransactionDetailsScreen.name)
 object ReceiptDetailsScreen:AppNavigationItems(NavScreensEnum.ReceiptDetailsScreen.name)
 object AddClerkScreen:AppNavigationItems(NavScreensEnum.AddClerkScreen.name)
 object ActivationScreen:AppNavigationItems(NavScreensEnum.ActivationScreen.name)
 object UserManagementScreen:AppNavigationItems(NavScreensEnum.UserManagementScreen.name)
 object BatchIdScreen:AppNavigationItems(NavScreensEnum.BatchIdScreen.name)
 object InactivityTimeoutScreen:AppNavigationItems(NavScreensEnum.InactivityTimeoutScreen.name)
 object SignatureScreen:AppNavigationItems(NavScreensEnum.SignatureScreen.name)
 object EBTSelScreen:AppNavigationItems(NavScreensEnum.EBTSelectionScreen.name)
 object TxnSelScreen:AppNavigationItems(NavScreensEnum.TxnSelectionScreen.name)
 object KeyEntryScreen:AppNavigationItems(NavScreensEnum.KeyEntryScreen.name)
 object VoucherScreen:AppNavigationItems(NavScreensEnum.VoucherCardScreen.name)
 object AuthCodeScreen:AppNavigationItems(NavScreensEnum.AuthScreen.name)

 object PinScreen : AppNavigationItems(NavScreensEnum.PinScreen.name)

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