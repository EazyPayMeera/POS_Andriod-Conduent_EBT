package com.eazypaytech.pos.features.voidsel.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.analogics.builder_core.data.model.Symbol
import com.analogics.builder_core.utils.formatAmount
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.core.themes.dimens
import com.eazypaytech.pos.core.ui.components.inputfields.BackgroundScreen
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.getVoidTransTypeString
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VoidSelectionScreen(navHostController: NavHostController) {

    val context = LocalContext.current
    val viewModel: VoidSelectionViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    val availableTransactions by viewModel.availableTransactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() },
        )

        BackgroundScreen {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_24_CompactMedium))

                TextView(
                    text = stringResource(R.string.select_transaction_to_void),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                when {
                    availableTransactions.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = MaterialTheme.dimens.DP_180_CompactMedium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_transactions_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.DP_12_CompactMedium)
                        ) {
                            items(
                                items = availableTransactions,
                                key = { it.id }
                            ) { transaction ->
                                TransactionVoidItem(
                                    transaction = transaction,
                                    onClick = {
                                        viewModel.onTransactionSelectedForVoid(
                                            selected         = transaction,
                                            navHostController = navHostController,
                                            sharedViewModel  = sharedViewModel,
                                            context          = context
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                CustomDialogBuilder.ShowComposed()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(context, sharedViewModel)
    }
}


// ─── Transaction List Item ────────────────────────────────────────────────────

@Composable
fun TransactionVoidItem(
    transaction: TransactionItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = MaterialTheme.dimens.DP_2_CompactMedium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium, vertical = MaterialTheme.dimens.DP_13_CompactMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getVoidTransTypeString(transaction.displayName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (transaction.amount != null) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_2_CompactMedium))
                    Text(
                        text = formatAmount(
                            input = transaction.amount ?: "",
                            symbol = Symbol(type = Symbol.Type.CURRENCY, currency = Symbol.Currency.USD, position = Symbol.Position.START)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (transaction.dateTime != null) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_2_CompactMedium))
                    Text(
                        text = transaction.dateTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MaterialTheme.dimens.DP_24_CompactMedium)
            )
        }
    }
}


// ─── Data model ──────────────────────────────────────────────────────────────

data class TransactionItem(
    val id: Long,
    val txnType: TxnType,
    val displayName: String,
    val amount: String? = null,
    val dateTime: String? = null
)