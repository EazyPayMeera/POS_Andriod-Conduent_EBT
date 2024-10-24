package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootModel.UiLanguage
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.language.viewmodel.LanguageViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dashboardOrangeColor
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun LanguageView(navHostController: NavHostController, viewModel: LanguageViewModel = hiltViewModel()) {
    val context = LocalContext.current // Get the context here
    val sharedViewModel = localSharedViewModel.current

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.set_lang),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium),
            elevation = MaterialTheme.dimens.DP_19_CompactMedium
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(
                    top = MaterialTheme.dimens.DP_30_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_11_CompactMedium
                )
            ) {
                TextView(
                    text = stringResource(id = R.string.select_lang),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )

                ImageView(
                    imageId = R.drawable.language,
                    size = MaterialTheme.dimens.DP_33_CompactMedium,
                    shape = RectangleShape,
                    alignment = Alignment.Center,
                    contentDescription = ""
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                Divider(
                    color = MaterialTheme.colorScheme.tertiary,
                    thickness = MaterialTheme.dimens.DP_1_CompactMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                // Row for "English" with RadioButton
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.orange_bullet),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_25_CompactMedium)
                            .padding(end = MaterialTheme.dimens.DP_10_CompactMedium)
                    )

                    TextView(
                        text = stringResource(id = R.string.english),
                        fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_15_CompactMedium),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    RadioButton(
                        selected = viewModel.uiLanguage.value == UiLanguage.ENGLISH,
                        onClick = { viewModel.onLanguageChange(UiLanguage.ENGLISH, sharedViewModel, context) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                Divider(
                    color = MaterialTheme.colorScheme.tertiary,
                    thickness = MaterialTheme.dimens.DP_1_CompactMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                // Row for "Hindi" with RadioButton
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.orange_bullet),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_25_CompactMedium)
                            .padding(end = MaterialTheme.dimens.DP_10_CompactMedium)
                    )

                    TextView(
                        text = stringResource(id = R.string.hindi),
                        fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(MaterialTheme.dimens.DP_15_CompactMedium),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    RadioButton(
                        selected = viewModel.uiLanguage.value == UiLanguage.HINDI,
                        onClick = { viewModel.onLanguageChange(UiLanguage.HINDI, sharedViewModel, context) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = dashboardOrangeColor,
                            unselectedColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(sharedViewModel)
    }
}
