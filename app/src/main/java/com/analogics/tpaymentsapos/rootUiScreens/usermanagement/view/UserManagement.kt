package com.analogics.tpaymentsapos.rootUiScreens.usermanagement.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.BatchDialogueBuilder
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.usermanagement.viewmodel.UserManagementViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.Roboto
import com.analogics.tpaymentsapos.ui.theme.dimens

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserManagementView(navHostController: NavHostController, viewModel: UserManagementViewModel = hiltViewModel()) {
    var isRemoveUser by remember { mutableStateOf(false) }
    val userList = viewModel.usersList.collectAsState().value  // Collecting users from the view model
    var isDialogVisible by remember { mutableStateOf(false) }

    Column {
        CommonTopAppBar(
            title = "User Management",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_19_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_36_CompactMedium)
            ) {
                TextView(
                    text = "User Info Management",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Roboto,
                    modifier = Modifier.padding(start = MaterialTheme.dimens.DP_11_CompactMedium),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium
                )

                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_31_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.AddClerkScreen.route)
                        },
                        title = "ADD USER",
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_31_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            isRemoveUser = true
                        },
                        title = "REMOVE USER",
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_31_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.ChangePasswordScreen.route)
                        },
                        title = stringResource(id = R.string.change_password)
                    )
                }
            }
        }
    }

    if (isRemoveUser) {
        viewModel.fetchUserDetails()
        BatchDialogueBuilder.create()
            .setTitle(stringResource(id = R.string.sel_user))
            .UserListDialog(
                onClose = { isRemoveUser = false },
                users = userList,
                onItemSelected = { selectedId ->
                    // Check if only 1 user is available for removal
                    if (userList.size > 1) {
                        viewModel.removeUser(selectedId)
                    } else {
                        isDialogVisible = true  // Show the dialog if there's only one user
                    }
                }
            )
    }

    if (isDialogVisible) {
        CustomDialogBuilder.create()
            .setTitle("Operation Not Allowed")
            .setSubtitle("Minimum 1 User")
            .setSmallText("is Mandatory")
            .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
            .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
            .setOnCancelAction {
                navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
            }
            .setOnConfirmAction {
                navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
            }
            .setShowButtons(false)
            .setAutoOff(false)
            .setNavAction {
                navHostController.popBackStack()
            }
            .buildDialog(onClose = { isDialogVisible = false })
    }
}
