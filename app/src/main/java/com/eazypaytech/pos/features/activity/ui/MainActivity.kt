package com.eazypaytech.pos.features.activity.ui

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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eazypaytech.pos.features.activation.ui.ActivationScreen
import com.eazypaytech.pos.features.addClerk.ui.AddClerkScreen
import com.eazypaytech.pos.features.confirmshift.ui.ConfirmShiftView
import com.eazypaytech.pos.features.ebtSelection.ui.EBTSelectionView
import com.eazypaytech.pos.features.login.ui.LoginScreenView
import com.eazypaytech.pos.features.onBoarding.ui.OnBoardSlideView
import com.eazypaytech.pos.core.themes.TPaymentsAPOSTheme
import com.eazypaytech.pos.features.amount.ui.AmountScreen
import com.eazypaytech.pos.features.amount.ui.CashBackScreen
import com.eazypaytech.pos.features.approved.ui.ApprovedScreen
import com.eazypaytech.pos.features.authCode.ui.AuthScreen
import com.eazypaytech.pos.features.cards.ui.CardScreen
import com.eazypaytech.pos.features.changepassword.ui.ChangePasswordScreen
import com.eazypaytech.pos.features.dashboard.ui.DashboardScreen
import com.eazypaytech.pos.features.keyManagement.ui.KeyEntryScreen
import com.eazypaytech.pos.features.language.ui.LanguageScreen
import com.eazypaytech.pos.features.manualentry.ui.ManualCardScreen
import com.eazypaytech.pos.features.password.ui.PasswordScreen
import com.eazypaytech.pos.features.receiptdetails.ui.ReceiptDetailsScreen
import com.eazypaytech.pos.features.configuration.ui.ConfigurationScreen
import com.eazypaytech.pos.features.settings.ui.SettingsScreen
import com.eazypaytech.pos.features.splash.ui.SplashScreen
import com.eazypaytech.pos.features.txnSel.ui.TxnSelectionScreen
import com.eazypaytech.pos.features.usermanagement.ui.UserManagementScreen
import com.eazypaytech.pos.features.voucher.ui.VoucherCardScreen
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.readerSetting.ui.ReaderSettingScreen
import dagger.hilt.android.AndroidEntryPoint

var localSharedViewModel= compositionLocalOf{ SharedViewModel() }

const val STORAGE_PERMISSION_CODE = 23
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedViewModel: SharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )

        if (!checkStoragePermissions(this))
            requestStoragePermissions(this)

        setContent {
            TPaymentsAPOSTheme {
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
            return Environment.isExternalStorageManager()
        } else {
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
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(AppNavigationItems.SplashScreen.route) {
            SplashScreen(navHostController)
        }
        composable(AppNavigationItems.DashBoardScreen.route) {
            DashboardScreen(navHostController)
        }
        composable(AppNavigationItems.OnBoardingScreen.route) {
            OnBoardSlideView(navHostController)
        }
        composable(AppNavigationItems.LoginScreen.route) {
            LoginScreenView(navHostController)
        }
        composable(AppNavigationItems.AmountScreen.route) {
            AmountScreen(navHostController)
        }
        composable(AppNavigationItems.ReceiptDetailsScreen.route) {
            ReceiptDetailsScreen(navHostController)
        }
        composable(AppNavigationItems.PasswordScreen.route) {
            PasswordScreen(navHostController)
        }
        composable(AppNavigationItems.CardScreen.route) {
            CardScreen(navHostController)
        }
        composable(AppNavigationItems.ApprovedScreen.route) {
            ApprovedScreen(navHostController)
        }
        composable(AppNavigationItems.SettingsScreen.route) {
            SettingsScreen(navHostController)
        }
        composable(AppNavigationItems.LanguageScreen.route) {
            LanguageScreen(navHostController)
        }
        composable(AppNavigationItems.ConfigurationScreen.route) {
            ConfigurationScreen(navHostController)
        }
        composable(AppNavigationItems.ConfirmShiftScreen.route) {
            ConfirmShiftView(navHostController)
        }
        composable(AppNavigationItems.ChangePasswordScreen.route) {
            ChangePasswordScreen(navHostController)
        }
        composable(AppNavigationItems.AddClerkScreen.route) {
            AddClerkScreen(navHostController)
        }
        composable(AppNavigationItems.ActivationScreen.route) {
            ActivationScreen(navHostController)
        }
        composable(AppNavigationItems.UserManagementScreen.route) {
            UserManagementScreen(navHostController)
        }
        composable(AppNavigationItems.CashBackScreen.route) {
            CashBackScreen(navHostController)
        }
        composable(AppNavigationItems.ManualCardScreen.route) {
            ManualCardScreen(navHostController)
        }
        composable(AppNavigationItems.EBTSelScreen.route) {
            EBTSelectionView(navHostController)
        }
        composable(AppNavigationItems.TxnSelScreen.route) {
            TxnSelectionScreen(navHostController)
        }
        composable(AppNavigationItems.KeyEntryScreen.route) {
            KeyEntryScreen(navHostController)
        }
        composable(AppNavigationItems.VoucherScreen.route) {
            VoucherCardScreen(navHostController)
        }
        composable(AppNavigationItems.AuthCodeScreen.route) {
            AuthScreen(navHostController)
        }
        composable(AppNavigationItems.ReaderSettingScreen.route) {
            ReaderSettingScreen(navHostController)
        }
    }
}
