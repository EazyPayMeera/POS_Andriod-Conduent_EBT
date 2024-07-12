package com.analogics.tpaymentsapos.rootUiScreens.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.InputTextField


@Composable
fun LoginScreenView(navHostController: NavHostController)
{
    Surface {
        var emailCredentails by remember { mutableStateOf("") }
        var pwdCredentails by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
        ) {
            InputTextField(
                inputValue = emailCredentails,
            onChange ={emailCredentails=it
                       Log.e("Text Input value",emailCredentails)
            },
                modifier = Modifier.fillMaxWidth()
        )
            InputTextField(
                inputValue = pwdCredentails,
                onChange ={pwdCredentails=it
                    Log.e("Text Input value",pwdCredentails)
                },
                modifier = Modifier.fillMaxWidth(),
                placeHolder = "Password",
                label = "Password",
                icon = Icons.Default.Lock
            )
            Box(
                modifier = Modifier.padding(top = 30.dp)
            )
            {
                AppButton(onClick = {
                    navHostController.navigate(AppNavigationItems.LoginScreen.route)
                }, title = "Get Login")
            }
        }
    }
}