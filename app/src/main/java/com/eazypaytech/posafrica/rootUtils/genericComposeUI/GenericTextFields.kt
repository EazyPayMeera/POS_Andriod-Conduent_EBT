package com.eazypaytech.posafrica.rootUtils.genericComposeUI


import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.ui.theme.dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin


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
fun LoginButton(
    onClick: () -> Unit,
    title: String,
    image: Painter? = null, // Optional parameter for the image
    enabled: Boolean? = true
) {
    val isKeyboardVisible = remember { mutableStateOf(false) }

    // Detect keyboard visibility using DisposableEffect
    val rootView = LocalView.current
    DisposableEffect(rootView) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen =
                ViewCompat.getRootWindowInsets(rootView)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            isKeyboardVisible.value = isKeyboardOpen
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    // Adjust padding based on keyboard visibility
    val paddingBottom = if (isKeyboardVisible.value) MaterialTheme.dimens.DP_25_CompactMedium else MaterialTheme.dimens.DP_40_CompactMedium

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingBottom) // Adjust padding based on keyboard visibility
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .align(if (isKeyboardVisible.value) Alignment.TopCenter else Alignment.BottomCenter) // Align based on keyboard visibility
                .width(MaterialTheme.dimens.DP_248_CompactMedium)
                .height(MaterialTheme.dimens.DP_50_CompactMedium),
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
                    modifier = Modifier.fillMaxHeight()
                )
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_11_CompactMedium))
            }

            Text(
                text = title,
                style = TextStyle(fontSize = MaterialTheme.dimens.SP_21_CompactMedium, fontWeight = FontWeight.Normal)
            )
        }
    }
}


@Composable
fun getTransTypeString(): String {
    val sharedViewModel = localSharedViewModel.current
    return when (sharedViewModel.objRootAppPaymentDetail.txnType) {
        TxnType.PURCHASE_CASHBACK -> stringResource(id = R.string.ebt_purchase_cashback)
        TxnType.CASH_PURCHASE -> stringResource(id = R.string.ebt_cash)
        TxnType.BALANCE_ENQUIRY_SNAP -> stringResource(id = R.string.ebt_bal_enquiry)
        TxnType.BALANCE_ENQUIRY_CASH -> stringResource(id = R.string.ebt_bal_enquiry)
        TxnType.VOUCHER_CLEAR -> stringResource(id = R.string.ebt_voucher_clear)
        TxnType.VOUCHER_RETURN -> stringResource(id = R.string.ebt_voucher_return)
        TxnType.VOID_LAST -> stringResource(id = R.string.ebt_void_last)
        TxnType.FOOD_PURCHASE -> stringResource(id = R.string.ebt_food_purchase)
        TxnType.FOODSTAMP_RETURN -> stringResource(id = R.string.ebt_foodstamp_return)
        TxnType.E_VOUCHER -> stringResource(id = R.string.ebt_e_voucher)
        null -> stringResource(id = R.string.app_name)
    }
}




@Composable
fun CommonTopAppBar(
    title: String? = null,
    onBackButtonClick: () -> Unit,
    backgroundColor: Color = Color(0xFFF8F8F7),
    modifier: Modifier = Modifier,
    showBackIcon: Boolean = true, // New parameter to control arrow visibility
    closeKeypadOnBackButton : Boolean?=false,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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
                        .size(MaterialTheme.dimens.DP_60_CompactMedium) // Invisible touch area for better touch responsiveness
                        .clickable(
                            onClick = { if(closeKeypadOnBackButton ==  true) keyboardController?.hide(); onBackButtonClick() },
                            indication = LocalIndication.current, // Remove ripple effect if it's not required
                            interactionSource = remember { MutableInteractionSource() }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon
                            .size(MaterialTheme.dimens.DP_23_CompactMedium) // Icon remains the same size
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
    val interactionSource = remember { MutableInteractionSource() }
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
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Text(
            text = title
        )
    }
}



@Composable
fun SettingsUpperSurface(
    modifier: Modifier = Modifier,
    elevation: Dp = MaterialTheme.dimens.DP_0_CompactMedium,
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
    elevation: Dp = MaterialTheme.dimens.DP_0_CompactMedium,
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
    elevation: Dp = MaterialTheme.dimens.DP_0_CompactMedium,
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

@Composable
fun FooterButtons(
    firstButtonTitle: String?=null,
    firstButtonOnClick: (() -> Unit)?={},
    secondButtonTitle: String?=null,
    secondButtonOnClick: (() -> Unit)?={},
    alignment: Alignment = Alignment.BottomCenter, // Default alignment
    enabled: Boolean?=true,
    closeKeypadOnFirstButton : Boolean?=false,
    closeKeypadOnSecondButton : Boolean?=false
) {
    val isKeyboardVisible = remember { mutableStateOf(false) }
    var isFirstButtonPressed by remember { mutableStateOf(false) }
    var isSecondButtonPressed by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

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

    LaunchedEffect(isFirstButtonPressed,isSecondButtonPressed) {
        delay(AppConstants.BUTTON_CLICK_EFFECT_MS)
        isFirstButtonPressed = false
        isSecondButtonPressed = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = MaterialTheme.dimens.DP_30_CompactMedium) // Adjust padding as needed
    ) {
        Row(
            modifier = Modifier
                .align(if (isKeyboardVisible.value) Alignment.TopCenter else alignment)
                .fillMaxWidth(),
                //.padding(vertical = MaterialTheme.dimens.DP_23_CompactMedium), // Adjust vertical padding if needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // First Button
            firstButtonTitle?.let {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    enabled = enabled!=false,
                    onClick = {
                        isFirstButtonPressed = true
                        if(closeKeypadOnFirstButton ==  true) keyboardController?.hide()
                        firstButtonOnClick?.invoke()
                    },
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
                    modifier = Modifier
                        .width(MaterialTheme.dimens.DP_145_CompactMedium)
                        .height(MaterialTheme.dimens.DP_50_CompactMedium)
                        .border(
                            width = if (isFirstButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else MaterialTheme.dimens.DP_0_CompactMedium,
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
                    ),
                    interactionSource = interactionSource
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

            // Second Button
            secondButtonTitle?.let {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    enabled = enabled!=false,
                    onClick = {
                        isSecondButtonPressed = true
                        if(closeKeypadOnSecondButton ==  true) keyboardController?.hide()
                        secondButtonOnClick?.invoke()
                    },
                    modifier = Modifier
                        .width(MaterialTheme.dimens.DP_145_CompactMedium)
                        .height(MaterialTheme.dimens.DP_50_CompactMedium)
                        .border(
                            width = if (isSecondButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else MaterialTheme.dimens.DP_0_CompactMedium,
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
                    ) ,
                    interactionSource = interactionSource
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isClicked = remember { mutableStateOf(false) }

    GenericCard(
        modifier = modifier
            .border(
                width = MaterialTheme.dimens.DP_2_CompactMedium, // Adjust the border width as needed
                color = if (isClicked.value) MaterialTheme.colorScheme.primary else Color.Transparent, // Orange color if selected, otherwise transparent
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_20_CompactMedium)
            )
            .clickable(
                onClick = { onClick(); isClicked.value = true },
                indication = null, // Disable the default ripple effect if not needed
                interactionSource = remember { MutableInteractionSource() }
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
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_12_CompactMedium))
            TextView(text = text, fontSize = MaterialTheme.dimens.SP_8_CompactMedium)
        }
    }

    LaunchedEffect(isClicked.value) {
        if(isClicked.value==true) {
            delay(AppConstants.BUTTON_CLICK_EFFECT_MS)
            isClicked.value = false
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
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon in the Box
                            .size(MaterialTheme.dimens.DP_23_CompactMedium) // Set the icon size
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
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon in the Box
                            .size(MaterialTheme.dimens.DP_23_CompactMedium) // Set the icon size
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
fun DialogueSurface(componentView :@Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
    isPassword: Boolean = false,
    visualTransformation: VisualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    modifier: Modifier = Modifier,
    amount: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }

    // Sync external value changes
    LaunchedEffect(value) {
        textFieldValue = TextFieldValue(value, selection = TextRange(value.length))
    }

    val handleValueChange: (String) -> Unit = { newText ->
        val formattedText = if (amount) formatAmount(newText) else newText
        if (!amount || removeNonDigits(formattedText).length <= 12) {
            textFieldValue = TextFieldValue(formattedText, selection = TextRange(formattedText.length))
            onValueChange(formattedText)
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { handleValueChange(it.text) },
        shape = shape,
        label = null,
        placeholder = { Text(placeholder, fontSize = MaterialTheme.dimens.SP_23_CompactMedium) },
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDoneAction() }),
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) {
                focusRequester.requestFocus()
            }
            .focusRequester(focusRequester)
            .padding(MaterialTheme.dimens.DP_2_CompactMedium)
            .width(MaterialTheme.dimens.DP_280_CompactMedium)
            .height(MaterialTheme.dimens.DP_70_CompactMedium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}


@Composable
fun CircularMenu(
    menuOptions: List<String>, // Accept the list of menu options
    onMenuOptionClick: (String) -> Unit,
    onPrintClick: () -> Unit // Add a new parameter for the print click action
) {
    var expanded by remember { mutableStateOf(false) }
    val distance = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val printButtonInitialColor = MaterialTheme.colorScheme.primary
    var printButtonColor by remember { mutableStateOf(printButtonInitialColor) }

    // Animation effect when expanded
    LaunchedEffect(expanded) {
        distance.animateTo(
            targetValue = if (expanded) 80f else 0f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = Modifier
            .size(MaterialTheme.dimens.DP_100_CompactMedium)
            .padding(MaterialTheme.dimens.DP_0_CompactMedium),
        contentAlignment = Alignment.Center
    ) {
        // Loop through menuOptions and position them around the center
        menuOptions.forEachIndexed { index, option ->
            val angle = when (index) {
                0 -> -30f // Right
                1 -> 210f // Left
                else -> 0f
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(
                        x = (distance.value * cos(Math.toRadians(angle.toDouble()))).dp,
                        y = (distance.value * sin(Math.toRadians(angle.toDouble()))).dp
                    )
                    .size(MaterialTheme.dimens.DP_60_CompactMedium)
                    .shadow(MaterialTheme.dimens.DP_4_CompactMedium, shape = CircleShape)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                    .clickable {
                        onMenuOptionClick(option)
                        expanded = false
                        scope.launch {
                            printButtonColor = printButtonInitialColor
                        }
                    }
            ) {
                androidx.compose.material.Text(
                    text = option,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = MaterialTheme.dimens.SP_13_CompactMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Main print button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MaterialTheme.dimens.DP_60_CompactMedium)
                .shadow(
                    MaterialTheme.dimens.DP_4_CompactMedium,
                    shape = CircleShape
                )
                .background(printButtonColor, shape = CircleShape)
                .clickable {
                    onPrintClick()
                    scope.launch {
                        printButtonColor = if (expanded) {
                            Color.Gray
                        } else {
                            printButtonInitialColor
                        }
                    }
                    expanded = !expanded
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.print_logo), // Replace with your image resource
                contentDescription = stringResource(id = R.string.print), // Provide a content description for accessibility
                modifier = Modifier.size(MaterialTheme.dimens.DP_60_CompactMedium) // Adjust size as needed
            )
        }
    }
}




