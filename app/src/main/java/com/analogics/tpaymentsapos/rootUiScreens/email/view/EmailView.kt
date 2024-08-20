package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun EmailView(navHostController: NavHostController, email: String) {

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    CommonLayout(
        title = when {
            isRefund -> stringResource(R.string.refund)
            isVoid -> stringResource(R.string.void_trans)
            isPreauth -> stringResource(R.string.pre_auth)
            else -> stringResource(R.string.purchase)
        },
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium)) // Blank space added here

        Text(
            text = stringResource(id = R.string.sucess),
            fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

        Text(
            text = stringResource(id = R.string.sent_email),
            fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

        Text(
            text = stringResource(id = R.string.ereceipt_sent),
            fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Text(
            text = "on $email",
            fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )


        Box(
            modifier = Modifier.padding(top = MaterialTheme.dimens.DP_30_CompactMedium)
        ) {
            OkButton(
                onClick = {
                    navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                },
                title = stringResource(id = R.string.ok)
            )
        }
    }
}
