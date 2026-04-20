package com.eazypaytech.pos.features.transactiondetails.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.CircularMenu
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.utils.getCardBrandStringId
import com.eazypaytech.pos.core.utils.getCardEntryStringId
import com.eazypaytech.pos.core.utils.getIsoResponseCodeString
import com.eazypaytech.pos.core.utils.getTxnStatusIconId
import com.eazypaytech.pos.core.utils.getTxnStatusStringId
import com.eazypaytech.pos.core.utils.getTxnTypeStringId
import com.eazypaytech.pos.core.utils.toAmountFormat
import com.eazypaytech.pos.core.utils.toCardBrand
import com.eazypaytech.pos.core.themes.dimens


@Composable
fun TransactionDetailsScreen(navHostController: NavHostController) {

    val viewModel: TransactionDetailsViewModel = hiltViewModel()
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Column {

        CommonTopAppBar(
            title = stringResource(id = R.string.ebt_void_last),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            GenericCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    TextView(
                        text = stringResource(id = getTxnTypeStringId(sharedViewModel.objRootAppPaymentDetail.txnType)),
                        fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = MaterialTheme.dimens.DP_21_CompactMedium)
                    )

                    TextView(
                        text = sharedViewModel.objRootAppPaymentDetail.ttlAmount.toAmountFormat(),
                        fontSize = MaterialTheme.dimens.SP_35_CompactMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = MaterialTheme.dimens.DP_15_CompactMedium)
                    )

                    TextView(
                        text = stringResource(id = getTxnStatusStringId(sharedViewModel.objRootAppPaymentDetail.txnStatus)),
                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = MaterialTheme.dimens.DP_15_CompactMedium)
                    )

                    if(sharedViewModel.objRootAppPaymentDetail.txnStatus == TxnStatus.DECLINED) {
                        sharedViewModel.objRootAppPaymentDetail.hostRespCode?.takeIf { it.length >= 2 }
                            ?.let {
                                TextView(
                                    text = "[ " + getIsoResponseCodeString(context,it) + " ]",
                                    fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(top = MaterialTheme.dimens.DP_15_CompactMedium)
                                )
                            }
                    }

                    ImageView(
                        imageId = getTxnStatusIconId(sharedViewModel.objRootAppPaymentDetail),
                        shape = RectangleShape,
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_50_CompactMedium)
                            .padding(top = MaterialTheme.dimens.DP_15_CompactMedium),
                        contentDescription = ""
                    )

                    TextView(
                        text = stringResource(id = R.string.date_time)+ " " + (sharedViewModel.objRootAppPaymentDetail.dateTime?.toString()?:"-"),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(
                                top = MaterialTheme.dimens.DP_33_CompactMedium,
                                start = MaterialTheme.dimens.DP_34_CompactMedium
                            )
                    )

                    TextView(
                        text = stringResource(id = R.string.card)+ " " + (stringResource(id = getCardBrandStringId(sharedViewModel.objRootAppPaymentDetail.cardBrand?.toCardBrand())).plus(" ")) + (sharedViewModel.objRootAppPaymentDetail.cardMaskedPan?:"-"),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(
                                top = MaterialTheme.dimens.DP_11_CompactMedium,
                                start = MaterialTheme.dimens.DP_34_CompactMedium
                            )
                    )

                    TextView(
                        text = stringResource(id = R.string.pos_entry)+ " " + (stringResource(id =getCardEntryStringId(sharedViewModel.objRootAppPaymentDetail.cardEntryMode))),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(
                                top = MaterialTheme.dimens.DP_11_CompactMedium,
                                start = MaterialTheme.dimens.DP_34_CompactMedium
                            )
                    )

                    TextView(
                        text = stringResource(id = R.string.inc_no) + " " + (sharedViewModel.objRootAppPaymentDetail.invoiceNo?:"-"),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(
                                top = MaterialTheme.dimens.DP_11_CompactMedium,
                                start = MaterialTheme.dimens.DP_34_CompactMedium
                            )
                    )
                    if (sharedViewModel.objRootAppPaymentDetail.hostTxnRef != null) {
                        TextView(
                            text = stringResource(id = R.string.ref_id) + " " + (sharedViewModel.objRootAppPaymentDetail.hostTxnRef
                                ?: "-"),
                            fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(
                                    top = MaterialTheme.dimens.DP_11_CompactMedium,
                                    start = MaterialTheme.dimens.DP_34_CompactMedium
                                )
                        )
                    }

                    if (sharedViewModel.objRootAppPaymentDetail.hostAuthCode != null) {
                        TextView(
                            text = stringResource(id = R.string.auth_code) + " " + sharedViewModel.objRootAppPaymentDetail.hostAuthCode,
                            fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(
                                    top = MaterialTheme.dimens.DP_11_CompactMedium,
                                    start = MaterialTheme.dimens.DP_34_CompactMedium
                                )
                        )
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MaterialTheme.dimens.DP_31_CompactMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularMenu(
                            menuOptions = listOf(context.resources.getString((R.string.cust_recp)), context.resources.getString((R.string.merchant_recp))),
                            onMenuOptionClick = { option ->
                                when (option) {
                                    context.resources.getString((R.string.cust_recp)) -> {
                                        viewModel.printReceipt(context, sharedViewModel.objRootAppPaymentDetail, true)
                                    }
                                    context.resources.getString((R.string.merchant_recp)) -> {
                                        viewModel.printReceipt(context, sharedViewModel.objRootAppPaymentDetail, false)
                                    }
                                }
                            },
                            onPrintClick = {}
                        )
                    }

                    CustomDialogBuilder.ShowComposed()
                }
            }
        }
    }

}




