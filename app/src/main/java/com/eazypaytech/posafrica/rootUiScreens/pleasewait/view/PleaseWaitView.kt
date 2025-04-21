package com.eazypaytech.posafrica.rootUiScreens.pleasewait.view

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.pleasewait.viewmodel.PleaseWaitViewModel
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PleaseWaitView(navHostController: NavHostController) {

    var isDialogVisible by remember { mutableStateOf(false) }

    CustomDialogBuilder.create()
        .setTitle(stringResource(id = R.string.processing))
        .setSubtitle(stringResource(id = R.string.plz_wait))
        .setSmallText(stringResource(id = R.string.processing))
        .setShowCloseButton(true) // Can set to false if you don't want the close button
        .setCancelable(true)
        .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
        .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
        .setNavAction {
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
        }
        .buildDialog(onClose = { isDialogVisible = false })
}
