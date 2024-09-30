

package com.analogics.tpaymentsapos.rootUiScreens.txnList.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Print
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel.TxnViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dimens
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListScreen(navHostController: NavHostController,viewModel: TxnViewModel = hiltViewModel()) {
    val transactions = viewModel.transactionList.collectAsState().value
    viewModel.fetchTransactions()
    var sharedViewModel= localSharedViewModel.current
    Column {
        CommonTopAppBar(
            title = stringResource(R.string.transactions),
            onBackButtonClick = { navHostController.popBackStack() }
        )
        GenericCard(
            modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(
                modifier = Modifier
            ) {
                HeaderSection(viewModel)
                SummarySection(viewModel)
            }
        }
        GenericCard(
            modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                    // Ensures the image and text are aligned vertically together
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = stringResource(id = R.string.recentActivity),
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                        )
                    }
                    Text(
                        text = "see",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(top=androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.filter_image),
                        contentDescription = "",
                        modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_23_CompactMedium).padding(end = 5.dp) // Adjust size as needed
                    )
                }
                LazyColumn {
                    items(transactions.size) { index ->
                        TransactionItem(transaction = transactions[index])
                    }
                }
            }
        }
    }
}

@Composable
fun SummarySection(viewModel: TxnViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Aligns items vertically at the center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // Ensures the image and text are aligned vertically together
            ) {
                Image(
                    painter = painterResource(id = R.drawable.purchase_txn),
                    contentDescription = "",
                    modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed
                )
                Spacer(modifier = Modifier.width(8.dp)) // Optional spacing between image and text
                Text("Purchase", style = MaterialTheme.typography.body2, color = Color.Gray)
            }

            Text(
                formatAmount(viewModel.totalPurchaseTransactions(TxnType.PURCHASE)),
                style = MaterialTheme.typography.body2
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Aligns items vertically at the center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // Ensures the image and text are aligned vertically together
            ) {
                Image(
                    painter = painterResource(id = R.drawable.refund_txn),
                    contentDescription = "",
                    modifier = Modifier.size(androidx.compose.material3.MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed
                )
                Spacer(modifier = Modifier.width(8.dp)) // Optional spacing between image and text
                Text("Refund", style = MaterialTheme.typography.body2, color = Color.Gray)
            }

            Text(
                formatAmount(viewModel.totalPurchaseTransactions(TxnType.REFUND)),
                style = MaterialTheme.typography.body2
            )
        }

    }
}

@Composable
fun TransactionItem(transaction: ObjRootAppPaymentDetails) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Log.d("TransactionDateTime", "DateTime: ${transaction.invoiceNo}")
                transaction.dateTime.toString().let {
                    Text(it, style = MaterialTheme.typography.caption, color = Color.Gray)
                }
                Text(transaction.txnType.toString(), style = MaterialTheme.typography.body2)
                Log.d("TransactionDateTime", "TxnType: ${transaction.txnType}")
            }

            val amountColor = if (transaction.txnType == TxnType.REFUND) {
                Color.Red
            } else {
                Color(0xFF4CAF50) // Green for other transaction types
            }

            transaction.ttlAmount?.let { formatAmount(it) }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2,
                    color = amountColor,
                    fontSize = 20.sp
                )
            }

            IconButton(onClick = { /* Handle item click */ }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "")
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium,
            color = Color.Gray
        )
    }
}






@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection(viewModel: TxnViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.caption,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp)
                )
                IconButton(onClick = { /* Handle print action */ }) {
                    Icon(Icons.Default.Print, contentDescription = "")
                }
            }

            Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.net_total),
                    style = MaterialTheme.typography.h5,
                    color = Color.Gray
                )
                Text(
                    text = formatAmount(
                        viewModel.totalPurchaseTransactions(TxnType.PURCHASE) - viewModel.totalPurchaseTransactions(
                            TxnType.REFUND
                        )
                    ),
                    style = MaterialTheme.typography.h4,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
