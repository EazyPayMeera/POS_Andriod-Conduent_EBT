package com.analogics.tpaymentsapos.rootUiScreens.usermanagement.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
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

    // Function to fetch user details from the database
    fun fetchUserDetails() {
        viewModelScope.launch {
            try {
                val userList = dbRepository.getUserList()
                val startDates = mutableListOf<String>()

                // Iterate through the fetched user list and add user info to startDates
                userList.forEach {
                    startDates.add(it.toString())  // Adjust based on actual user object properties (e.g., user.id, user.name)
                }

                // Update the user list in the state flow
                _userList.value = startDates
            } catch (e: Exception) {
                Log.e("FetchUserDetails", "Error fetching user details: ${e.message}")
            }
        }
    }

    // Function to remove a user from the database
    @RequiresApi(Build.VERSION_CODES.O)
    fun removeUser(user: String) {
        viewModelScope.launch {
            try {
                dbRepository.removeUser(user)
            } catch (e: Exception) {
                Log.e("RemoveUser", "Error removing user: ${e.message}")
            }
        }
    }
}
