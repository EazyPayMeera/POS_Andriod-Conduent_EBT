package com.analogics.tpaymentsapos.rootUiScreens.login


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel.ApprovedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.carddetect.viewmodel.updated_amt
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toAmountFormat
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CircularMenu(
    onMenuOptionClick: (String) -> Unit
) {
    val menuOptions = listOf(
        stringResource(id = R.string.cust_recp), stringResource(id = R.string.merchant_recp), stringResource(
        id = R.string.e_recp
    ))
    var expanded by remember { mutableStateOf(false) }
    val distance = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val printButtonInitialColor = MaterialTheme.colorScheme.primary
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
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                    .clickable {
                        onMenuOptionClick(option)
                        expanded = false
                        scope.launch {
                            printButtonColor = printButtonInitialColor
                        }
                    }
            ) {
                TextView(
                    text = option,
                    color = MaterialTheme.colorScheme.tertiary,
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
                .shadow(
                    MaterialTheme.dimens.DP_4_CompactMedium,
                    shape = CircleShape
                ) // Add shadow with circular shape
                .background(printButtonColor, shape = CircleShape)
                .clickable {
                    scope.launch {
                        printButtonColor = if (expanded) {
                            Color.Gray
                        } else {
                            printButtonInitialColor
                        }
                    }
                    expanded = !expanded
                }
        ) {
            TextView(
                text = stringResource(id = R.string.print),
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}






@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovedView(navHostController: NavHostController) {
    val context = LocalContext.current
    //val viewModel: ApprovedViewModel = viewModel { ApprovedViewModel(context) }
    val viewModel: ApprovedViewModel = hiltViewModel()
    val printStatus by viewModel.printStatus
    val updatedAmount = updated_amt
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope

    val sharedViewModel = localSharedViewModel.current

    //viewModel.updateTxnData(sharedViewModel.objRootAppPaymentDetail)

    Column {
        CommonTopAppBar(
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
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_24_CompactMedium)) // Blank space

                TextView(
                    text = stringResource(id = R.string.approved),
                    fontSize = MaterialTheme.dimens.SP_29_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))
                sharedViewModel.objRootAppPaymentDetail.txnType.takeIf { it != TxnType.VOID }?.let {
                    Text(
                        text = sharedViewModel.objRootAppPaymentDetail.ttlAmount.toAmountFormat(),
                        fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                }

                ImageView(
                    imageId = R.drawable.approve,
                    size = MaterialTheme.dimens.DP_126_CompactMedium,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_33_CompactMedium))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(/*horizontal = MaterialTheme.dimens.DP_24_CompactMedium,*/),
                    contentAlignment = Alignment.Center
                ) {
                    CircularMenu(
                        onMenuOptionClick = { option ->
                            when (option) {
                                context.resources.getString((R.string.cust_recp)) -> {
                                    viewModel.printReceipt(context, true,sharedViewModel.objRootAppPaymentDetail)
                                }
                                context.resources.getString((R.string.merchant_recp)) -> {
                                    viewModel.printReceipt(
                                        context,
                                        objRootAppPaymentDetail = sharedViewModel.objRootAppPaymentDetail
                                    )
                                }
                                context.resources.getString((R.string.e_recp)) -> {
                                    navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
                                }
                            }
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
                            Log.d("StoredDateTime", "Stored Date and Time: ${sharedViewModel.objRootAppPaymentDetail.dateTime}")
                           viewModel.updateTxnData(sharedViewModel.objRootAppPaymentDetail)
                            //viewModel.onPurchaseApi(sharedViewModel.objRootAppPaymentDetail)
                            Log.d("StoredDateTime", "Stored Date and Time after db entry: ${sharedViewModel.objRootAppPaymentDetail.dateTime}")
                            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                        },
                        title = stringResource(id = R.string.done),
                    )
                }

                if (viewModel.isPrinting.value) {
                    CustomDialogBuilder.create()
                        .setTitle(stringResource(id = R.string.printing))
                        .setSubtitle(stringResource(id = R.string.plz_wait))
                        .setSmallText(stringResource(id = if(viewModel.isCustomer.value) R.string.cust_recp else R.string.merchant_recp))
                        .setShowCloseButton(true) // Can set to false if you don't want the close button
                        .setCancelable(true)
                        .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                        .setProgressColor(MaterialTheme.colorScheme.primary) // Orange color
                        .buildDialog(onClose = { viewModel.isPrinting.value = false })
                }

            }
        }
    }
}








