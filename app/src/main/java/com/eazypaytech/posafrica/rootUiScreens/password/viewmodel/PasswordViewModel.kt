package com.eazypaytech.posafrica.rootUiScreens.password.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.generateMasterPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PasswordValidation {
    data class Result(var isValid:Boolean) : PasswordValidation()
}

@HiltViewModel
class PasswordViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password
    val event = MutableSharedFlow<PasswordValidation>()

    fun updatePassword(newValue: String):String {
        _password.value = newValue
        return _password.value
    }

    fun onVerifyPassword(sharedViewModel: SharedViewModel, context: Context, enteredPassword:String)
    {
        viewModelScope.launch {
            try {
                val password = dbRepository.fetchPassword(sharedViewModel.objPosConfig?.loginId.toString())
                if(password != enteredPassword && enteredPassword != generateMasterPassword(sharedViewModel.objPosConfig?.loginId, sharedViewModel))
                {
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.resources.getString(
                            R.string.default_alert_title_error
                        ),
                        subtitle = context.resources.getString(R.string.default_alert_subtitle_wrong_password)
                    )
                    _password.value = ""
                }
                else
                {
                    _password.value = ""
                    event.emit(PasswordValidation.Result(true))
                }
            } catch (e: Exception) {
                Log.e("FetchStartDates", "Error fetching start dates: ${e.message}")
            }
        }
    }

    fun onCancel()
    {
        viewModelScope.launch {
            _password.value = ""
            event.emit(PasswordValidation.Result(false))
        }
    }

}
