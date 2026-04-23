package com.eazypaytech.pos.features.keyManagement.ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.eazypaytech.pos.R
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class KeyEntryViewModel @Inject constructor() : ViewModel() {

    var key by mutableStateOf("")
        private set

    /**
     * Initializes the screen by loading the Master KEK from local storage.
     *
     * @param context Application context used to access file storage
     * @param sharedViewModel Shared ViewModel containing app state
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(context: Context,sharedViewModel: SharedViewModel)
    {
        key = readMasterKEK(context,sharedViewModel).toString()
    }

    /**
     * Updates the key value when user input changes.
     *
     * @param newValue Newly entered key string
     */
    fun onCardNoChange(newValue: String) {
        key = newValue
    }

    /**
     * Validates the entered key and triggers save operation if valid.
     *
     * Validation Rules:
     * - Key must not be empty
     * - Key must be exactly 32 characters long
     *
     * @param context Application context
     * @param navHostController Navigation controller for screen transitions
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(context: Context,navHostController: NavHostController) {
        if(key.isEmpty()) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.plz_enter_key)
            )
        }
        else if(key.length != 32)
        {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = "Key Should be of 32 digit"
            )
        }
        else {
            saveMasterKEK(navHostController,context)

        }
    }

    /**
     * Handles cancel action and navigates user back to dashboard.
     *
     * @param navHostController Navigation controller
     */
    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    /**
     * Reads the Master KEK value from Config.json file.
     *
     * Behavior:
     * - Returns null if file does not exist
     * - Returns null if key is missing or empty
     * - Logs errors if parsing fails
     *
     * @param context Application context
     * @param sharedViewModel Shared ViewModel (reserved for future use)
     * @return Master KEK string or null
     */
    fun readMasterKEK(context: Context, sharedViewModel: SharedViewModel): String? {
        val configFile = File(context.getExternalFilesDir(null), "Config.json")

        if (!configFile.exists()) {
            Log.d("ConfigRead", "Config file does not exist!")
            return null
        }

        return try {
            val json = configFile.readText()
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)

            val masterKey = jsonObject.get("master_kek")?.asString

            if (masterKey.isNullOrEmpty()) {
                Log.d("ConfigRead", "Master KEK is missing or empty in config!")
                null
            } else {
                masterKey
            }
        } catch (e: Exception) {
            Log.e("ConfigRead", "Error reading config: ${e.message}", e)
            null
        }
    }

    /**
     * Saves the Master KEK into Config.json file.
     *
     * Behavior:
     * - Creates file if it does not exist
     * - Updates existing JSON if present
     * - Shows success dialog on completion
     *
     * @param navHostController Navigation controller for redirection
     * @param context Application context
     * @return true if save successful, false otherwise
     */
    fun saveMasterKEK(navHostController: NavHostController,context: Context): Boolean {
        val configFile = File(context.getExternalFilesDir(null), "Config.json")

        return try {
            val jsonObject = if (configFile.exists()) {
                val json = configFile.readText()
                Gson().fromJson(json, JsonObject::class.java)
            } else {
                JsonObject()
            }

            // Update or insert master_kek
            jsonObject.addProperty("master_kek", key)

            // Write back to file
            configFile.writeText(GsonBuilder().setPrettyPrinting().create().toJson(jsonObject))

            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_success),
                message = "Key Saved Successfully"
            )
            navHostController.navigateAndClean(AppNavigationItems.ActivationScreen.route)
            true
        } catch (e: Exception) {
            Log.e("ConfigWrite", "Error saving config: ${e.message}", e)
            false
        }
    }

}