package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.activity

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
import com.analogics.tpaymentsapos.rootUiScreens.login.TrainingView
import com.analogics.tpaymentsapos.rootUiScreens.login.CardView
import com.analogics.tpaymentsapos.rootUiScreens.login.CardDetectView
import com.analogics.tpaymentsapos.rootUiScreens.login.PinView
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
    }
}
