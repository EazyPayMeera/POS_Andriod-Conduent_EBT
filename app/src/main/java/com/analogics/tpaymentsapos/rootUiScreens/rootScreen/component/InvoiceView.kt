package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ScannerButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun InvoiceView(navHostController: NavHostController) {
    var invoiceno by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = Authorisation.isNewauth
    val isAuthcap = Authorisation.isAuthcap

    // Print the value of isAuthcap to logcat
    Log.d("InvoiceView", "isAuthcap: $isAuthcap")

    Column {
        CommonTopAppBar(
            title = if (isRefund) "Refund" else "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = "Enter the Invoice Number",
            label = "Invoice Number",
            placeholder = "Invoice Number",
            value = invoiceno,
            onValueChange = { invoiceno = it },
            onDoneAction = { navHostController.navigate(AppNavigationItems.AmountScreen.route) },
            isRefund = isRefund,
            isVoid = isVoid,
            isAuthcap = isAuthcap,
            keyboardType = KeyboardType.Text
        ) {
            // Log values of all relevant variables
            Log.d("InvoiceView", "isRefund: $isRefund, isVoid: $isVoid, isPreauth: $isPreauth, isAuthcap: $isAuthcap")

            if (isRefund || isVoid || isAuthcap) {
                Log.d("InvoiceView", "Inside if condition, showing ScannerButton")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "-----------or-----------",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                ScannerButton(
                    text = "Scan QR/Barcode",
                    onClick = { showMenu = !showMenu },
                    backgroundColor = Color(0xFFEDEDED),
                    contentColor = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}



@Composable
fun CircularMenu(
    isVisible: Boolean,
    onOptionClick: (String) -> Unit
) {
    val menuItems = listOf("Option 1", "Option 2", "Option 3")
    val itemCount = menuItems.size
    val angleIncrement = (2 * Math.PI) / itemCount

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, shape = CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                menuItems.forEachIndexed { index, item ->
                    val angle = angleIncrement * index
                    val x = (100 * cos(angle)).toFloat()
                    val y = (100 * sin(angle)).toFloat()

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .offset(x.dp, y.dp)
                            .alpha(alpha)
                            .background(Color.LightGray, shape = CircleShape)
                            .align(Alignment.Center)
                    ) {
                        Button(
                            onClick = { onOptionClick(item) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = item, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
