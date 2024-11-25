package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.decline.viewmodel.DeclineViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun DeclineView(navHostController: NavHostController, totalAmount: String) {
    val viewModel: DeclineViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    // Resolve the string resources here in the composable context
    val custRecp = stringResource(id = R.string.cust_recp)
    val merchantRecp = stringResource(id = R.string.merchant_recp)
    val eRecp = stringResource(id = R.string.e_recp)

    Column {
        // Top App Bar with back button
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )
        BackgroundScreen(
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the content inside the inner Surface
                    .fillMaxSize(), // Fill the entire available space
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space

                // Approved TextView
                TextView(
                    text = stringResource(id = R.string.decline),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_31_CompactMedium)) // Blank space

                // Image for approval
                ImageView(
                    imageId = R.drawable.decline, // Replace with your image resource
                    size = MaterialTheme.dimens.DP_110_CompactMedium,
                    alignment = Alignment.Center, // Align image horizontally within the Box
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = "" // Align the Box horizontally within the parent
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_31_CompactMedium)) // Blank space

                // Circular Menu with Print and menu option handling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium), // Optional padding for horizontal spacing
                    contentAlignment = Alignment.Center
                ) {
                    CircularMenu(
                        onMenuOptionClick = { option ->

                            when (option) {
                                custRecp -> {
                                    navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
                                }
                                merchantRecp -> {
                                    navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
                                }
                                eRecp -> {
                                    // Handle e receipt click or any specific action here
                                }
                            }
                        }
                    )
                }

                // Done button at the bottom
                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_55_CompactMedium)
                        .align(Alignment.CenterHorizontally), // Aligns the Box itself horizontally within the parent
                    contentAlignment = Alignment.Center // Centers the OkButton within the Box
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                        },
                        title = stringResource(id = R.string.done),
                    )
                }
            }
        }
    }
}
