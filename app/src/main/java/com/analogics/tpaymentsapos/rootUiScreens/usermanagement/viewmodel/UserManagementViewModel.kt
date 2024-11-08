package com.analogics.tpaymentsapos.rootUiScreens.usermanagement.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {

    private val _userList = MutableStateFlow<List<String>>(emptyList())

    val usersList: StateFlow<List<String>> = _userList

    fun fetchUserDetails() {
        viewModelScope.launch {
            try {
                val startDates = mutableListOf<String>()

                val userList = dbRepository.getUserList()
                userList.let {
                    startDates.add(it.toString()) // Add the non-null start date to the list
                }

                _userList.value = startDates
                // Print batch ID and start date
            } catch (e: Exception) {
                Log.e("FetchStartDates", "Error fetching start dates: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun removeUser(user:String) {
        viewModelScope.launch {
            dbRepository.removeUser(user)
        }
    }
}