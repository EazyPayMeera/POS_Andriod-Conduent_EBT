package com.analogics.tpaymentsapos.rootUiScreens.usermanagement.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.ListDialogueBuilder
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.usermanagement.viewmodel.UserManagementViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSwitch
import com.analogics.tpaymentsapos.ui.theme.dimens

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserManagementView(navHostController: NavHostController, viewModel: UserManagementViewModel = hiltViewModel()) {
    val sharedViewModel= localSharedViewModel.current
    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.label_user_management),
            onBackButtonClick = { navHostController.popBackStack() }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.DP_13_CompactMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CustomDrawerContent(
                onCloseDrawer = { },
                navHostController = navHostController,
                viewModel = viewModel,  // Pass viewModel here,
                sharedViewModel = sharedViewModel
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomDrawerContent(
    onCloseDrawer: () -> Unit,
    navHostController: NavHostController,
    viewModel: UserManagementViewModel,
    sharedViewModel: SharedViewModel
) {
    var isViewUser by remember { mutableStateOf(false) }
    var isRemoveUser by remember { mutableStateOf(false) }
    val userList = viewModel.usersList.collectAsState().value
    val isAdmin = viewModel.isAdmin.collectAsState().value

    val drawersItems = listOf(
        DrawerItem(
            imageRes = Icons.Default.PermIdentity,
            text = stringResource(id = R.string.label_add_user),
            isChecked = false,
            isEnabled = isAdmin,
            onCheckedChange = { if(!isAdmin) viewModel.onShowAdminOnly(navHostController.context) else navHostController.navigate(AppNavigationItems.AddClerkScreen.route) }
        ),
        DrawerItem(
            imageRes = Icons.Default.Person,
            text = stringResource(id = R.string.label_view_user),
            isChecked = false,
            isEnabled = true,
            onCheckedChange = { isViewUser = true }
        ),
        DrawerItem(
            imageRes = Icons.Default.Person,
            text = stringResource(id = R.string.label_remove_user),
            isChecked = false,
            isEnabled = isAdmin,
            onCheckedChange = { if(!isAdmin) viewModel.onShowAdminOnly(navHostController.context) else isRemoveUser = true }
        ),
        DrawerItem(
            imageRes = Icons.Default.Settings,
            text = stringResource(id = R.string.change_password),
            isChecked = false,
            isEnabled = true,
            onCheckedChange = { navHostController.navigate(AppNavigationItems.ChangePasswordScreen.route) }
        ),
    )

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.dimens.DP_4_CompactMedium),
            elevation = CardDefaults.elevatedCardElevation(MaterialTheme.dimens.DP_11_CompactMedium)
        ) {
            Column {
                drawersItems.forEachIndexed { index, item ->
                    DrawersSurface(
                        modifier = Modifier.fillMaxWidth(),
                        item = item
                    )

                    index.takeIf { it < drawersItems.size - 1 }?.let {
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = MaterialTheme.dimens.DP_1_CompactMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    if (isViewUser) {
        viewModel.prepareUserList()
        ListDialogueBuilder.create()
            .setTitle(stringResource(id = R.string.label_view_user))
            .UserListDialog(
                title = stringResource(id = R.string.label_view_user),
                msgOnEmpty = stringResource(id = R.string.msg_user_list_empty),
                onClose = { isViewUser = false },
                users = userList,
                onItemSelected = { }
            )
    }

    if (isRemoveUser) {
        viewModel.prepareUserList()
        ListDialogueBuilder.create()
            .setTitle(stringResource(id = R.string.sel_user))
            .UserListDialog(
                title = stringResource(id = R.string.label_remove_user),
                msgOnEmpty = stringResource(id = R.string.msg_user_list_empty),
                onClose = { isRemoveUser = false },
                users = userList,
                onItemSelected = { selectedId ->
                    viewModel.removeUser(navHostController,selectedId, sharedViewModel)
                }
            )
    }

    CustomDialogBuilder.ShowComposed()

    LaunchedEffect(Unit) {
        viewModel.onLoad(sharedViewModel)
    }
}

data class DrawerItem(
    val imageRes: ImageVector,
    val text: String,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    var isEnabled : Boolean
)

@Composable
fun DrawersSurface(
    modifier: Modifier = Modifier,
    item: DrawerItem
) {
    Surface(
        modifier = Modifier.height(MaterialTheme.dimens.DP_60_CompactMedium)
            .then(if (!item.isEnabled) Modifier.alpha(0.5f) else Modifier) // Dim surface when isAdmin is false
            .clickable(enabled = item.isEnabled) {}, // Make it non-clickable if isAdmin is false ,
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        DrawersContent(
            item = item
        )
    }
}

@Composable
fun DrawersContent(
    item: DrawerItem
) {
    // Handle the click event to toggle the switch
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium)
            .clickable {
                item.onCheckedChange(!item.isChecked) // Toggle the switch state
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_24_CompactMedium), // Added padding for better touch area
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.imageRes,  // Use Icon for ImageVector
                    contentDescription = item.text,
                    modifier = Modifier.size(MaterialTheme.dimens.DP_28_CompactMedium)
                )

                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
                )
            }

            CustomSwitch(
                checked = item.isChecked,
                onCheckedChange = { newCheckedState ->
                    item.onCheckedChange(newCheckedState)
                },
                checkedImage = R.drawable.arrow, // Your checked drawable
                uncheckedImage = R.drawable.arrow, // Your unchecked drawable
                imageSize = MaterialTheme.dimens.DP_23_CompactMedium
            )
        }
    }
}
