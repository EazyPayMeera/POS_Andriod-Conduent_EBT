import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.IconButtonWithText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.MenuTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TopBoldText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens

val OrangeColor = Color(0xFFF7931E)

@Composable
fun TrainingView(navHostController: NavHostController) {
    // State to track which button is selected
    val selectedButton = remember { mutableStateOf<String?>(null) }

    Column {
        MenuTopAppBar(
            title = stringResource(id = R.string.training),
            onMenuItemClick = { option ->
                // Handle menu item click
                when (option) {
                    "Settings" -> {
                        navHostController?.navigate(AppNavigationItems.SettingsScreen.route)
                    }
                    "Option 2" -> {
                        // Handle Option 2 click
                    }
                }
            }
        )

        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                .height(MaterialTheme.dimens.DP_580_CompactMedium)
                .width(MaterialTheme.dimens.DP_420_CompactMedium),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            elevation = MaterialTheme.dimens.DP_20_CompactMedium
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_10_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBoldText(
                    text = stringResource(id = R.string.training),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium // Custom font size
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First set of buttons with images
                    IconButtonWithText(
                        text = stringResource(id = R.string.purchase),
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == stringResource(id = R.string.purchase),
                        onClick = {
                            selectedButton.value = "Purchase"
                            TransactionState.isPurchase = true
                            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
                        }
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_20_CompactMedium))

                    IconButtonWithText(
                        text = stringResource(id = R.string.refund),
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == stringResource(id = R.string.refund),
                        onClick = {
                            selectedButton.value = "Refund"
                            TransactionState.isRefund = true
                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Second set of buttons with images
                    IconButtonWithText(
                        text = stringResource(id = R.string.pre_auth),
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == stringResource(id = R.string.pre_auth),
                        onClick = {
                            selectedButton.value = "Pre-Auth"
                            TransactionState.isPreauth = true
                            navHostController.navigate(AppNavigationItems.PreauthScreen.route)
                        }
                    )
                    Spacer(modifier = Modifier.width(20.dp))

                    IconButtonWithText(
                        text = stringResource(id = R.string.void_trans),
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == stringResource(id = R.string.void_trans),
                        onClick = {
                            selectedButton.value = "Void"
                            TransactionState.isVoid = true
                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_33_CompactMedium))
                    // Third set of buttons with images
                    IconButtonWithText(
                        text = stringResource(id = R.string.transactions),
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == stringResource(id = R.string.transactions),
                        onClick = {
                            selectedButton.value = "Transactions"
                            navHostController.navigate(AppNavigationItems.ConfirmShiftScreen.route)
                        }
                    )
                }


                Box(
                    modifier = Modifier.padding(top = MaterialTheme.dimens.DP_10_CompactMedium)
                ) {
                    AppButton(
                        onClick = {
                            navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
                        },
                        title = stringResource(id = R.string.print_last_receipt),
                        image = painterResource(id = R.drawable.print)
                    )
                }
            }
        }
    }
}


