package com.analogics.tpaymentsapos.rootUiScreens.hostProcessing.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.login.viewModel.LoginViewModel
import com.analogics.tpaymentsapos.ui.theme.dimens

/*@Composable
fun HostProcessingDialog(
    navHostController: NavHostController?,
    viewModel: LoginViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(MaterialTheme.dimens.DP_0_CompactMedium)
                .fillMaxSize()
        ) {
            HostProcessingContent(navHostController, viewModel)
        }
    }
}*/

@Composable
fun HostProcessingDialog(
    navHostController: NavHostController?,
    viewModel: LoginViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Optional background
            .clickable { onDismissRequest() }, // Dismiss on outside click if desired
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()  // Ensures full-screen content without padding
        ) {
            HostProcessingContent(navHostController)
        }
    }
}

@Composable
fun HostProcessingContent(navHostController: NavHostController?) {
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
            .padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
    ) {



    }
}
