package com.eazypaytech.posafrica.rootUiScreens.activity



import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activationScreen.view.ActivationScreen
import com.eazypaytech.posafrica.rootUiScreens.addClerk.view.AddClerkScreen
import com.eazypaytech.posafrica.rootUiScreens.amount.view.AmountView
import com.eazypaytech.posafrica.rootUiScreens.approved.view.ApprovedView
import com.eazypaytech.posafrica.rootUiScreens.barcode.BarcodeView
import com.eazypaytech.posafrica.rootUiScreens.batchId.view.BatchIdView
import com.eazypaytech.posafrica.rootUiScreens.carddetect.view.CardDetectView
import com.eazypaytech.posafrica.rootUiScreens.cardview.view.CardView
import com.eazypaytech.posafrica.rootUiScreens.changepassword.view.ChangePasswordView
import com.eazypaytech.posafrica.rootUiScreens.confirmation.view.ConfirmationView
import com.eazypaytech.posafrica.rootUiScreens.confirmshift.view.ConfirmShiftView
import com.eazypaytech.posafrica.rootUiScreens.dashboard.view.DashboardView
import com.eazypaytech.posafrica.rootUiScreens.decline.view.DeclineView
import com.eazypaytech.posafrica.rootUiScreens.email.view.EmailView
import com.eazypaytech.posafrica.rootUiScreens.enteremail.view.EnterEmailView
import com.eazypaytech.posafrica.rootUiScreens.forgetpassword.view.ForgetPasswordView
import com.eazypaytech.posafrica.rootUiScreens.inactivityTimeout.InactivityTimeoutView
import com.eazypaytech.posafrica.rootUiScreens.invoice.InvoiceView
import com.eazypaytech.posafrica.rootUiScreens.isinfo.InfoConfirmView
import com.eazypaytech.posafrica.rootUiScreens.language.view.LanguageView
import com.eazypaytech.posafrica.rootUiScreens.login.view.LoginScreenView
import com.eazypaytech.posafrica.rootUiScreens.onBoarding.view.OnBoardSlideView
import com.eazypaytech.posafrica.rootUiScreens.password.view.PasswordView
import com.eazypaytech.posafrica.rootUiScreens.pin.view.PinView
import com.eazypaytech.posafrica.rootUiScreens.pleasewait.view.PleaseWaitView
import com.eazypaytech.posafrica.rootUiScreens.preauth.view.PreauthView
import com.eazypaytech.posafrica.rootUiScreens.receiptdetails.view.ReceiptDetailsView
import com.eazypaytech.posafrica.rootUiScreens.settings.SettingsView
import com.eazypaytech.posafrica.rootUiScreens.settings.config.ConfigurationView
import com.eazypaytech.posafrica.rootUiScreens.signature.SignatureView
import com.eazypaytech.posafrica.rootUiScreens.splash.view.SplashScreenView
import com.eazypaytech.posafrica.rootUiScreens.sucess.SucessView
import com.eazypaytech.posafrica.rootUiScreens.tax.view.TaxPercentageView
import com.eazypaytech.posafrica.rootUiScreens.tip.view.TipPercentageView
import com.eazypaytech.posafrica.rootUiScreens.tip.view.TipView
import com.eazypaytech.posafrica.rootUiScreens.transactiondetails.TransactionDetailsView
import com.eazypaytech.posafrica.rootUiScreens.txnList.view.TransactionListScreen
import com.eazypaytech.posafrica.rootUiScreens.usermanagement.view.UserManagementView
import com.eazypaytech.posafrica.ui.theme.TPaymentsAPOSTheme
import dagger.hilt.android.AndroidEntryPoint

var localSharedViewModel= compositionLocalOf{SharedViewModel()}

const val STORAGE_PERMISSION_CODE = 23
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedViewModel:SharedViewModel= ViewModelProvider(this)[SharedViewModel::class.java]

        if (!checkStoragePermissions(this))
            requestStoragePermissions(this)

        setContent {
            TPaymentsAPOSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(localSharedViewModel provides sharedViewModel) {
                        AppNavigationGraph(navHostController = rememberNavController())
                    }

                }
            }
        }
    }
}

fun checkStoragePermissions(context: Context): Boolean {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            val read =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    } catch (_: Exception) {

    }

    return true
}

fun requestStoragePermissions(activity: Activity) {
    try {

        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.setData(uri)
                startActivity(activity, intent, Bundle())
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                activity,
                arrayOf<String>(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }

    } catch (_: Exception) {
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TPaymentsAPOSTheme {
        Greeting("Android")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: String = AppNavigationItems.SplashScreen.route
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable(AppNavigationItems.SplashScreen.route) {
            SplashScreenView(navHostController)
        }
        composable(AppNavigationItems.DashBoardScreen.route) {
            DashboardView(navHostController)
        }
        composable(AppNavigationItems.OnBoardingScreen.route) {
            OnBoardSlideView(navHostController)
        }
        composable(AppNavigationItems.LoginScreen.route) {
            LoginScreenView(navHostController)
        }
        composable(AppNavigationItems.ForgetPasswordScreen.route) {
            ForgetPasswordView(navHostController)
        }
        composable(AppNavigationItems.PleaseWaitScreen.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PleaseWaitView(navHostController)
            }
        }
        composable(AppNavigationItems.AmountScreen.route) {
            AmountView(navHostController)
        }
        composable(AppNavigationItems.ReceiptDetailsScreen.route) {
            ReceiptDetailsView(navHostController)
        }
        composable(AppNavigationItems.InvoiceScreen.route) {
            InvoiceView(navHostController)
        }
        composable(AppNavigationItems.PasswordScreen.route) {
            PasswordView(navHostController)
        }
        composable(AppNavigationItems.BarcodeScreen.route) {
            BarcodeView(navHostController)
        }
        composable(AppNavigationItems.ConfirmationScreen.route) { entry->
            val customTipAmount = entry.savedStateHandle.get<Double?>(AppConstants.NAV_KEY_CUSTOM_TIP_AMOUNT)
            entry.savedStateHandle.remove<Double?>(AppConstants.NAV_KEY_CUSTOM_TIP_AMOUNT)
            ConfirmationView(navHostController, customTipAmount)
        }
        composable(AppNavigationItems.TipScreen.route) {
            TipView(navHostController)
        }
        composable(
            route = AppNavigationItems.EmailScreen.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailView(navHostController, email)
        }
        composable(
            route = AppNavigationItems.CardScreen.route
        ) { backStackEntry ->
            CardView(navHostController)
        }

        composable(AppNavigationItems.CardDetectScreen.route) {
            CardDetectView(navHostController)
        }
        composable(AppNavigationItems.PinScreen.route) {
            PinView(navHostController)
        }
        composable(
            route = AppNavigationItems.ApprovedScreen.route,
            arguments = listOf(navArgument("totalAmount") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount") ?: "0.00"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ApprovedView(navHostController)
            }
        }
        composable(AppNavigationItems.SettingsScreen.route) {
            SettingsView(navHostController)

        }
        composable(AppNavigationItems.LanguageScreen.route) {
            LanguageView(navHostController)
        }
        composable(AppNavigationItems.ConfigurationScreen.route) {
            ConfigurationView(navHostController)
        }
        composable(AppNavigationItems.ConfirmShiftScreen.route) {
            ConfirmShiftView(navHostController)
        }
        composable(AppNavigationItems.TaxPercentageScreen.route) {
            TaxPercentageView(navHostController)
        }
        composable(AppNavigationItems.TipPercentageScreen.route) {
            TipPercentageView(navHostController)
        }
        composable(AppNavigationItems.PreauthScreen.route) {
            PreauthView(navHostController)
        }
        composable(AppNavigationItems.EnterEmailScreen.route) {
            EnterEmailView(navHostController)
        }
        composable(AppNavigationItems.InfoConfirmScreen.route) {
            InfoConfirmView(navHostController)
        }
        composable(AppNavigationItems.TxnListScreen.route) {
           TransactionListScreen(navHostController)
        }
        composable(AppNavigationItems.SucessScreen.route) {
            SucessView(navHostController)
        }
        composable(AppNavigationItems.ChangePasswordScreen.route) {
            ChangePasswordView(navHostController)
        }
        composable(AppNavigationItems.TransactionDetailsScreen.route) {
            TransactionDetailsView(navHostController)
        }
        composable(AppNavigationItems.AddClerkScreen.route) {
            AddClerkScreen(navHostController)
        }
        composable(AppNavigationItems.ActivationScreen.route) {
            ActivationScreen(navHostController)
        }
        composable(AppNavigationItems.UserManagementScreen.route) {
            UserManagementView(navHostController)
        }
        composable(AppNavigationItems.BatchIdScreen.route) {
            BatchIdView(navHostController)
        }
        composable(AppNavigationItems.InactivityTimeoutScreen.route) {
            InactivityTimeoutView(navHostController)
        }
        composable(AppNavigationItems.SignatureScreen.route) {
            SignatureView(navHostController)
        }
        composable(
            route = AppNavigationItems.DeclineScreen.route,
            arguments = listOf(navArgument("totalAmount") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount") ?: "0.00"
            DeclineView(navHostController, totalAmount)
        }
    }
}
