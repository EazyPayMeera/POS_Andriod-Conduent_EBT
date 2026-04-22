package com.eazypaytech.pos.features.readerSetting.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.pos.core.themes.dimens
import com.eazypaytech.pos.core.ui.components.inputfields.AppHeader
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.R
import com.eazypaytech.pos.core.ui.components.inputfields.ReaderToggleRow

@Composable
fun ReaderSettingScreen(
    navHostController: NavHostController,
    viewModel: ReaderSettingViewModel = hiltViewModel()
) {
    val sharedViewModel = localSharedViewModel.current

    val tapEnabled = viewModel.isTapEnabled.value
    val insertEnabled = viewModel.isInsertEnabled.value

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.reader_setting),
                onBackButtonClick = { navHostController.popBackStack() },
                icon1 = R.drawable.baseline_arrow_back_24,
                onIcon1Click = { navHostController.popBackStack() },
                backgroundColor = MaterialTheme.colorScheme.onPrimary,
                isIcon2Visible = false
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
                ) {

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_23_CompactMedium))
                    ReaderToggleRow(
                        label = stringResource(id = R.string.tap),
                        description = stringResource(id = R.string.enable_nfc),
                        isEnabled = tapEnabled,
                        onToggle = { viewModel.onTapToggle(sharedViewModel,it) }
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_23_CompactMedium))

                    // INSERT Toggle Row
                    ReaderToggleRow(
                        label = stringResource(id = R.string.insert),
                        description = stringResource(id = R.string.enable_chip),
                        isEnabled = insertEnabled,
                        onToggle = { viewModel.onInsertToggle(sharedViewModel,it) }
                    )

                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.initOnce(sharedViewModel)
    }
}


