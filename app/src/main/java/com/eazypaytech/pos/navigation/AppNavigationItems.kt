package com.eazypaytech.pos.navigation


/**
 * Defines all navigation routes used in the application.
 *
 * Behavior:
 * - Represents each screen as a sealed class object
 * - Uses enum-based route names for consistency
 * - Ensures type-safe navigation across the app
 *
 * Usage:
 * - Use these objects with NavController for navigation
 * - Example: navController.navigate(AppNavigationItems.LoginScreen.route)
 *
 * @property route Unique route string for each screen
 */
sealed class AppNavigationItems(val route: String) {
 object SplashScreen : AppNavigationItems(NavScreensEnum.SplashScreen.name)
 object DashBoardScreen : AppNavigationItems(NavScreensEnum.DashBoardScreen.name)
 object OnBoardingScreen : AppNavigationItems(NavScreensEnum.OnBoardingScreen.name)
 object LoginScreen : AppNavigationItems(NavScreensEnum.LoginScreen.name)
 object AmountScreen : AppNavigationItems(NavScreensEnum.AmountScreen.name)
 object CardScreen : AppNavigationItems(NavScreensEnum.CardScreen.name)
 object CashBackScreen : AppNavigationItems(NavScreensEnum.CashBackScreen.name)
 object ManualCardScreen : AppNavigationItems(NavScreensEnum.ManualCardScreen.name)
 object PasswordScreen : AppNavigationItems(NavScreensEnum.PasswordScreen.name)
 object SettingsScreen : AppNavigationItems(NavScreensEnum.SettingsScreen.name)
 object LanguageScreen : AppNavigationItems(NavScreensEnum.LanguageScreen.name)
 object ConfigurationScreen : AppNavigationItems(NavScreensEnum.ConfigurationScreen.name)
 object ConfirmShiftScreen : AppNavigationItems(NavScreensEnum.ConfirmShiftScreen.name)
 object ChangePasswordScreen:AppNavigationItems(NavScreensEnum.ChangePasswordScreen.name)
 object ReceiptDetailsScreen:AppNavigationItems(NavScreensEnum.ReceiptDetailsScreen.name)
 object AddClerkScreen:AppNavigationItems(NavScreensEnum.AddClerkScreen.name)
 object ActivationScreen:AppNavigationItems(NavScreensEnum.ActivationScreen.name)
 object UserManagementScreen:AppNavigationItems(NavScreensEnum.UserManagementScreen.name)
 object EBTSelScreen:AppNavigationItems(NavScreensEnum.EBTSelectionScreen.name)
 object TxnSelScreen:AppNavigationItems(NavScreensEnum.TxnSelectionScreen.name)
 object KeyEntryScreen:AppNavigationItems(NavScreensEnum.KeyEntryScreen.name)
 object VoucherScreen:AppNavigationItems(NavScreensEnum.VoucherCardScreen.name)
 object AuthCodeScreen:AppNavigationItems(NavScreensEnum.AuthCodeScreen.name)
 object ReaderSettingScreen:AppNavigationItems(NavScreensEnum.ReaderSettingScreen.name)
 object ApprovedScreen:AppNavigationItems(NavScreensEnum.ApprovedScreen.name)

}
