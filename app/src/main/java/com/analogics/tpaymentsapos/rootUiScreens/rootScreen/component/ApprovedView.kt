package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularMenu(
    onPrintClick: () -> Unit,
    onMenuOptionClick: (String) -> Unit
) {
    val menuOptions = listOf("E-RECEIPT", "Merchant Receipt", "Print")
    var expanded by remember { mutableStateOf(false) }
    val distance = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val printButtonInitialColor = colorResource(id = R.color.purple_200)
    var printButtonColor by remember { mutableStateOf(printButtonInitialColor) }

    LaunchedEffect(expanded) {
        distance.animateTo(
            targetValue = if (expanded) 80f else 0f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = Modifier
            .size(110.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        menuOptions.forEachIndexed { index, option ->
            val angle = when (index) {
                0 -> 0f // Right
                1 -> 180f // Left
                2 -> -90f // Up
                else -> 0f
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(
                        x = (distance.value * cos(Math.toRadians(angle.toDouble()))).dp,
                        y = (distance.value * sin(Math.toRadians(angle.toDouble()))).dp
                    )
                    .size(60.dp)
                    .shadow(4.dp, shape = CircleShape)
                    .background(colorResource(id = R.color.purple_200), shape = CircleShape)
                    .clickable {
                        onMenuOptionClick(option)
                        expanded = false
                        scope.launch {
                            printButtonColor = printButtonInitialColor
                        }
                    }
            ) {
                Text(
                    text = option,
                    color = Color.Black,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .shadow(4.dp, shape = CircleShape) // Add shadow with circular shape
                .background(printButtonColor, shape = CircleShape)
                .clickable {
                    scope.launch {
                        printButtonColor = if (expanded) {
                            Color.Gray
                        } else {
                            printButtonInitialColor
                        }
                    }
                    onPrintClick()
                    expanded = !expanded
                }
        ) {
            Text(
                text = "PRINT",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
fun ApprovedView(navHostController: NavHostController, totalAmount: String) {
    CommonLayout(
        title = "Approved",
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(10.dp)) // Blank space added here

        Text(
            text = "APPROVED",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Image(
            painter = painterResource(id = R.drawable.approve), // Replace with your image resource
            contentDescription = null, // Decorative image
            modifier = Modifier
                .size(110.dp)
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally) // Center the image
        )
        Spacer(modifier = Modifier.height(30.dp)) // Blank space added here

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularMenu(
                onPrintClick = {
                    // Do something on Print click
                },
                onMenuOptionClick = { option ->
                    // Handle circular menu option clicks
                    // For demonstration, navigate to EnterEmailScreen
                    navHostController?.navigate(AppNavigationItems.EnterEmailScreen.route)
                }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier.padding(top = 10.dp)
        ) {
            OkButton(
                onClick = {
                    navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                },
                title = "Done"
            )
        }
    }
}
