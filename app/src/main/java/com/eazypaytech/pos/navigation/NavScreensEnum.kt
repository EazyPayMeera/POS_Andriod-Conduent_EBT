

/**
 * Defines all screen identifiers used for navigation.
 *
 * Behavior:
 * - Provides a centralized list of all screen names
 * - Used as a source for generating navigation routes
 * - Ensures consistency between navigation and screen references
 *
 * Usage:
 * - Typically accessed via `.name` to create route strings
 * - Example: NavScreensEnum.LoginScreen.name
 *
 * Note:
 * - Any new screen added to the app should be declared here
 * - Keep naming consistent with corresponding UI screens
 */
enum class NavScreensEnum {
    SplashScreen,DashBoardScreen,OnBoardingScreen,LoginScreen,ForgetPasswordView,PleaseWaitView,AmountScreen,InvoiceView,ConfirmationView,TipView,ServiceChargeView,CardScreen,CardDetectView,PinView,ApprovedScreen,PasswordScreen,EmailView,RefundAmtView,SettingsScreen,LanguageScreen,ConfigurationScreen,ConfirmShiftScreen,TaxPercentageView,TipPercentageView,ServiceChargePercentageView,PreauthView,EnterEmailView,DeclineView,Drawer,TxnListView,InfoConfirmView,SucessView,ChangePasswordScreen,BarcodeView,TransactionDetailsView,ReceiptDetailsScreen,UserManagementScreen,BatchIdView,InactivityTimeoutView,SignatureView,EBTSelectionScreen,TxnSelectionScreen,KeyEntryScreen,VoucherCardScreen,AuthCodeScreen
    ,AddClerkScreen,ActivationScreen,CashBackScreen,ManualCardScreen,ReaderSettingScreen
}