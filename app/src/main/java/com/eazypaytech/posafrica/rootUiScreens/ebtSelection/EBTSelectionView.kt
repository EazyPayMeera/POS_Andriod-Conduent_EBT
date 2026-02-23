package com.eazypaytech.posafrica.rootUiScreens.ebtSelection

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.approved.viewmodel.ApprovedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.BackgroundScreen
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OkButton
import com.eazypaytech.posafrica.ui.theme.dimens


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EBTSelectionView(navHostController: NavHostController) {

    val context = LocalContext.current
    val viewModel: ApprovedViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    Column {
        CommonTopAppBar(
            onBackButtonClick = { },
            showBackIcon = false
        )

        // Outer Surface with background color, padding, and rounded corners
        BackgroundScreen(
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the content inside the inner Surface
                    .fillMaxSize(), // Fill the entire available space
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_24_CompactMedium)) // Blank space

                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_180_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.CardScreen.route)
                        },
                        title = stringResource(id = R.string.snap),
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_21_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.CardScreen.route)
                        },
                        title = stringResource(id = R.string.cash),
                    )
                }

                CustomDialogBuilder.ShowComposed()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(context,sharedViewModel)
    }
}

