package com.eazypaytech.posafrica.rootUiScreens.sucess

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.ui.theme.BgColor
import com.eazypaytech.posafrica.ui.theme.dimens

@Composable
fun SucessView(navHostController: NavHostController) {
    
    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium,
            backgroundColor = BgColor
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_40_CompactMedium)
            ) {

                TextView(
                    text = stringResource(id = R.string.payment),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    //modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                TextView(
                    text = stringResource(id = R.string.successful),
                    fontSize = MaterialTheme.dimens.SP_28_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    //modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                ImageView(
                    imageId = R.drawable.sucess,
                    size = MaterialTheme.dimens.DP_250_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = ""
                )

            }
        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.e_recp),
            firstButtonOnClick = {navHostController.navigate(AppNavigationItems.EnterEmailScreen.route) },
            secondButtonTitle = stringResource(id = R.string.home),
            secondButtonOnClick = {navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route) }
        )
    }
}