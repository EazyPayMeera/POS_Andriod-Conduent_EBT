package com.eazypaytech.posafrica.rootUiScreens.preauth.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ScannerButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.ui.theme.dimens

@Composable
fun PreauthView(navHostController: NavHostController) {

    Column {

        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {

                TextView(
                    text = stringResource(id = R.string.sel_pre_auth),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                // Image for approval
                ImageView(
                    imageId = R.drawable.card, // Replace with your image resource
                    size = MaterialTheme.dimens.DP_110_CompactMedium,
                    alignment = Alignment.Center, // Align image horizontally within the Box
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = "" // Align the Box horizontally within the parent
                )

                ScannerButton(
                    text = stringResource(id = R.string.new_auth),
                    onClick = {
                        navHostController.navigate(AppNavigationItems.InvoiceScreen.route)},
                    backgroundColor = MaterialTheme.colorScheme.onSecondary,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
                )
            }
        }
    }
}
