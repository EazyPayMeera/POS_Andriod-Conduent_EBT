package com.analogics.tpaymentsapos.rootUiScreens.transactiondetails

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
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CircularMenu
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCardBrandStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCardEntryStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getIsoResponseCodeString
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getIsoResponseCodeStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getTxnStatusIconId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getTxnStatusStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getTxnTypeStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toAmountFormat
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toCardBrand
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun TransactionDetailsView(navHostController: NavHostController) {

    val viewModel: TransactionDetailsViewModel = hiltViewModel()
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Column {

        CommonTopAppBar(
            title = stringResource(id = R.string.transactions),
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
                                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
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

                    TextView(
                        text = stringResource(id = R.string.ref_id) + " " + (sharedViewModel.objRootAppPaymentDetail.hostTxnRef?:"-"),
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
                        text = stringResource(id = R.string.auth_code) + " " + (sharedViewModel.objRootAppPaymentDetail.hostAuthCode?:"-"),
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
                                        viewModel.printReceipt(R.drawable.master_mono,sharedViewModel,context, true,sharedViewModel.objRootAppPaymentDetail)
                                    }
                                    context.resources.getString((R.string.merchant_recp)) -> {
                                        viewModel.printReceipt(R.drawable.master_mono,sharedViewModel,context, false,sharedViewModel.objRootAppPaymentDetail)
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




