package com.eazypaytech.posafrica.features.activity.ui



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
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.features.activation.ui.ActivationScreen
import com.eazypaytech.posafrica.features.addClerk.ui.AddClerkScreen
import com.eazypaytech.posafrica.features.amount.ui.AmountView
import com.eazypaytech.posafrica.features.amount.ui.CashBackView
import com.eazypaytech.posafrica.features.approved.ui.ApprovedView
import com.eazypaytech.posafrica.features.authCode.ui.AuthCodeView
import com.eazypaytech.posafrica.features.cards.ui.CardView
import com.eazypaytech.posafrica.features.changepassword.ui.ChangePasswordView
import com.eazypaytech.posafrica.features.confirmshift.ui.ConfirmShiftView
import com.eazypaytech.posafrica.features.dashboard.ui.DashboardView
import com.eazypaytech.posafrica.features.ebtSelection.ui.EBTSelectionView
import com.eazypaytech.posafrica.features.keyManagement.KeyEntryView
import com.eazypaytech.posafrica.features.language.ui.LanguageView
import com.eazypaytech.posafrica.features.login.ui.LoginScreenView
import com.eazypaytech.posafrica.features.manualentry.ManualCardView
import com.eazypaytech.posafrica.features.onBoarding.ui.OnBoardSlideView
import com.eazypaytech.posafrica.features.password.ui.PasswordView
import com.eazypaytech.posafrica.features.receiptdetails.ui.ReceiptDetailsView
import com.eazypaytech.posafrica.features.settings.ui.SettingsView
import com.eazypaytech.posafrica.features.settings.ui.ConfigurationView
import com.eazypaytech.posafrica.features.splash.ui.SplashScreenView
import com.eazypaytech.posafrica.features.transactiondetails.ui.TransactionDetailsView
import com.eazypaytech.posafrica.features.txnList.ui.TransactionListScreen
import com.eazypaytech.posafrica.features.txnSel.ui.TxnSelectionView
import com.eazypaytech.posafrica.features.usermanagement.ui.UserManagementView
import com.eazypaytech.posafrica.features.voucher.VoucherCardView
import com.eazypaytech.posafrica.core.themes.TPaymentsAPOSTheme
import dagger.hilt.android.AndroidEntryPoint

var localSharedViewModel= compositionLocalOf{ SharedViewModel() }

const val STORAGE_PERMISSION_CODE = 23
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedViewModel: SharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

//        window.setFlags(    // to avoid screenshot and Screen Recording
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )

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
        composable(AppNavigationItems.AmountScreen.route) {
            AmountView(navHostController)
        }
        composable(AppNavigationItems.ReceiptDetailsScreen.route) {
            ReceiptDetailsView(navHostController)
        }
        composable(AppNavigationItems.InvoiceScreen.route) {
            //InvoiceView(navHostController)
        }
        composable(AppNavigationItems.PasswordScreen.route) {
            PasswordView(navHostController)
        }
        composable(AppNavigationItems.BarcodeScreen.route) {
            //BarcodeView(navHostController)
        }
        composable(AppNavigationItems.TipScreen.route) {
            //TipView(navHostController)
        }
        composable(AppNavigationItems.ServiceChargeScreen.route) {
            //ServiceChargeView(navHostController)
        }
        composable(
            route = AppNavigationItems.EmailScreen.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            //EmailView(navHostController, email)
        }
        composable(
            route = AppNavigationItems.CardScreen.route
        ) { backStackEntry ->
            CardView(navHostController)
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
            //TaxPercentageView(navHostController)
        }
        composable(AppNavigationItems.TipPercentageScreen.route) {
            //TipPercentageView(navHostController)
        }
        composable(AppNavigationItems.ServiceChargePercentageScreen.route) {
            //ServiceChargePercentageView(navHostController)
        }
        composable(AppNavigationItems.PreauthScreen.route) {
            //PreauthView(navHostController)
        }
        composable(AppNavigationItems.EnterEmailScreen.route) {
            //EnterEmailView(navHostController)
        }
        composable(AppNavigationItems.TxnListScreen.route) {
           TransactionListScreen(navHostController)
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
            //BatchIdView(navHostController)
        }
        composable(AppNavigationItems.SignatureScreen.route) {
            //SignatureView(navHostController)
        }
        composable(AppNavigationItems.CashBackScreen.route) {
            CashBackView(navHostController)
        }
        composable(AppNavigationItems.ManualCardScreen.route) {
            ManualCardView(navHostController)
        }
        composable(AppNavigationItems.EBTSelScreen.route) {
            EBTSelectionView(navHostController)
        }
        composable(AppNavigationItems.TxnSelScreen.route) {
            TxnSelectionView(navHostController)
        }
        composable(AppNavigationItems.KeyEntryScreen.route) {
            KeyEntryView(navHostController)
        }
        composable(AppNavigationItems.VoucherScreen.route) {
            VoucherCardView(navHostController)
        }
        composable(AppNavigationItems.AuthCodeScreen.route) {
            AuthCodeView(navHostController)
        }
    }
}
