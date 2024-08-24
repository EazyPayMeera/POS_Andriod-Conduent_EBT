//package com.analogics.tpaymentsapos.rootUiScreens.login.view
//
//mport android.util.Log import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.Button
//import androidx.compose.material.ButtonDefaults
//import androidx.compose.material.IconButton
//import androidx.compose.material.Surface
//import androidx.compose.material.Text
//import androidx.compose.material.TextField
//import androidx.compose.material.TextFieldDefaults
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.Lock
//import androidx.compose.material.icons.filled.LockOpen
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.res.dimensionResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.analogics.tpaymentsapos.R
//import com.analogics.tpaymentsapos.rootUiScreens.appComponent.ContentD
//import com.analogics.tpaymentsapos.ui.theme.Dimens
//import com.analogics.tpaymentsapos.ui.theme.dimens
//
//@Composable
//fun LoginScreenView(navHostController: NavHostController) {
//    Surface {
//        var emailCredentails by remember { mutableStateOf("") }
//        var pwdCredentails by remember { mutableStateOf("") }
//        var passwordVisible by remember { mutableStateOf(false) }
//        Column(
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
//        ) {
//            Image(
//                imageVector = Icons.Default.LockOpen,
//                contentDescription = "My Image",
//                modifier = Modifier.padding(10.dp)
//            )
//            Text(
//                text = "Please Login to continue",
//                modifier = Modifier.heightIn(min = 40.dp),
//                textAlign = TextAlign.Center,
//                color = Color.Gray,
//                style = TextStyle(
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    fontStyle = FontStyle.Normal,
//                )
//            ) ElevatedTextField (value = emailCredentails, onValueChange = {
//            emailCredentails = it Log . e ("Username Input", emailCredentails)
//        }, label = "Username", leadingIcon = {
//            Icon(
//                imageVector = Icons.Default.Person, contentDescription = "Username Icon"
//            )
//        } ) Spacer(modifier = Modifier.height(16.dp)) ElevatedTextField(value = pwdCredentails, onValueChange = {
//            pwdCredentails = it Log . e ("Password Input", pwdCredentails)
//        }, label = "Password", leadingIcon = {
//            Icon(
//                imageVector = Icons.Default.Lock, contentDescription = "Password Icon"
//            )
//        }, trailingIcon = {
//            val image =
//                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff IconButton (onClick =
//                    { passwordVisible = !passwordVisible }) {
//                    Icon(
//                        imageVector = image, contentDescription = "Toggle Password Visibility"
//                    )
//                }
//        }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()) Text(text = "Forgot Password", color = colorResource(id = R.color.purple_200), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal, ), modifier = Modifier.padding(top = 10.dp)) Button(onClick = {
//            navHostController.navigate(
//                "login_screen"
//            )
//        }, modifier = Modifier .padding(top = 20.dp) .fillMaxWidth(), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500)), ) {
//            Row(
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Login", fontWeight = FontWeight.Bold, fontSize = 18.sp
//                ) Spacer (modifier = Modifier.width(8.dp)) Icon (imageVector =
//                    Icons.Default.ArrowForward, contentDescription = "Arrow")
//            }
//        }
//        }
//    }
//}
//
//@Composable
//fun ElevatedTextField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    label: String,
//    leadingIcon: @Composable (() -> Unit)? = null,
//    trailingIcon: @Composable (() -> Unit)? = null,
//    visualTransformation: VisualTransformation = VisualTransformation.None
//) {
//    var isFocused by remember { mutableStateOf(false) }
//    Box (modifier = Modifier
//        .fillMaxWidth()
//        .padding(5.dp)
//        .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
//        .background(color = Color.White, shape = RoundedCornerShape(8.dp))
//        .border(
//            width = 2.dp,
//            color = if (isFocused) Color(0xFFFFA500) else Color.Transparent,
//            shape = RoundedCornerShape(8.dp)
//        )) {
//        TextField(
//            value = value,
//            onValueChange = onValueChange,
//            label = { Text(label) },
//            leadingIcon = leadingIcon,
//            trailingIcon = trailingIcon,
//            visualTransformation = visualTransformation,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(5.dp)
//                .onFocusChanged { isFocused = it.isFocused || value.isNotEmpty() },
//            singleLine = true,
//            shape = RoundedCornerShape(8.dp),
//            colors = TextFieldDefaults.textFieldColors(
//                backgroundColor = Color.Transparent,
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                cursorColor = Color(0xFFFFA500)
//            )
//        )
//    }
//}