package com.eazypaytech.posafrica.core.navigation.routes

import com.eazypaytech.posafrica.core.navigation.utilities.ScreensRouteTypes

sealed class AppNavigationItems(val route: String) {
 object SplashScreen : AppNavigationItems(ScreensRouteTypes.SplashScreen.name)
 object DashBoardScreen : AppNavigationItems(ScreensRouteTypes.DashBoardScreen.name)
 object OnBoardingScreen : AppNavigationItems(ScreensRouteTypes.OnBoardingScreen.name)
 object LoginScreen : AppNavigationItems(ScreensRouteTypes.LoginScreen.name)
 object ForgetPasswordScreen : AppNavigationItems(ScreensRouteTypes.ForgetPasswordView.name)
 object PleaseWaitScreen : AppNavigationItems(ScreensRouteTypes.PleaseWaitScreen.name)
 object AmountScreen : AppNavigationItems(ScreensRouteTypes.AmountScreen.name)
 object CardScreen : AppNavigationItems(ScreensRouteTypes.CardScreen.name)
 object ConfirmationScreen : AppNavigationItems(ScreensRouteTypes.ConfirmationScreen.name)
 object CashBackScreen : AppNavigationItems(ScreensRouteTypes.CashBackScreen.name)
 object ManualCardScreen : AppNavigationItems(ScreensRouteTypes.ManualCardScreen.name)
 object InvoiceScreen : AppNavigationItems(ScreensRouteTypes.InvoiceView.name)
 object TipScreen : AppNavigationItems(ScreensRouteTypes.TipScreen.name)
 object ServiceChargeScreen : AppNavigationItems(ScreensRouteTypes.ServiceChargeScreen.name)
 object PasswordScreen : AppNavigationItems(ScreensRouteTypes.PasswordScreen.name)
 object RefundAmtScreen : AppNavigationItems(ScreensRouteTypes.RefundAmtScreen.name)
 object SettingsScreen : AppNavigationItems(ScreensRouteTypes.SettingsScreen.name)
 object LanguageScreen : AppNavigationItems(ScreensRouteTypes.LanguageScreen.name)
 object ConfigurationScreen : AppNavigationItems(ScreensRouteTypes.ConfigurationScreen.name)
 object ConfirmShiftScreen : AppNavigationItems(ScreensRouteTypes.ConfirmShiftScreen.name)
 object TaxPercentageScreen : AppNavigationItems(ScreensRouteTypes.TaxPercentageScreen.name)
 object TipPercentageScreen : AppNavigationItems(ScreensRouteTypes.TipPercentageScreen.name)
 object ServiceChargePercentageScreen : AppNavigationItems(ScreensRouteTypes.ServiceChargePercentageScreen.name)
 object PreauthScreen : AppNavigationItems(ScreensRouteTypes.PreauthScreen.name)
 object EnterEmailScreen : AppNavigationItems(ScreensRouteTypes.EnterEmailScreen.name)
 object InfoConfirmScreen : AppNavigationItems(ScreensRouteTypes.InfoConfirmScreen.name)
 object TxnListScreen:AppNavigationItems(ScreensRouteTypes.TxnListScreen.name)
 object SucessScreen:AppNavigationItems(ScreensRouteTypes.SucessScreen.name)
 object ChangePasswordScreen:AppNavigationItems(ScreensRouteTypes.ChangePasswordScreen.name)
 object BarcodeScreen:AppNavigationItems(ScreensRouteTypes.BarcodeScreen.name)
 object CardDetectScreen:AppNavigationItems(ScreensRouteTypes.CardDetectScreen.name)
 object TransactionDetailsScreen:AppNavigationItems(ScreensRouteTypes.TransactionDetailsScreen.name)
 object ReceiptDetailsScreen:AppNavigationItems(ScreensRouteTypes.ReceiptDetailsScreen.name)
 object AddClerkScreen:AppNavigationItems(ScreensRouteTypes.AddClerkScreen.name)
 object ActivationScreen:AppNavigationItems(ScreensRouteTypes.ActivationScreen.name)
 object UserManagementScreen:AppNavigationItems(ScreensRouteTypes.UserManagementScreen.name)
 object BatchIdScreen:AppNavigationItems(ScreensRouteTypes.BatchIdScreen.name)
 object InactivityTimeoutScreen:AppNavigationItems(ScreensRouteTypes.InactivityTimeoutScreen.name)
 object SignatureScreen:AppNavigationItems(ScreensRouteTypes.SignatureScreen.name)
 object EBTSelScreen:AppNavigationItems(ScreensRouteTypes.EBTSelectionScreen.name)
 object TxnSelScreen:AppNavigationItems(ScreensRouteTypes.TxnSelectionScreen.name)
 object KeyEntryScreen:AppNavigationItems(ScreensRouteTypes.KeyEntryScreen.name)
 object VoucherScreen:AppNavigationItems(ScreensRouteTypes.VoucherCardScreen.name)
 object AuthCodeScreen:AppNavigationItems(ScreensRouteTypes.AuthScreen.name)
 object ReaderSettingScreen:AppNavigationItems(ScreensRouteTypes.ReaderSettingScreen.name)

 object PinScreen : AppNavigationItems(ScreensRouteTypes.PinScreen.name)

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