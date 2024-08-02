import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.IconButtonWithText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState

val OrangeColor = Color(0xFFF7931E)

@Composable
fun TrainingView(navHostController: NavHostController) {
    // State to track which button is selected
    val selectedButton = remember { mutableStateOf<String?>(null) }

    Column {
        CommonTopAppBar(
            title = "Training",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(25.dp)
                .fillMaxWidth()
                .height(540.dp)
                .width(430.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Training",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First set of buttons with images
                    IconButtonWithText(
                        text = "Purchase",
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == "Purchase",
                        onClick = {
                            selectedButton.value = "Purchase"
                            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
                        }
                    )

                    IconButtonWithText(
                        text = "Refund",
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == "Refund",
                        onClick = {
                            selectedButton.value = "Refund"
                            TransactionState.isRefund = true
                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Second set of buttons with images
                    IconButtonWithText(
                        text = "Pre-Auth",
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == "Pre-Auth",
                        onClick = {
                            selectedButton.value = "Pre-Auth"
                            TransactionState.isPreauth = true
                            navHostController.navigate(AppNavigationItems.PreauthScreen.route)
                        }
                    )

                    IconButtonWithText(
                        text = "Void",
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == "Void",
                        onClick = {
                            selectedButton.value = "Void"
                            TransactionState.isVoid = true
                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Third set of buttons with images
                    IconButtonWithText(
                        text = "Transactions",
                        icon = painterResource(id = R.drawable.card),
                        isSelected = selectedButton.value == "Transactions",
                        onClick = {
                            selectedButton.value = "Transactions"
                            navHostController.navigate(AppNavigationItems.TaxPercentageScreen.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fourth set of buttons with images
                    Button(
                        onClick = { /* Handle print receipt click */ },
                        modifier = Modifier
                            .size(260.dp)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = OrangeColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Print Last Receipt", color = Color.Black)
                    }
                }
            }
        }
    }
}


