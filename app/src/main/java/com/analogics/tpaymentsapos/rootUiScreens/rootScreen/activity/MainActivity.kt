package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.activity

import LanguageView
import TrainingView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.DashboardScreenView
import com.analogics.tpaymentsapos.rootUiScreens.login.AmountView
import com.analogics.tpaymentsapos.rootUiScreens.login.ApprovedView
import com.analogics.tpaymentsapos.rootUiScreens.login.ConfirmationView
import com.analogics.tpaymentsapos.rootUiScreens.login.LoginScreenView
import com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component.SplashScreenView
import com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component.OnBoardSlideView
import com.analogics.tpaymentsapos.rootUiScreens.login.ForgetPasswordView
import com.analogics.tpaymentsapos.rootUiScreens.login.PasswordView
import com.analogics.tpaymentsapos.rootUiScreens.login.InvoiceView
import com.analogics.tpaymentsapos.rootUiScreens.login.PleaseWaitView
import com.analogics.tpaymentsapos.rootUiScreens.login.TipView
import com.analogics.tpaymentsapos.rootUiScreens.login.CardView
import com.analogics.tpaymentsapos.rootUiScreens.login.CardDetectView
import com.analogics.tpaymentsapos.rootUiScreens.login.ConfigurationView
import com.analogics.tpaymentsapos.rootUiScreens.login.ConfirmShiftView
import com.analogics.tpaymentsapos.rootUiScreens.login.EmailView
import com.analogics.tpaymentsapos.rootUiScreens.login.EnterEmailView
import com.analogics.tpaymentsapos.rootUiScreens.login.PinView
import com.analogics.tpaymentsapos.rootUiScreens.login.PreauthView
import com.analogics.tpaymentsapos.rootUiScreens.login.RefundAmtView
import com.analogics.tpaymentsapos.rootUiScreens.login.SettingsView
import com.analogics.tpaymentsapos.rootUiScreens.login.TaxPercentageView
import com.analogics.tpaymentsapos.ui.theme.TPaymentsAPOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TPaymentsAPOSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationGraph(navHostController = rememberNavController())
                }
            }
        }
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
            DashboardScreenView(navHostController)
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
            PleaseWaitView(navHostController)
        }
        composable(AppNavigationItems.TrainingScreen.route) {
            TrainingView(navHostController)
        }
        composable(AppNavigationItems.AmountScreen.route) {
            AmountView(navHostController)
        }
        composable(AppNavigationItems.InvoiceScreen.route) {
            InvoiceView(navHostController)
        }
        composable(AppNavigationItems.PasswordScreen.route) {
            PasswordView(navHostController)
        }
        composable(
            route = AppNavigationItems.ConfirmationScreen.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.00"
            ConfirmationView(navHostController, amount)
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
            route = AppNavigationItems.CardScreen.route,
            arguments = listOf(navArgument("totalAmount") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount") ?: "0.00"
            CardView(navHostController, totalAmount)
        }
        composable(
            route = AppNavigationItems.CardDetectScreen.route,
            arguments = listOf(navArgument("totalAmount") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount") ?: "0.00"
            CardDetectView(navHostController, totalAmount)
        }
        composable(AppNavigationItems.PinScreen.route) {
            PinView(navHostController)
        }
        composable(
            route = AppNavigationItems.ApprovedScreen.route,
            arguments = listOf(navArgument("totalAmount") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount") ?: "0.00"
            ApprovedView(navHostController, totalAmount)
        }
        composable(AppNavigationItems.RefundAmtScreen.route) {
            RefundAmtView(navHostController)
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
        composable(AppNavigationItems.PreauthScreen.route) {
            PreauthView(navHostController)
        }
        composable(AppNavigationItems.EnterEmailScreen.route) {
            EnterEmailView(navHostController)
        }
    }
}
