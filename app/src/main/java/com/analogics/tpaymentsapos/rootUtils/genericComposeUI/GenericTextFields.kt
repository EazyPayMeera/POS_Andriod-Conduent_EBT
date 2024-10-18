package com.analogics.tpaymentsapos.rootUtils.genericComposeUI


import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.ui.theme.dimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun InputTextField(
    enabled : Boolean ?= true,
    inputValue: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeHolder: String = "",
    icon: ImageVector = Icons.Default.Person,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: (KeyboardActionScope.() -> Unit)? = null, // Changed to nullable
    isPasswordField: Boolean = false,
    placeholderColor: Color = MaterialTheme.colorScheme.onSecondary,
    onActionDone: (() -> Unit)? = null // Added onActionDone parameter
) {
    var isPasswordVisible by remember { mutableStateOf(!isPasswordField) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = inputValue,
        onValueChange = onChange,
        modifier = modifier,
        enabled = enabled != false,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium), // Need to change Here
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        trailingIcon = {
            if (isPasswordField) {
                val visibilityIcon = if (isPasswordVisible) {
                    Icons.Default.Visibility
                } else {
                    Icons.Default.VisibilityOff
                }
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = visibilityIcon, contentDescription = null)
                }
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done // Set to Done action
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { onActionDone?.invoke() } // Call onActionDone when Done is pressed
        ),
        placeholder = {
            Text(text = placeHolder, color = placeholderColor)
        },
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPasswordField && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    title: String,
    image: Painter? = null, // Optional parameter for the image
    enabled: Boolean? = true
) {
    Box(
        modifier = Modifier
            .width(MaterialTheme.dimens.DP_248_CompactMedium)
            .height(MaterialTheme.dimens.DP_50_CompactMedium)
    )
    {
        Button(onClick = onClick,
            modifier = Modifier
                .align(Alignment.Center) // Align button at the bottom
                .fillMaxSize(),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
            colors = buttonColors(
                contentColor = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = enabled != false
            ) {
                if (image != null) {
                    Image(
                        painter = image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxHeight(),
                        )
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_11_CompactMedium))
                }

                Text(text = title, fontSize = MaterialTheme.dimens.SP_21_CompactMedium, fontWeight = FontWeight.Normal)
        }
    }
}


@Composable
fun getTransTypeString() : String
{
    val sharedViewModel = localSharedViewModel.current
    return when(sharedViewModel.objRootAppPaymentDetail.txnType){
        TxnType.PURCHASE -> stringResource(id = R.string.purchase)
        TxnType.REFUND -> stringResource(id = R.string.refund)
        TxnType.PREAUTH -> stringResource(id = R.string.pre_auth)
        TxnType.AUTHCAP -> stringResource(id = R.string.auth_capture)
        TxnType.VOID -> stringResource(id = R.string.void_trans)
        TxnType.TXNLIST -> stringResource(id=R.string.auth_capture)
        null -> stringResource(id = R.string.app_name)
    }
}



@Composable
fun CommonTopAppBar(
    title: String? = null,
    onBackButtonClick: () -> Unit,
    backgroundColor: Color = Color(0xFFF8F8F7),
    modifier: Modifier = Modifier,
    showBackIcon: Boolean = true // New parameter to control arrow visibility
) {
    TopAppBar(
        title = {
            Text(
                text = title ?: getTransTypeString(),
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        backgroundColor = backgroundColor,
        navigationIcon = if (showBackIcon) {
            {
                Box(
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_60_CompactMedium) // Set a larger size for the clickable area
                        .clickable {
                            onBackButtonClick()
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon in the Box
                            .size(24.dp) // Keep the icon size unchanged
                    )
                }
            }
        } else null, // If showBackIcon is false, do not display the navigation icon
        modifier = modifier
    )
}


@Composable
fun OkButton(
    onClick: () -> Unit,
    title: String,
    maxsizebutton: Boolean = true, // New parameter to control button size,
    enabled: Boolean?=false
) {
    Button(
        modifier = Modifier
            .then(
                if (maxsizebutton) {
                    Modifier
                        .width(MaterialTheme.dimens.DP_248_CompactMedium)
                        .height(MaterialTheme.dimens.DP_50_CompactMedium)
                } else {
                    Modifier
                        .width(MaterialTheme.dimens.DP_126_CompactMedium) // Specify a different size for the full button
                        .height(MaterialTheme.dimens.DP_50_CompactMedium)
                }
            ), // Using Modifier.then() to conditionally apply the size
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium), // Keep the shape here
        colors = buttonColors(
            contentColor = MaterialTheme.colorScheme.tertiary,
            containerColor = if(enabled==true) MaterialTheme.colorScheme.primary else colorResource(R.color.grey) // Keep or change as needed
        ),
        onClick = onClick
    ) {
        Text(
            text = title
        )
    }
}





@Composable
fun SettingsUpperSurface(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    color: Color = MaterialTheme.colorScheme.background,
    height: Dp, // Added customizable height parameter
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .height(height) // Applying customizable height
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(
                    topStart = MaterialTheme.dimens.DP_24_CompactMedium,
                    topEnd = MaterialTheme.dimens.DP_24_CompactMedium
                )
            ),
        shape = RoundedCornerShape(topStart = MaterialTheme.dimens.DP_24_CompactMedium, topEnd = MaterialTheme.dimens.DP_24_CompactMedium),
        color = color
    ) {
        content()
    }
}

@Composable
fun SettingsMiddleSurface(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    color: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation,
            ),
        color = color
    ) {
        content()
    }
}

@Composable
fun SettingsLowerSurface(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    color: Color = MaterialTheme.colorScheme.background,
    height: Dp, // Customizable height parameter
    bottomStartRadius: Dp = MaterialTheme.dimens.DP_24_CompactMedium,
    bottomEndRadius: Dp = MaterialTheme.dimens.DP_24_CompactMedium,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .height(height) // Applying customizable height
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(
                    bottomStart = bottomStartRadius,
                    bottomEnd = bottomEndRadius
                )
            ),
        shape = RoundedCornerShape(
            bottomStart = bottomStartRadius,
            bottomEnd = bottomEndRadius
        ),
        color = color
    ) {
        content()
    }
}

/*@Composable
fun FooterButtons(
    firstButtonTitle: String,
    firstButtonOnClick: () -> Unit,
    secondButtonTitle: String,
    secondButtonOnClick: () -> Unit,
    alignment: Alignment = Alignment.BottomCenter // Default alignment
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium) // Adjust padding as needed // need to change
    ) {
        Row(
            modifier = Modifier
                .align(alignment)
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_23_CompactMedium), // Adjust vertical padding if needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var isFirstButtonPressed by remember { mutableStateOf(false) }
            var isSecondButtonPressed by remember { mutableStateOf(false) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(MaterialTheme.dimens.DP_126_CompactMedium)
                    .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                    .shadow(
                        MaterialTheme.dimens.DP_4_CompactMedium,
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                    )
            ) {
                Button(
                    onClick = {
                        isFirstButtonPressed = true
                        firstButtonOnClick()
                    },
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
                    modifier = Modifier
                        .width(*//*MaterialTheme.dimens.DP_130_CompactMedium*//*130.dp)
                        .height(MaterialTheme.dimens.DP_48_CompactMedium)
                        .border(
                            width = if (isFirstButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else 0.dp,
                            color = if (isFirstButtonPressed) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        ),
                    colors = buttonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        containerColor = colorResource(R.color.grey)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                        pressedElevation = MaterialTheme.dimens.DP_12_CompactMedium,
                        hoveredElevation = MaterialTheme.dimens.DP_10_CompactMedium,
                        focusedElevation = MaterialTheme.dimens.DP_11_CompactMedium
                    )
                ) {

                    TextView(
                        text = firstButtonTitle.uppercase(),
                        fontSize = *//*MaterialTheme.dimens.SP_21_CompactMedium*//*15.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        1,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LaunchedEffect(isFirstButtonPressed) {
                if (isFirstButtonPressed) {
                    //kotlinx.coroutines.delay(100)
                    isFirstButtonPressed = false
                }
            }

            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .width(MaterialTheme.dimens.DP_126_CompactMedium)
                    .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                    .shadow(
                        MaterialTheme.dimens.DP_4_CompactMedium,
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                    )
            ) {
                Button(
                    onClick = {
                        isSecondButtonPressed = true
                        secondButtonOnClick()
                    },
                    modifier = Modifier
                        .width(MaterialTheme.dimens.DP_145_CompactMedium)
                        .height(MaterialTheme.dimens.DP_48_CompactMedium)
                        .border(
                            width = if (isSecondButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else 0.dp,
                            color = if (isSecondButtonPressed) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        ),
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
                    colors = buttonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        containerColor = colorResource(R.color.grey)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                        pressedElevation = MaterialTheme.dimens.DP_12_CompactMedium,
                        hoveredElevation = MaterialTheme.dimens.DP_10_CompactMedium,
                        focusedElevation = MaterialTheme.dimens.DP_11_CompactMedium
                    )
                ) {

                    TextView(
                        text = secondButtonTitle.uppercase(),
                        fontSize = *//*MaterialTheme.dimens.SP_21_CompactMedium*//*15.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        1,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LaunchedEffect(isSecondButtonPressed) {
                if (isSecondButtonPressed) {
                    kotlinx.coroutines.delay(100)
                    isSecondButtonPressed = false
                }
            }
        }
    }
}*/

@Composable
fun FooterButtons(
    firstButtonTitle: String?=null,
    firstButtonOnClick: (() -> Unit)?={},
    secondButtonTitle: String?=null,
    secondButtonOnClick: (() -> Unit)?={},
    alignment: Alignment = Alignment.BottomCenter // Default alignment
) {
    // State to track keyboard visibility
    val context = LocalContext.current
    val isKeyboardVisible = remember { mutableStateOf(false) }

    fun updateKeyboardState(view: View) {
        val isKeyboardOpen =
            ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) != false
        isKeyboardVisible.value = isKeyboardOpen
    }
    // Get the current view
    val rootView = LocalView.current
    val view = LocalView.current
    // Use DisposableEffect to set up a listener for layout changes
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            updateKeyboardState(view)
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    LaunchedEffect(view) {
        updateKeyboardState(view)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium) // Adjust padding as needed
    ) {
        Row(
            modifier = Modifier
                .align(if (isKeyboardVisible.value) Alignment.TopCenter else alignment)
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_23_CompactMedium), // Adjust vertical padding if needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var isFirstButtonPressed by remember { mutableStateOf(false) }
            var isSecondButtonPressed by remember { mutableStateOf(false) }

            // First Button
            firstButtonTitle?.let {
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .width(MaterialTheme.dimens.DP_126_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .clickable {
                            isFirstButtonPressed = true
                            firstButtonOnClick?.invoke()
                        }
                        .padding(MaterialTheme.dimens.DP_20_CompactMedium) // Increase padding to enlarge touchable area
                        .shadow(
                            MaterialTheme.dimens.DP_4_CompactMedium,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        )
                ) {
                    Button(
                        onClick = {
                            isFirstButtonPressed = true
                            firstButtonOnClick?.invoke()
                        },
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
                        modifier = Modifier
                            .width(MaterialTheme.dimens.DP_145_CompactMedium)
                            .height(MaterialTheme.dimens.DP_48_CompactMedium)
                            .border(
                                width = if (isFirstButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else 0.dp,
                                color = if (isFirstButtonPressed) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                            ),
                        colors = buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = colorResource(R.color.grey)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_12_CompactMedium,
                            hoveredElevation = MaterialTheme.dimens.DP_10_CompactMedium,
                            focusedElevation = MaterialTheme.dimens.DP_11_CompactMedium
                        )
                    ) {
                        TextView(
                            text = firstButtonTitle.uppercase(),
                            fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Second Button
            secondButtonTitle?.let {
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .width(MaterialTheme.dimens.DP_126_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium)
                        .clickable {
                            isSecondButtonPressed = true
                            secondButtonOnClick?.invoke()
                        }
                        .padding(8.dp) // Increase padding to enlarge touchable area
                        .shadow(
                            MaterialTheme.dimens.DP_4_CompactMedium,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        )
                ) {
                    Button(
                        onClick = {
                            isSecondButtonPressed = true
                            secondButtonOnClick?.invoke()
                        },
                        modifier = Modifier
                            .width(MaterialTheme.dimens.DP_145_CompactMedium)
                            .height(MaterialTheme.dimens.DP_48_CompactMedium)
                            .border(
                                width = if (isSecondButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else 0.dp,
                                color = if (isSecondButtonPressed) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                            ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
                        colors = buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = colorResource(R.color.grey)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_12_CompactMedium,
                            hoveredElevation = MaterialTheme.dimens.DP_10_CompactMedium,
                            focusedElevation = MaterialTheme.dimens.DP_11_CompactMedium
                        )
                    ) {
                        TextView(
                            text = secondButtonTitle.uppercase(),
                            fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_12_CompactMedium),
        modifier = modifier
            .padding(MaterialTheme.dimens.DP_2_CompactMedium)
            .width(MaterialTheme.dimens.DP_180_CompactMedium)
            .height(MaterialTheme.dimens.DP_70_CompactMedium)
    ) {
        Text(
            text = text,
            fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getFormattedDateTime(): String {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd-MM-yyyy @ HH:mm:ss", Locale.getDefault())
    return dateFormat.format(calendar.time)
}



@Composable
fun CardWithImageText(
    text: String,
    imageResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GenericCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = MaterialTheme.dimens.DP_2_CompactMedium, // Adjust the border width as needed
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, // Orange color if selected, otherwise transparent
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_20_CompactMedium)
            ),
        backgroundColor = MaterialTheme.colorScheme.secondary,
        elevation = MaterialTheme.dimens.DP_5_CompactMedium,
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth() // Fills the width available
                .padding(
                    start = MaterialTheme.dimens.DP_22_CompactMedium,
                    end = MaterialTheme.dimens.DP_22_CompactMedium,
                    top = MaterialTheme.dimens.DP_11_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_11_CompactMedium
                )
        ) {
            ImageView(
                imageId = imageResId,
                modifier = Modifier
                    .size(MaterialTheme.dimens.DP_40_CompactMedium) // Adjust the size as needed
                    .align(Alignment.CenterHorizontally),
                contentDescription = "" // Center the image
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium))
            TextView(text = text, fontSize = MaterialTheme.dimens.SP_18_CompactMedium)
        }
    }
}


@Composable
fun AppHeader(
    title: String,
    backgroundColor: Color = Color(0xFFF8F8F7),
    icon1: Int? = null,
    icon2: Int? = null,
    onIcon1Click: (() -> Unit)? = null,
    onIcon2Click: (() -> Unit)? = null,
    isIcon1Visible: Boolean = true,
    isIcon2Visible: Boolean = true,
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    TopAppBar(

        title = {
            // Add Spacer before title only when both icons are null
            if (icon1 == null && icon2 == null) {
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_11_CompactMedium)) // Adjust the width as per your layout needs
            }

            Text(
                text = title,
                color = MaterialTheme.colorScheme.tertiary, // Ensure text color contrasts with the background
                style = TextStyle(
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium, // Ensure font size is large enough
                    fontWeight = FontWeight.Bold
                )
            )
        },
        backgroundColor = backgroundColor,
        navigationIcon = if (isIcon1Visible && icon1 != null) {
            {
                Box(
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_60_CompactMedium) // Same touch area size as in CommonTopAppBar
                        .clickable { onIcon1Click?.invoke() }
                ) {
                    Image(
                        painter = painterResource(id = icon1),
                        contentDescription = "icon1",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon in the Box
                            .size(24.dp) // Set the icon size
                    )
                }
            }
        } else null, // Don't add the navigation icon if it's not visible or null
        actions = {
            if (isIcon2Visible && icon2 != null) {
                Box(
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_60_CompactMedium) // Same touch area size for the action icon
                        .clickable { onIcon2Click?.invoke() }
                ) {
                    Image(
                        painter = painterResource(id = icon2),
                        contentDescription = "icon2",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon in the Box
                            .size(24.dp) // Set the icon size
                    )
                }
            }
        },
        modifier = modifier
    )
}



@Composable
fun BackgroundScreen(componentView :@Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.DP_24_CompactMedium)
            .shadow(
                elevation = MaterialTheme.dimens.DP_50_CompactMedium,
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium)
            )
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium)
            )
    ) {
        Card(
            elevation =  MaterialTheme.dimens.DP_5_CompactMedium,
            backgroundColor= MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = MaterialTheme.dimens.DP_25_CompactMedium,
                    start = MaterialTheme.dimens.DP_25_CompactMedium,
                    end = MaterialTheme.dimens.DP_25_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_25_CompactMedium,
                )
                .align(Alignment.Center)
        ) {
            componentView()
        }
    }
}

@Composable
fun ImageView(
    imageId: Int,
    size: Dp = MaterialTheme.dimens.DP_70_CompactMedium,
    shape: Shape = RectangleShape,
    alignment: Alignment = Alignment.Center, // Alignment parameter for usage within a Box
    modifier: Modifier = Modifier,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(modifier), // Apply any additional modifiers passed in
        contentAlignment = alignment // Set the alignment within the Box
    ) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape) // Apply the shape clipping to the image
        )
    }
}
val passwordTransform = object : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence, view: View?): CharSequence {
        val transformed = super.getTransformation(source, view)
        // Convert to String and replace bullet character with '*'
        return transformed.toString().replace('\u2022', '*')
    }
}

@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    textStyle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium),
    keyboardType: KeyboardType = KeyboardType.Text,
    onDoneAction: () -> Unit = {},
    isPassword: Boolean = false, // New parameter to indicate if it's a password field
    visualTransformation: VisualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    modifier: Modifier = Modifier,
    amount: Boolean = false, // New parameter to indicate if ₹ icon should be shown
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
) {
    // Create a FocusRequester instance
    val focusRequester = remember { FocusRequester() }

    // Handle value change with length restriction if `amount` flag is true
    val handleValueChange: (TextFieldValue) -> Unit = { newValue ->
        if (amount) {
            if(removeNonDigits(newValue.text).length <= 12) onValueChange(formatAmount(newValue.text)) // Restrict input to 12 characters
        } else {
            onValueChange(newValue.text)
        }
    }

    OutlinedTextField(
        value = TextFieldValue(value, selection = TextRange(value.length)),
        onValueChange = handleValueChange,
        shape = shape,
        label = { Text("") }, // Label is always empty
        placeholder = { Text(placeholder, fontSize = MaterialTheme.dimens.SP_23_CompactMedium) },
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDoneAction() }
        ),
        visualTransformation = visualTransformation, // Use passed visualTransformation
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        modifier = modifier
            .focusRequester(focusRequester)
            .padding(MaterialTheme.dimens.DP_2_CompactMedium)
            .width(MaterialTheme.dimens.DP_280_CompactMedium)
            .height(MaterialTheme.dimens.DP_70_CompactMedium),
        prefix = if (amount) {
            {
                Text(
                    text = "\u20B9", // Unicode for ₹ symbol
                    fontSize = MaterialTheme.dimens.SP_29_CompactMedium, // Adjust the size of the icon
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary, // Set the color to black,
                    textAlign = TextAlign.Center

                )
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary, // Orange color for focused state
            unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer, // Light grey color for unfocused state
            focusedLabelColor = MaterialTheme.colorScheme.primary, // Orange color for focused label
            unfocusedLabelColor = MaterialTheme.colorScheme.primaryContainer, // Light grey color for unfocused label,
            cursorColor = Color.Transparent
        )

    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}







