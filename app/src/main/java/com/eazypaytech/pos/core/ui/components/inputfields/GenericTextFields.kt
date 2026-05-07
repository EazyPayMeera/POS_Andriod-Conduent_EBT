package com.eazypaytech.pos.core.ui.components.inputfields


import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.utils.formatAmount
import com.eazypaytech.pos.core.utils.removeNonDigits
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.core.themes.dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom reusable input field based on Material 3 OutlinedTextField.
 *
 * Features:
 * - Leading icon support
 * - Optional password masking with toggle visibility
 * - Custom keyboard actions (Done/Next)
 * - Placeholder + label support
 * - Themed styling using app dimens and color system
 *
 * Designed for POS applications where consistent input UI is required
 * across login, forms, and configuration screens.
 *
 * @param enabled Enables/disables input interaction
 * @param inputValue Current text value
 * @param onChange Callback when text changes
 * @param modifier Compose modifier
 * @param label Field label text
 * @param placeHolder Placeholder text inside field
 * @param icon Leading icon
 * @param keyboardType Keyboard type (text, number, password, etc.)
 * @param keyboardActions Optional custom keyboard actions override
 * @param isPasswordField Enables password masking and toggle visibility icon
 * @param placeholderColor Placeholder text color
 * @param onActionDone Callback when IME Done is pressed
 */
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

/**
 * Primary application button used across POS flows.
 *
 * Features:
 * - Optional icon support
 * - Fixed standard dimensions (design system based)
 * - Material 3 styling
 * - Disabled state support
 *
 * Used for:
 * - Login
 * - Payment actions
 * - Navigation confirmations
 */
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

/**
 * Login screen primary action button.
 *
 * Special behavior:
 * - Moves based on keyboard visibility
 * - Prevents overlap with IME
 * - Automatically shifts alignment (top/bottom)
 *
 * Used only in login/auth flows.
 */
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

/**
 * Returns transaction type display label based on current payment flow.
 *
 * NOTE:
 * Uses sharedViewModel state (global transaction context).
 */
@Composable
fun getTransTypeString(): String {
    val sharedViewModel = localSharedViewModel.current
    return when (sharedViewModel.objRootAppPaymentDetail.txnType) {
        TxnType.PURCHASE_CASHBACK -> stringResource(id = R.string.ebt_purchase_cashback)
        TxnType.CASH_PURCHASE -> stringResource(id = R.string.ebt_cash_benefit)
        TxnType.CASH_WITHDRAWAL -> stringResource(id = R.string.ebt_cash_withdrawal)
        TxnType.BALANCE_ENQUIRY_SNAP -> stringResource(id = R.string.ebt_bal_inquiry)
        TxnType.BALANCE_ENQUIRY_CASH -> stringResource(id = R.string.ebt_bal_inquiry)
        TxnType.VOUCHER_CLEAR -> stringResource(id = R.string.ebt_voucher_clear)
        TxnType.VOUCHER_RETURN -> stringResource(id = R.string.ebt_voucher_return)
        TxnType.VOID_LAST -> stringResource(id = R.string.ebt_void_last)
        TxnType.FOOD_PURCHASE -> stringResource(id = R.string.ebt_food_purchase)
        TxnType.FOODSTAMP_RETURN -> stringResource(id = R.string.ebt_foodstamp_return)
        TxnType.E_VOUCHER -> stringResource(id = R.string.ebt_e_voucher)
        null -> stringResource(id = R.string.ebt_bal_inquiry)
    }
}

@Composable
fun getVoidTransTypeString(txnType: String?): String {
    return when (txnType) {
        TxnType.CASH_PURCHASE.toString() -> stringResource(id = R.string.ebt_cash_benefit)
        TxnType.FOOD_PURCHASE.toString() -> stringResource(id = R.string.ebt_food_purchase)
        TxnType.FOODSTAMP_RETURN.toString() -> stringResource(id = R.string.ebt_foodstamp_return)
        else -> stringResource(id = R.string.ebt_bal_inquiry)
    }
}

/**
 * Returns label for transaction amount field based on transaction type.
 */
@Composable
fun getTransTypeAmountTitle(): String {
    val sharedViewModel = localSharedViewModel.current
    return when (sharedViewModel.objRootAppPaymentDetail.txnType) {
        TxnType.PURCHASE_CASHBACK -> stringResource(id = R.string.ebt_cash_amount)
        TxnType.CASH_PURCHASE -> stringResource(id = R.string.ebt_cash_amount)
        TxnType.CASH_WITHDRAWAL -> stringResource(id = R.string.ebt_cash_withdrawal_amount)
        TxnType.BALANCE_ENQUIRY_SNAP -> stringResource(id = R.string.ebt_bal_inquiry)
        TxnType.BALANCE_ENQUIRY_CASH -> stringResource(id = R.string.ebt_bal_inquiry)
        TxnType.VOUCHER_CLEAR -> stringResource(id = R.string.ebt_voucher_clear)
        TxnType.VOUCHER_RETURN -> stringResource(id = R.string.ebt_voucher_return)
        TxnType.VOID_LAST -> stringResource(id = R.string.ebt_void_last)
        TxnType.FOOD_PURCHASE -> stringResource(id = R.string.purchase_snap_amt)
        TxnType.FOODSTAMP_RETURN -> stringResource(id = R.string.return_amt)
        TxnType.E_VOUCHER -> ""
        null -> stringResource(id = R.string.ebt_bal_inquiry)
    }
}




/**
 * Custom Top App Bar used across POS screens.
 *
 * Features:
 * - Dynamic title (or transaction-based fallback)
 * - Optional back button
 * - Optional keyboard dismissal on back
 */
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
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    fontWeight = FontWeight.Medium
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


/**
 * Simple OK confirmation button.
 *
 * Features:
 * - Two size variants (standard / compact)
 * - Disabled state support via color change
 */
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

/**
 * A reusable footer button component with one or two action buttons.
 *
 * Features:
 * - Supports up to two buttons (primary + secondary)
 * - Automatically adjusts position when keyboard is visible
 * - Optional keyboard dismissal on button click
 * - Click animation effect
 * - Customizable labels and click actions
 *
 * @param firstButtonTitle Label for first button (nullable)
 * @param firstButtonOnClick Click callback for first button
 * @param secondButtonTitle Label for second button (nullable)
 * @param secondButtonOnClick Click callback for second button
 * @param alignment Button alignment when keyboard is hidden
 * @param enabled Enables/disables both buttons
 * @param closeKeypadOnFirstButton Whether to hide keyboard on first button click
 * @param closeKeypadOnSecondButton Whether to hide keyboard on second button click
 */
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



/**
 * A clickable card UI component displaying centered text.
 *
 * Features:
 * - Card with border selection effect
 * - Click animation state handling
 * - Custom styling via GenericCard
 *
 * @param text Text displayed inside the card
 * @param onClick Callback triggered when card is clicked
 * @param modifier Modifier for external customization
 */
@Composable
fun CardWithImageText(
    text: String,
    //imageResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isClicked = remember { mutableStateOf(false) }

    GenericCard(
        modifier = modifier
            .border(
                width = 0.6.dp, // Adjust the border width as needed
                color = if (isClicked.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiary, // Orange color if selected, otherwise transparent
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
                .fillMaxWidth()   // 👈 important
                .height(120.dp)
                .padding(
                    start = MaterialTheme.dimens.DP_22_CompactMedium,
                    end = MaterialTheme.dimens.DP_22_CompactMedium,
                    top = MaterialTheme.dimens.DP_23_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_23_CompactMedium
                )
        ) {
            TextView(
                text = text,
                fontSize = MaterialTheme.dimens.SP_13_CompactMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    LaunchedEffect(isClicked.value) {
        if(isClicked.value==true) {
            delay(AppConstants.BUTTON_CLICK_EFFECT_MS)
            isClicked.value = false
        }
    }
}


/**
 * Top app bar component with title and optional action icons.
 *
 * Features:
 * - Title display with bold styling
 * - Optional left and right icons
 * - Click actions for both icons
 * - Custom background color support
 *
 * @param title Header title text
 * @param backgroundColor Background color of the app bar
 * @param icon1 Left icon resource (navigation icon)
 * @param icon2 Right icon resource (action icon)
 * @param onIcon1Click Callback for left icon click
 * @param onIcon2Click Callback for right icon click
 * @param isIcon1Visible Controls visibility of left icon
 * @param isIcon2Visible Controls visibility of right icon
 * @param modifier Modifier for styling
 * @param onBackButtonClick Back navigation callback
 */
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


/**
 * A reusable background container screen that provides a styled card layout.
 *
 * This component:
 * - Adds padding and shadowed background surface
 * - Wraps content inside a styled Material Card
 * - Provides consistent screen layout structure for forms and pages
 *
 * @param componentView Composable content to render inside the screen
 */
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
                color = MaterialTheme.colorScheme.onTertiary,
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_17_CompactMedium)
            )
    ) {
        Card(
            elevation =  MaterialTheme.dimens.DP_5_CompactMedium,
            backgroundColor= MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = MaterialTheme.dimens.DP_25_CompactMedium,
                    start = MaterialTheme.dimens.DP_11_CompactMedium,
                    end = MaterialTheme.dimens.DP_11_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_25_CompactMedium,
                )
                .align(Alignment.Center)
        ) {
            componentView()
        }
    }
}

/**
 * A dialog-style surface container with elevated background styling.
 *
 * Features:
 * - Full-screen overlay layout
 * - Shadowed and rounded background surface
 * - Inner card container for dialog content
 *
 * Typically used for modal/dialog UI screens.
 *
 * @param componentView Composable content displayed inside the dialog surface
 */
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

/**
 * A reusable image component with configurable size, shape, and alignment.
 *
 * Features:
 * - Loads image from drawable resource
 * - Supports clipping with custom shape
 * - Flexible alignment inside container
 * - Customizable size and modifiers
 *
 * @param imageId Drawable resource ID
 * @param size Image size (default: 70dp)
 * @param shape Shape used to clip the image
 * @param alignment Alignment inside parent box
 * @param modifier External modifier for styling
 * @param contentDescription Accessibility description for the image
 */
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


/**
 * Custom password transformation that replaces masking bullets with '*' character.
 *
 * Overrides default Android password bullet (•) with asterisk (*)
 * for UI consistency across the application.
 */
val passwordTransform = object : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence, view: View?): CharSequence {
        val transformed = super.getTransformation(source, view)
        // Convert to String and replace bullet character with '*'
        return transformed.toString().replace('\u2022', '*')
    }
}

/**
 * Custom OutlinedTextField with enhanced formatting and input control.
 *
 * Features:
 * - Supports amount formatting with auto-separator logic
 * - Optional password masking
 * - Custom visual transformation support
 * - Auto focus handling
 * - Trailing icon support
 * - Keyboard action handling (Done action)
 *
 * Special behavior:
 * - Limits numeric input to 12 digits when amount mode is enabled
 * - Automatically formats input using formatAmount()
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param placeholder Placeholder text
 * @param shape Shape of the text field
 * @param textStyle Styling for input text
 * @param keyboardType Keyboard type configuration
 * @param onDoneAction Callback when keyboard "Done" is pressed
 * @param isPassword Enables password masking
 * @param visualTransformation Custom visual transformation
 * @param modifier Modifier for styling
 * @param amount Enables currency formatting behavior
 * @param trailingIcon Optional trailing icon composable
 * @param readOnly If true, disables editing
 */
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
        singleLine = true,
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

/**
 * A row component that displays a label, description, and toggle switch.
 *
 * Features:
 * - Left-aligned label and description
 * - Right-aligned toggle switch
 * - Styled background container
 *
 * @param label Main title text
 * @param description Supporting description text
 * @param isEnabled Current toggle state
 * @param onToggle Callback when toggle state changes
 */
@Composable
fun ReaderToggleRow(
    label: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium, vertical = MaterialTheme.dimens.DP_35_CompactMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = MaterialTheme.dimens.DP_4_CompactMedium)
            )
        }

        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_24_CompactMedium))

        SliderToggle(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

/**
 * Custom animated slider toggle switch component.
 *
 * Features:
 * - Smooth thumb animation
 * - Custom track and thumb styling
 * - Click-to-toggle behavior
 * - No ripple effect for clean UI
 *
 * @param checked Current toggle state
 * @param onCheckedChange Callback when toggle is changed
 */
@Composable
fun SliderToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val trackWidth = MaterialTheme.dimens.DP_50_CompactMedium
    val trackHeight = MaterialTheme.dimens.DP_33_CompactMedium
    val thumbSize = MaterialTheme.dimens.DP_23_CompactMedium
    val thumbPadding = MaterialTheme.dimens.DP_3_CompactMedium

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    val trackColor by remember(checked) {
        derivedStateOf {
            if (checked) Color(0xFFF7931E) else Color(0xFFBDBDBD)
        }
    }

    Box(
        modifier = Modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * A simple labeled input row for configuration settings.
 *
 * Features:
 * - Label displayed above input field
 * - Outlined text field input
 * - Single-line input support
 * - Keyboard type customization
 *
 * @param label Field label text
 * @param value Current input value
 * @param onValueChange Callback when value changes
 * @param keyboardType Keyboard type configuration
 */
@Composable
fun ConfigRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.DP_20_CompactMedium)
    ) {

        Text(
            text = label,
            fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
            fontWeight = FontWeight.Medium
        )

        androidx.compose.material.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
            ),
            singleLine = true
        )
    }
}

/**
 * A circular animated floating menu with expandable action buttons.
 *
 * Features:
 * - Central main action button (print button)
 * - Animated radial menu expansion
 * - Multiple menu options arranged in circular layout
 * - Click animations and state handling
 *
 * Behavior:
 * - Clicking main button toggles menu expansion
 * - Menu items collapse automatically after selection
 * - Supports custom menu actions per item
 *
 * @param menuOptions List of menu option labels
 * @param onMenuOptionClick Callback when a menu option is selected
 * @param onPrintClick Callback for main print button click
 */
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




