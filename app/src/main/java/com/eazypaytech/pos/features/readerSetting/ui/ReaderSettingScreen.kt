package com.eazypaytech.pos.features.readerSetting.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.pos.core.themes.dimens
import com.eazypaytech.pos.core.ui.components.inputfields.AppHeader
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.R


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
                isIcon1Visible = true,
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

                    Spacer(modifier = Modifier.height(24.dp))
                    ReaderToggleRow(
                        label = "TAP",
                        description = "Enable NFC tap payments",
                        isEnabled = tapEnabled,
                        onToggle = { viewModel.onTapToggle(sharedViewModel,it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // INSERT Toggle Row
                    ReaderToggleRow(
                        label = "INSERT",
                        description = "Enable chip card insert payments",
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


@Composable
fun ReaderToggleRow(
    label: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        SliderToggle(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}


@Composable
fun SliderToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val trackWidth = 56.dp
    val trackHeight = 30.dp
    val thumbSize = 24.dp
    val thumbPadding = 3.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    val trackColor by remember(checked) {
        derivedStateOf {
            if (checked) Color(0xFFF7931E) else Color(0xFFBDBDBD)
        }
    }

    Box(
        modifier = Modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}