package com.eazypaytech.pos.features.usermanagement.ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.analogics.securityframework.database.entity.UserManagementEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {

    // StateFlow to hold the user list
    private val _userList = MutableStateFlow<List<String>>(emptyList())
    val usersList: StateFlow<List<String>> = _userList
    lateinit var userList: List<UserManagementEntity>
    private val _isAdmin = MutableStateFlow(false) // Change to StateFlow
    val isAdmin: StateFlow<Boolean> get() = _isAdmin

    // Function to fetch user details from the database
    fun prepareUserList() {
        try {
            val uiList = mutableListOf<String>()

            userList.forEach {
                uiList.add(it.userId.toString())
            }

            _userList.value = uiList
        } catch (e: Exception) {
            Log.e("FetchUserDetails", "Error fetching user details: ${e.message}")
        }

    }

    // Function to remove a user from the database
    @RequiresApi(Build.VERSION_CODES.O)
    fun removeUser(navHostController: NavHostController, user: String, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            try {
                dbRepository.isAdmin(user).let {
                    if(user == sharedViewModel.objPosConfig?.loginId)
                    {
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHostController.context.getString(
                            R.string.label_remove_user),
                            subtitle = navHostController.context.getString(R.string.msg_logout_first)
                        )
                    }
                    else if(it==true)
                    {
                        dbRepository.getAdminCount().let {
                            if(it>1)
                            {
                                dbRepository.removeUser(user).let {
                                    CustomDialogBuilder.composeAlertDialog(title = navHostController.context.getString(
                                        R.string.label_remove_user),
                                        subtitle = navHostController.context.getString(R.string.msg_user_removed)
                                    )
                                    refreshUserList()
                                }
                            }
                            else
                            {
                                CustomDialogBuilder.composeAlertDialog(
                                    title = navHostController.context.getString(R.string.label_remove_user),
                                    subtitle = navHostController.context.getString(R.string.min_one_admin_required)
                                )
                            }
                        }
                    }
                    else
                    {
                        dbRepository.removeUser(user).let {
                            CustomDialogBuilder.composeAlertDialog(title = navHostController.context.getString(
                                R.string.label_remove_user),
                                subtitle = navHostController.context.getString(R.string.msg_user_removed)
                            )
                            refreshUserList()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RemoveUser", "Error removing user: ${e.message}")
            }
        }
    }

    /**
     * Initializes screen data on load.
     *
     * Behavior:
     * - Fetches and refreshes user list
     * - Checks whether current user has admin privileges
     *
     * @param sharedViewModel Shared ViewModel containing login details
     */
    fun onLoad(sharedViewModel: SharedViewModel) {
        refreshUserList()
        checkIfAdmin(sharedViewModel)
    }

    /**
     * Fetches all user details from database.
     *
     * Behavior:
     * - Retrieves user list asynchronously
     * - Updates UI state with latest data
     * - Handles exceptions and logs errors
     */
    fun refreshUserList() {
        viewModelScope.launch {
            try {
                dbRepository.getAllUserDetails().let {
                    userList = it ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("FetchUserDetails", "Error fetching user details: ${e.message}")
            }
        }
    }

    /**
     * Checks if the currently logged-in user is an admin.
     *
     * Behavior:
     * - Fetches login ID from POS config
     * - Queries database for admin status
     * - Updates admin state flag
     *
     * @param sharedViewModel Shared ViewModel containing POS configuration
     */
    fun checkIfAdmin(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig?.loginId?.let {
                dbRepository.isAdmin(it).let {
                    _isAdmin.value = it
                }
            }
        }
    }

    /**
     * Displays a dialog indicating restricted access.
     *
     * Behavior:
     * - Shows alert dialog for non-admin users
     * - Used when accessing admin-only features
     *
     * @param context Application context for resource access
     */
    fun onShowAdminOnly(context: Context)
    {
        CustomDialogBuilder.composeAlertDialog(
            title = context.resources.getString(
                R.string.restricted
            ),
            subtitle = context.resources.getString(R.string.for_admin)
        )
    }
}