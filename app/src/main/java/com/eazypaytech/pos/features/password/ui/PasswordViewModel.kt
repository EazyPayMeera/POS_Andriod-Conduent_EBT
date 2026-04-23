package com.eazypaytech.pos.features.password.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.utils.generateMasterPassword
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

    /**
     * Updates password input state.
     *
     * @param newValue Entered password string
     * @return Updated password value
     */
    fun updatePassword(newValue: String):String {
        _password.value = newValue
        return _password.value
    }

    /**
     * Verifies entered password against stored credentials.
     *
     * Behavior:
     * - Fetches saved password from database
     * - Validates against entered password or master password
     * - Shows error dialog if invalid
     * - Emits success event if valid
     *
     * @param sharedViewModel Shared ViewModel containing login details
     * @param context Application context for accessing resources
     * @param enteredPassword Password entered by user
     */
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

    /**
     * Handles cancel action during password verification.
     *
     * Behavior:
     * - Clears password input
     * - Emits failure result event
     */
    fun onCancel()
    {
        viewModelScope.launch {
            _password.value = ""
            event.emit(PasswordValidation.Result(false))
        }
    }

}
