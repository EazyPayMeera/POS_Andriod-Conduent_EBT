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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.dashboard.DashboardScreenView
import com.analogics.tpaymentsapos.rootUiScreens.login.LoginScreenView
import com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component.SplashScreenView
import com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component.OnBoardSlideView
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
                    AppNavigationGraph(navHostController = rememberNavController() )
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
fun AppNavigationGraph(modifier: Modifier=Modifier,
                       navHostController: NavHostController,
                       startDestination:String=AppNavigationItems.SplashScreen.route)
{
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
        composable(AppNavigationItems.OnBoardingScreen.route)
        {
            OnBoardSlideView(navHostController)
        }
        composable(AppNavigationItems.LoginScreen.route)
        {
            LoginScreenView(navHostController)
        }
    }
}