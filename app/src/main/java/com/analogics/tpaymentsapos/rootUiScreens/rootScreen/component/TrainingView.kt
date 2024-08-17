

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.MenuTopAppBar

//val OrangeColor = Color(0xFFF7931E)

//@Composable
//fun TrainingView(navHostController: NavHostController) {
//    // State to track which button is selected
//    val selectedButton = remember { mutableStateOf<String?>(null) }
//
//    Column {
//        MenuTopAppBar(
//            title = "Training",
//            onMenuItemClick = { option ->
//                // Handle menu item click
//                when (option) {
//                    "Settings" -> {
//                        navHostController?.navigate(AppNavigationItems.SettingsScreen.route)
//                    }
//                    "Option 2" -> {
//                        // Handle Option 2 click
//                    }
//                }
//            }
//        )
//
//        Surface(
//            color = Color.White,
//            modifier = Modifier
//                .padding(25.dp)
//                .fillMaxWidth()
//                .height(540.dp)
//                .width(430.dp),
//            shape = RoundedCornerShape(18.dp),
//            elevation = 8.dp
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxSize(),
//                verticalArrangement = Arrangement.Top,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Training",
//                    fontSize = 20.sp,
//                    color = Color.Black,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .padding(bottom = 20.dp)
//                        .align(Alignment.CenterHorizontally)
//                )
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // First set of buttons with images
//                    IconButtonWithText(
//                        text = "Purchase",
//                        icon = painterResource(id = R.drawable.card),
//                        isSelected = selectedButton.value == "Purchase",
//                        onClick = {
//                            selectedButton.value = "Purchase"
//                            TransactionState.isPurchase = true
//                            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
//                        }
//                    )
//
//                    IconButtonWithText(
//                        text = "Refund",
//                        icon = painterResource(id = R.drawable.card),
//                        isSelected = selectedButton.value == "Refund",
//                        onClick = {
//                            selectedButton.value = "Refund"
//                            TransactionState.isRefund = true
//                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Second set of buttons with images
//                    IconButtonWithText(
//                        text = "Pre-Auth",
//                        icon = painterResource(id = R.drawable.card),
//                        isSelected = selectedButton.value == "Pre-Auth",
//                        onClick = {
//                            selectedButton.value = "Pre-Auth"
//                            TransactionState.isPreauth = true
//                            navHostController.navigate(AppNavigationItems.PreauthScreen.route)
//                        }
//                    )
//
//                    IconButtonWithText(
//                        text = "Void",
//                        icon = painterResource(id = R.drawable.card),
//                        isSelected = selectedButton.value == "Void",
//                        onClick = {
//                            selectedButton.value = "Void"
//                            TransactionState.isVoid = true
//                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Third set of buttons with images
//                    IconButtonWithText(
//                        text = "Transactions",
//                        icon = painterResource(id = R.drawable.card),
//                        isSelected = selectedButton.value == "Transactions",
//                        onClick = {
//                            selectedButton.value = "Transactions"
//                            navHostController.navigate(AppNavigationItems.TaxPercentageScreen.route)
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Fourth set of buttons with images
//                    Button(
//                        onClick = { /* Handle print receipt click */ },
//                        modifier = Modifier
//                            .size(260.dp)
//                            .padding(8.dp),
//                        colors = ButtonDefaults.buttonColors(backgroundColor = OrangeColor),
//                        shape = RoundedCornerShape(10.dp)
//                    ) {
//                        Text(text = "Print Last Receipt", color = Color.Black)
//                    }
//                }
//            }
//        }
//    }
//}


import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource

import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CardWithImageText
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.DrawerContent
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.MenuTopAppBar1
import kotlinx.coroutines.launch


data class ButtonConfig(
    val text: String,
    val iconResId: Int,
    val onClick: () -> Unit
)
val OrangeColor = Color(0xFFF7931E)


@Composable
fun DashboardView(navHostController: NavHostController){
    TrainingView(navHostController = navHostController, buttonConfigs =ProvideButtonConfigs(navHostController) ) {}
}


@Composable
fun ProvideButtonConfigs(navHostController: NavHostController): List<ButtonConfig> {
    return listOf(
        ButtonConfig(stringResource(id = R.string.purchase), R.drawable.dashboard_purchase, onClick = {
            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)

        }),
        ButtonConfig(stringResource(id = R.string.refund), R.drawable.dashboard_refund,onClick = {
            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
        }),
        ButtonConfig(stringResource(id = R.string.pre_auth), R.drawable.dashboard_preauth,onClick = {
            navHostController.navigate(AppNavigationItems.PreauthScreen.route)

        }),
        ButtonConfig(stringResource(id = R.string.auth_capture), R.drawable.dashboard_auth_capture,onClick = {
            navHostController.navigate(AppNavigationItems.PasswordScreen.route)

        }),
        ButtonConfig(stringResource(id = R.string.void_trans), R.drawable.dashboard_void,onClick = {
            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
        }),
        ButtonConfig(stringResource(id = R.string.transactions), R.drawable.dashboard_transaction,onClick= {
            navHostController.navigate(AppNavigationItems.PasswordScreen.route)

        }))
}
@Composable
fun TrainingView(
    navHostController: NavHostController,
    buttonConfigs: List<ButtonConfig>,
    onMenuItemClick: (String) -> Unit
) {
    // State to track which button is selected
    val selectedButton = remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Function to handle drawer open and close
    fun toggleDrawer(open: Boolean) {
        coroutineScope.launch {
            if (open) drawerState.open() else drawerState.close()
        }
    }

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navHostController = navHostController,
                onMenuItemClick = onMenuItemClick
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.training), color = Color.Black)
                    },
                    navigationIcon = {
                        IconButton(onClick = { toggleDrawer(true) }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                        }
                    },
                    backgroundColor = Color.White
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ContentSurface(
                        buttonConfigs = buttonConfigs,
                        selectedButton = selectedButton.value,
                        onButtonClick = { text, onClick ->
                            selectedButton.value = text
                            onClick()
                        }
                    )
                }
            }
        )
    }
}


@Composable
fun ContentSurface(
    buttonConfigs: List<ButtonConfig>,
    selectedButton: String?,
    onButtonClick: (String, () -> Unit) -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.training),
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            buttonConfigs.chunked(2).forEach { rowConfigs ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowConfigs.forEach { config ->
                        CardWithImageText(
                            text = config.text,
                            imageResId = config.iconResId,
                            isSelected = selectedButton == config.text,
                            onClick = { onButtonClick(config.text, config.onClick) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    onClick = { /* Handle print receipt click */ },
                    title = stringResource(id = R.string.print_last_receipt),
                )
            }
        }
    }
}
