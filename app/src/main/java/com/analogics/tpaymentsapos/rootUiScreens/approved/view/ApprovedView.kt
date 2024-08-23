package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel.ApprovedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularMenu(
    onPrintClick: () -> Unit,
    onMenuOptionClick: (String) -> Unit
) {
    val menuOptions = listOf("Customer Receipt", "Merchant Receipt", "E-RECEIPT")
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
            .size(MaterialTheme.dimens.DP_120_CompactMedium)
            .padding(MaterialTheme.dimens.DP_21_CompactMedium),
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
                    .size(MaterialTheme.dimens.DP_60_CompactMedium)
                    .shadow(MaterialTheme.dimens.DP_4_CompactMedium, shape = CircleShape)
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
                    fontSize = MaterialTheme.dimens.SP_8_CompactMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MaterialTheme.dimens.DP_200_CompactMedium)
                .shadow(MaterialTheme.dimens.DP_4_CompactMedium, shape = CircleShape) // Add shadow with circular shape
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
                text = stringResource(id = R.string.print),
                color = Color.Black,
                fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
fun ApprovedView(navHostController: NavHostController, totalAmount: String) {

    // Get the context
    val context = LocalContext.current

    // Create the ViewModel with the context
    val viewModel: ApprovedViewModel = viewModel { ApprovedViewModel(context) }

    val printStatus by viewModel.printStatus
    val errorMessage by viewModel.errorMessage

    Column {
        // Top App Bar with back button
        CommonTopAppBar(
            title = stringResource(id = R.string.approved),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Outer Surface with background color, padding, and rounded corners
        BackgroundScreen(
//            color = Color(0xFFF7931E), // Orange color for the outer Surface
//            modifier = Modifier
//                .padding(MaterialTheme.dimens.DP_25_CompactMedium) // Padding for the outer Surface
//                .height(MaterialTheme.dimens.DP_540_CompactMedium) // Adjust the height as per your requirement
//                .width(MaterialTheme.dimens.DP_410_CompactMedium), // Adjust the width as per your requirement
//            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium) // Rounded corners for the outer Surface
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
                    text = stringResource(id = R.string.approved),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_31_CompactMedium)) // Blank space

                // Image for approval
                ImageView(
                    imageId = R.drawable.approve, // Replace with your image resource
                    size = MaterialTheme.dimens.DP_110_CompactMedium,
                    alignment = Alignment.Center, // Align image horizontally within the Box
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Align the Box horizontally within the parent
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
                        onPrintClick = {
                            viewModel.initPrint(context)
                            viewModel.GetStatus()
                        },
                        onMenuOptionClick = { option ->
                            when (option) {
                                "Customer Receipt" -> {
                                    viewModel.addTextDetails("Hello World")
                                    viewModel.printReceipt(context)
                                }
                                "Merchant Receipt" -> {
                                    // Handle Merchant Receipt click
                                    navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
                                }
                                "E-RECEIPT" -> {

                                }
                            }
                        }
                    )
                }

                //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium)) // Blank space

                // Done button at the bottom
                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_55_CompactMedium)
                        .align(Alignment.CenterHorizontally), // Aligns the Box itself horizontally within the parent
                    contentAlignment = Alignment.Center // Centers the OkButton within the Box
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                        },
                        title = stringResource(id = R.string.done),
                    )
                }
            }
        }
    }
}



