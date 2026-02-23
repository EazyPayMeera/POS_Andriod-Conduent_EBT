package com.eazypaytech.posafrica.rootUiScreens.activationScreen.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.rootUiScreens.activationScreen.viewModel.ActivationViewModel
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView




@Composable
fun ActivationScreen(
    navHostController: NavHostController,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current
    AppLogger.log(context, "ACTIVATION", "Starting activation")
    Scaffold(
        topBar = {

        }
    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextView(
                    text = "Connecting to Host...",
                    fontSize = 18.sp
                )
            }
        }

        CustomDialogBuilder.ShowComposed()
    }

    LaunchedEffect(Unit) {
        viewModel.sharedViewModel = sharedViewModel
        viewModel.navHostController = navHostController
        viewModel.startActivateProcess()
    }
}


