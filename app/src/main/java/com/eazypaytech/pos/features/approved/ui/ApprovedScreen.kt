package com.eazypaytech.pos.features.approved.ui


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.hardwarecore.data.model.EmvSdkResult
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.BackgroundScreen
import com.eazypaytech.pos.core.ui.components.inputfields.CircularMenu
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.inputfields.OkButton
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.utils.getBalInquiryStringId
import com.eazypaytech.pos.core.utils.getTxnStatusIconId
import com.eazypaytech.pos.core.utils.getTxnStatusStringId
import com.eazypaytech.pos.core.utils.toAmountFormat
import com.eazypaytech.pos.core.themes.dimens
import com.eazypaytech.pos.core.ui.components.textview.AutoResizeText
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovedScreen(navHostController: NavHostController) {

    val context = LocalContext.current
    val viewModel: ApprovedViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current
    var txnRecord = remember { sharedViewModel.objRootAppPaymentDetail }
    val hasDbRecord = viewModel.hasDbRecord.collectAsState().value
    val infiniteTransition = rememberInfiniteTransition()
    val isBalanceInquiry = txnRecord.txnType == TxnType.BALANCE_ENQUIRY_SNAP || txnRecord.txnType == TxnType.BALANCE_ENQUIRY_CASH
    val snapBegin = txnRecord.snapEndBalance?.plus(txnRecord.ttlAmount!!)
    val cashBegin = txnRecord.cashEndBalance?.plus(txnRecord.ttlAmount!!)
    sharedViewModel.objRootAppPaymentDetail.snapBeginBal = snapBegin
    sharedViewModel.objRootAppPaymentDetail.cashBeginBal = cashBegin

    Column {
        CommonTopAppBar(
            onBackButtonClick = { },
            showBackIcon = false
        )

        BackgroundScreen(
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_24_CompactMedium))
                if(isBalanceInquiry && txnRecord.txnStatus == TxnStatus.APPROVED)
                {
                    TextView(
                        text = stringResource(id = getTxnStatusStringId(txnRecord.txnStatus)),
                        fontSize = MaterialTheme.dimens.SP_29_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))
                    Text(
                        text = "SNAP: ${txnRecord.snapEndBalance.toAmountFormat()}\n\n" +
                                "CASH: ${txnRecord.cashEndBalance.toAmountFormat()}",
                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = MaterialTheme.dimens.DP_55_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_100_CompactMedium))
                }
                else {
                    TextView(
                        text = stringResource(id = getTxnStatusStringId(txnRecord.txnStatus)),
                        fontSize = MaterialTheme.dimens.SP_29_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))
                    AutoResizeText(
                        text = if (isBalanceInquiry) { "" } else { txnRecord.ttlAmount.toAmountFormat() },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(MaterialTheme.dimens.DP_33_CompactMedium)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium))
                    ImageView(
                        imageId = getTxnStatusIconId(txnRecord),
                        size = MaterialTheme.dimens.DP_120_CompactMedium,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_40_CompactMedium)
                            .align(Alignment.CenterHorizontally),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_7_CompactMedium))
                    if (txnRecord.txnStatus != TxnStatus.APPROVED) {
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700),
                                repeatMode = RepeatMode.Reverse
                            ), label = ""
                        )

                        Text(
                            text = txnRecord.hostResMessage.toString() + if (!txnRecord.hostRespCode.isNullOrBlank()) {
                                " (${txnRecord.hostRespCode})"
                            } else {
                                ""
                            },
                            fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(MaterialTheme.dimens.DP_33_CompactMedium)
                                .alpha(alpha)
                        )
                    }

                }
                if (hasDbRecord) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularMenu(
                            menuOptions = listOf(
                                context.resources.getString((R.string.cust_recp)),
                                context.resources.getString((R.string.merchant_recp))
                            ),
                            onMenuOptionClick = { option ->
                                when (option) {
                                    context.resources.getString((R.string.cust_recp)) -> {
                                        viewModel.printReceipt(
                                            context,
                                            sharedViewModel,
                                            true
                                        )
                                    }

                                    context.resources.getString((R.string.merchant_recp)) -> {
                                        viewModel.printReceipt(
                                            context,
                                            sharedViewModel,
                                            false
                                        )
                                    }
                                }
                            },
                            onPrintClick = {}
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_70_CompactMedium))
                }
                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_50_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            viewModel.onDone(navHostController)
                        },
                        title = stringResource(id = R.string.done),
                    )
                }

                CustomDialogBuilder.ShowComposed()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(context, sharedViewModel)
        while (true) {
            if (viewModel.isCardExists(context)) {
                CustomDialogBuilder.composeAlertDialog(
                    title = context.resources.getString(R.string.default_alert_title_error),
                    subtitle = context.resources.getString(R.string.emv_msg_id_remove_card)
                )
            } else {
                break
            }
            delay(500)
        }
    }
}








