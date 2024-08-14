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
import androidx.compose.ui.res.stringResource
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
import com.analogics.tpaymentsapos.ui.theme.dimens
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


    Column {
        CommonTopAppBar(
            title = when {
                isRefund -> stringResource(R.string.refund)
                isVoid -> stringResource(R.string.void_trans)
                isPreauth -> stringResource(R.string.pre_auth)
                else -> stringResource(R.string.purchase)
            },
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card,
            titleText = stringResource(id = R.string.enter_invoice),
            label = "",
            placeholder = stringResource(id = R.string.invoice_no),
            value = invoiceno,
            onValueChange = { invoiceno = it },
            onDoneAction = { navHostController.navigate(AppNavigationItems.AmountScreen.route) },
            isRefund = isRefund,
            isVoid = isVoid,
            isAuthcap = isAuthcap,
            keyboardType = KeyboardType.Text
        ) {

            if (isRefund || isVoid || isAuthcap) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))
                Text(
                    text = "-----------or-----------",
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                ScannerButton(
                    text = stringResource(id = R.string.scan_qr),
                    onClick = { showMenu = !showMenu },
                    backgroundColor = Color(0xFFEDEDED),
                    contentColor = Color.Black,
                    modifier = Modifier.padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
                )
            }
        }
    }
}




