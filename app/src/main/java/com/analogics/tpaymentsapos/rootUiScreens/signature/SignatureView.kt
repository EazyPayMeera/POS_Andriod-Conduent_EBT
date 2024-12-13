package com.analogics.tpaymentsapos.rootUiScreens.signature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SignatureBox
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureView(navHostController: NavHostController) {
    val touchPoints = remember { mutableStateListOf<Offset>() }
    val viewModel: SignatureViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current  // Adjusted to directly assign the shared view model

    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {

                TextView(
                    text = "Please Sign Inside the Box",
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                // Signature Box
                SignatureBox(
                    touchPoints = touchPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )
            }
        }
    }

    FooterButtons(
        firstButtonTitle = stringResource(id = R.string.cancel_btn),
        firstButtonOnClick = { touchPoints.clear() },
        secondButtonTitle = stringResource(id = R.string.confirm_btn),
        secondButtonOnClick = {
            viewModel.onDoneButtonClick(navHostController, sharedViewModel,touchPoints)
        }
    )
}



