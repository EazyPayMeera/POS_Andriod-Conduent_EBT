package com.analogics.tpaymentsapos.rootUiScreens.usermanagement.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.UserManagementEntity
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.google.android.gms.maps.model.CustomCap
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

            // Iterate through the fetched user list and add user info to startDates
            userList.forEach {
                uiList.add(it.userId.toString())  // Adjust based on actual user object properties (e.g., user.id, user.name)
            }

            // Update the user list in the state flow
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
                        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.getString(R.string.label_remove_user),
                            subtitle = navHostController.context.getString(R.string.msg_logout_first)
                        )
                    }
                    else if(it==true)
                    {
                        dbRepository.getAdminCount().let {
                            if(it>1)
                            {
                                dbRepository.removeUser(user).let {
                                    CustomDialogBuilder.composeAlertDialog(title = navHostController.context.getString(R.string.label_remove_user),
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
                            CustomDialogBuilder.composeAlertDialog(title = navHostController.context.getString(R.string.label_remove_user),
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

    fun onLoad(sharedViewModel: SharedViewModel) {
        refreshUserList()
        checkIfAdmin(sharedViewModel)
    }

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

    fun checkIfAdmin(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig?.loginId?.let {
                dbRepository.isAdmin(it).let {
                    _isAdmin.value = it
                }
            }
        }
    }

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
