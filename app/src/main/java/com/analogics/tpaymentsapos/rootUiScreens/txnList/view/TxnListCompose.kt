

package com.analogics.tpaymentsapos.rootUiScreens.txnList.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.txnList.model.TxnDataList
import com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel.TxnViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dimens
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListScreen(navHostController: NavHostController,viewModel: TxnViewModel = hiltViewModel()) {
    val transactions = viewModel.transactionList.collectAsState().value
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
                HeaderSection()
                SummarySection()
            }
        }
        GenericCard(
            modifier = Modifier.padding(androidx.compose.material3.MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(
                modifier = Modifier
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(8.dp)
                )

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
fun SummarySection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Purchase", style = MaterialTheme.typography.body2, color = Color.Gray)
            Text(formatAmount(450.00), style = MaterialTheme.typography.body2)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Refund", style = MaterialTheme.typography.body2, color = Color.Gray)
            Text(formatAmount(50.00), style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
fun TransactionItem(transaction: TxnDataList) {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.date, style = MaterialTheme.typography.caption, color = Color.Gray)
                Text(transaction.type, style = MaterialTheme.typography.body2)
            }
            Text(
                text = formatAmount(transaction.amount),
                style = MaterialTheme.typography.body2,
                color = if (transaction.isPositive) Color(0xFF4CAF50) else Color.Red
            )
            IconButton(onClick = { /* Handle item click */ }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Arrow")
            }
        }
        Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.Gray)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewTransactionListScreen() {
    val navHostController:NavHostController=NavHostController(context = LocalContext.current)
    val mockTransactions = listOf(
        TxnDataList(1, "Today @ 14:15:30", "Purchase", 450.00, true),
        TxnDataList(2, "Today @ 14:15:30", "Refund", 50.00, false),
        TxnDataList(3, "26-2-2020 @ 14:15:30", "Purchase", 50.00, true)
    )
    TransactionListScreen(navHostController,viewModel = FakeTransactionViewModel(mockTransactions))
}

class FakeTransactionViewModel(mockTransactions: List<TxnDataList>) : TxnViewModel() {
    init {
        _transactionList.value = mockTransactions
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = MaterialTheme.typography.caption,
                color = Color(0xFFFFA000) // Orange color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Net Total",
                style = MaterialTheme.typography.h5,
                color = Color.Gray
            )
            Text(
                text = formatAmount(400.00),
                style = MaterialTheme.typography.h4,
                color = Color(0xFFFFA000) // Orange color
            )
        }
        IconButton(onClick = { /* Handle print action */ }) {
            Icon(Icons.Default.Print, contentDescription = "Print")
        }
    }
}
