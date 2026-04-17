package com.eazypaytech.posafrica.features.keyManagement.ui

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
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class KeyEntryViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    var key by mutableStateOf("")
        private set


    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(context: Context,sharedViewModel: SharedViewModel)
    {
        key = readMasterKEK(context,sharedViewModel).toString()
    }

    fun onCardNoChange(newValue: String) {
        key = newValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(context: Context,navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        if(key.isEmpty()) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = "Please Enter Key"
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

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

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