package com.analogics.tpaymentsapos.rootUtils.genericComposeUI


import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.ui.theme.dimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun InputTextField(
    inputValue: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeHolder: String = "",
    icon: ImageVector = Icons.Default.Person,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: (KeyboardActionScope.() -> Unit)? = null, // Changed to nullable
    isPasswordField: Boolean = false,
    placeholderColor: Color = Color.Gray,
    onActionDone: (() -> Unit)? = null // Added onActionDone parameter
) {
    var isPasswordVisible by remember { mutableStateOf(!isPasswordField) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = inputValue,
        onValueChange = onChange,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium), // Need to change Here
                tint = Color.Black
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
            focusedBorderColor = colorResource(id = R.color.purple_200),
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = colorResource(id = R.color.purple_200),
            unfocusedLabelColor = Color.LightGray
        )
    )
}





@Composable
fun AppButton(
    onClick: () -> Unit,
    title: String,
    image: Painter? = null // Optional parameter for the image
) {
    Box(
        modifier = Modifier
            .width(MaterialTheme.dimens.DP_248_CompactMedium)
            .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
            .background(
                colorResource(R.color.purple_200),
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
            )
    )
    {
        Button(onClick = onClick,
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = MaterialTheme.dimens.DP_20_CompactMedium),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                containerColor = colorResource(R.color.purple_200)
            ),
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                image?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_28_CompactMedium) // Adjust size as needed
                            .clip(CircleShape) // Make the image round
                            .padding(end = MaterialTheme.dimens.DP_15_CompactMedium) // Space between image and text
                    )
                }
                Text(text = title)
            }
        }
    }
}

@Composable
fun CustomSurface(
    imageResourceId: Int,
    titleText: String,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDoneAction: () -> Unit,
    isPassword: Boolean = false,
    isRefund: Boolean = false,
    isVoid: Boolean = false,
    isAuthcap:Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)? = null // New parameter for custom content
) {

    // Create a FocusRequester instance
    val focusRequester = remember { FocusRequester() }

    // Define height and width based on the isRefund flag
    val surfaceHeight = if (isRefund) MaterialTheme.dimens.DP_380_CompactMedium else if (isVoid || isAuthcap) MaterialTheme.dimens.DP_540_CompactMedium else MaterialTheme.dimens.DP_250_CompactMedium
    val surfaceWidth = MaterialTheme.dimens.DP_410_CompactMedium

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        color = Color.White,
        modifier = modifier
            .padding(MaterialTheme.dimens.DP_24_CompactMedium)
            .fillMaxWidth()
            .height(surfaceHeight)
            .width(surfaceWidth),
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        shadowElevation = MaterialTheme.dimens.DP_20_CompactMedium
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText,
                fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = null,
                modifier = Modifier
                    .size(MaterialTheme.dimens.DP_70_CompactMedium)
                    .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_21_CompactMedium),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDoneAction() }
                ),
                visualTransformation = visualTransformation,
                modifier = modifier
                    .focusRequester(focusRequester)
                    .padding(MaterialTheme.dimens.DP_2_CompactMedium)
                    .width(MaterialTheme.dimens.DP_180_CompactMedium)
                    .height(MaterialTheme.dimens.DP_70_CompactMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFA500), // Orange color for focused state
                    unfocusedBorderColor = Color.LightGray, // Light grey color for unfocused state
                    focusedLabelColor = Color(0xFFFFA500), // Orange color for focused label
                    unfocusedLabelColor = Color.LightGray // Light grey color for unfocused label
                )
            )

            // Custom content
            content?.invoke(this)
        }
    }
}




@Composable
fun CommonTopAppBar(
    title: String,
    onBackButtonClick: () -> Unit,
    backgroundColor: Color = Color(0xFFF8F8F7),
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold, // Make text bold
                style = TextStyle(
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium, // Adjust font size if needed
                    fontWeight = FontWeight.Bold
                )
            )
        },
        backgroundColor = backgroundColor,
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "back_button",
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.dimens.DP_12_CompactMedium)
                    .clickable { onBackButtonClick() }
            )
        },
        modifier = modifier
    )
}
@Composable
fun CommonLayout(
    title: String,
    imageResId: Int? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        CommonTopAppBar(
            title = title,
            onBackButtonClick = { /* Handle back button click */ }
        )

        Surface(
            color = colorResource(id = R.color.purple_200), // Orange color for the outer Surface
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the outer Surface
                .height(MaterialTheme.dimens.DP_540_CompactMedium) // Adjust the height as per your requirement
                .width(MaterialTheme.dimens.DP_410_CompactMedium), // Adjust the width as per your requirement
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium) // Rounded corners for the outer Surface
        ) {
            Box( // Use Box to apply shadow before Surface
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_11_CompactMedium) // Padding for the outer Surface
                    .height(MaterialTheme.dimens.DP_440_CompactMedium) // Adjust the height as per your requirement
                    .width(MaterialTheme.dimens.DP_390_CompactMedium) // Adjust the width as per your requirement
                    .shadow( // Apply shadow using Modifier.shadow
                        elevation = MaterialTheme.dimens.DP_20_CompactMedium, // Elevation height for shadow
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium), // Match shape to Surface
                        clip = false // Do not clip to the shape
                    )
            ) {
                Surface(
                    color = Color.White, // White color for the inner Surface
                    modifier = Modifier.fillMaxSize(), // Fill available space
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium) // Rounded corners for the inner Surface
                ) {
                    Column(
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the content inside the inner Surface
                            .fillMaxSize(), // Fill the entire available space
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start // Align content to the start
                    ) {
                        imageResId?.let {
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = contentDescription,
                                modifier = Modifier
                                    .size(MaterialTheme.dimens.DP_40_CompactMedium)
                                    .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                                    .align(Alignment.End) // Align the image to the end
                            )
                        }
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun OkButton(onClick:()->Unit,
              title:String)
{
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(MaterialTheme.dimens.DP_248_CompactMedium)
            .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))

    )
    {
        Button(modifier = Modifier.wrapContentSize(),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                containerColor = colorResource(R.color.grey)
            ),
            onClick = onClick) {
            Text(
                text = title,
            )
        }
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

@Composable
fun FooterButtons(
    firstButtonTitle: String,
    firstButtonOnClick: () -> Unit,
    secondButtonTitle: String,
    secondButtonOnClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = MaterialTheme.dimens.DP_23_CompactMedium) // Adjust padding as needed // need to change
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_23_CompactMedium), // Adjust vertical padding if needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var isFirstButtonPressed by remember { mutableStateOf(false) }
            var isSecondButtonPressed by remember { mutableStateOf(false) }

            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .width(MaterialTheme.dimens.DP_126_CompactMedium)
                    .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                    .shadow(
                        MaterialTheme.dimens.DP_4_CompactMedium,
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                    )
                    .background(
                        color = colorResource(R.color.white),
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
                        .width(/*MaterialTheme.dimens.DP_130_CompactMedium*/130.dp)
                        .height(MaterialTheme.dimens.DP_48_CompactMedium)
                        .border(
                            width = if (isFirstButtonPressed) MaterialTheme.dimens.DP_2_CompactMedium else 0.dp,
                            color = if (isFirstButtonPressed) colorResource(id = R.color.purple_200) else Color.Transparent,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = colorResource(R.color.grey)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                        pressedElevation = MaterialTheme.dimens.DP_12_CompactMedium,
                        hoveredElevation = MaterialTheme.dimens.DP_10_CompactMedium,
                        focusedElevation = MaterialTheme.dimens.DP_11_CompactMedium
                    )
                ) {
                    Text(
                        text = firstButtonTitle,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LaunchedEffect(isFirstButtonPressed) {
                if (isFirstButtonPressed) {
                    kotlinx.coroutines.delay(100)
                    isFirstButtonPressed = false
                }
            }

            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .width(MaterialTheme.dimens.DP_126_CompactMedium)
                    .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                    .shadow(
                        MaterialTheme.dimens.DP_4_CompactMedium,
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                    )
                    .background(
                        color = colorResource(R.color.white),
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
                            color = if (isSecondButtonPressed) colorResource(id = R.color.purple_200) else Color.Transparent,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium)
                        ),
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_11_CompactMedium),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = colorResource(R.color.grey)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                        pressedElevation = MaterialTheme.dimens.DP_12_CompactMedium,
                        hoveredElevation = MaterialTheme.dimens.DP_10_CompactMedium,
                        focusedElevation = MaterialTheme.dimens.DP_11_CompactMedium
                    )
                ) {
                    Text(
                        text = secondButtonTitle,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium
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
}



object TransactionState {
    var isRefund: Boolean = false
    var isVoid: Boolean = false
    var isPurchase: Boolean = false
    var isPreauth: Boolean = false
    var isTransaction: Boolean = false
    var isAuthcap: Boolean = false
}

object Authorisation {
    var isMerchantReceipt: Boolean = false
    var isEreceipt: Boolean = false
}

@Composable
fun ScannerButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
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
fun PreauthTypeSelectionSurface(
    title: String,
    imageResourceId: Int,
    firstButtonText: String,
    secondButtonText: String,
    onFirstButtonClick: () -> Unit,
    onSecondButtonClick: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .padding(MaterialTheme.dimens.DP_35_CompactMedium)
            .width(MaterialTheme.dimens.DP_430_CompactMedium)
            .height(MaterialTheme.dimens.DP_400_CompactMedium),
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium)
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = null,
                modifier = Modifier
                    .size(MaterialTheme.dimens.DP_70_CompactMedium)
                    .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
            )

            ScannerButton(
                text = firstButtonText,
                onClick = onFirstButtonClick,
                backgroundColor = colorResource(id = R.color.white),
                contentColor = Color.Black,
                modifier = Modifier.padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))

            Text(
                text = stringResource(id = R.string.or),
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_30_CompactMedium)
                    .align(Alignment.CenterHorizontally)
            )

            ScannerButton(
                text = secondButtonText,
                onClick = onSecondButtonClick,
                backgroundColor = Color(0xFFEDEDED),
                contentColor = Color.Black,
                modifier = Modifier.padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
            )
        }
    }
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
            .padding(start = MaterialTheme.dimens.DP_5_CompactMedium,)
            .clickable(onClick = onClick)
            .border(
                width = MaterialTheme.dimens.DP_2_CompactMedium, // Adjust the border width as needed
                color = if (isSelected) colorResource(id = R.color.purple_200) else Color.Transparent, // Orange color if selected, otherwise transparent
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_20_CompactMedium)
            ),
        backgroundColor = colorResource(id = R.color.white),
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
            ImageView(imageId = imageResId,
                modifier = Modifier
                    .size(MaterialTheme.dimens.DP_40_CompactMedium) // Adjust the size as needed
                    .align(Alignment.CenterHorizontally) // Center the image
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium))
            TextView(text = text, fontSize = MaterialTheme.dimens.SP_18_CompactMedium)
        }
    }
}


@Composable
fun GifImage(
    modifier: Modifier = Modifier,
    gifResId: Int
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = gifResId).apply {
                size(Size.ORIGINAL)
            }.build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = modifier.fillMaxWidth(),
    )
}


@Composable
fun AppHeader(
    title: String,
    onBackButtonClick: () -> Unit,
    backgroundColor: Color = Color(0xFFF8F8F7),
    icon1: Int? = null,
    icon2: Int? = null,
    onIcon1Click: (() -> Unit)? = null,
    onIcon2Click: (() -> Unit)? = null,
    isIcon1Visible: Boolean = true,
    isIcon2Visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.Black, // Ensure text color contrasts with the background
                style = TextStyle(
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium, // Ensure font size is large enough
                    fontWeight = FontWeight.Bold
                )
            )
        },
        backgroundColor = backgroundColor,
        navigationIcon = {
            if (isIcon1Visible) {
                if (icon1 != null) {
                    Image(
                        painter = painterResource(id = icon1),
                        contentDescription = "icon1",
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.dimens.DP_12_CompactMedium)
                            .clickable { onIcon1Click?.invoke() }
                    )
                }
            }
        },
        actions = {
            if (isIcon2Visible && icon2 != null) {
                Image(
                    painter = painterResource(id = icon2),
                    contentDescription = "icon2",
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.dimens.DP_12_CompactMedium)
                        .clickable { onIcon2Click?.invoke() }
                )
            }
        },
        modifier = modifier
    )
}




// Added this function to add Bold Top text for UI In logout screen Amount Screen
@Composable
fun TopBoldText(
    text: String,
    fontSize: TextUnit = MaterialTheme.dimens.SP_27_CompactMedium // Default size if not provided
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally within the Column
        modifier = Modifier
            .fillMaxWidth() // Ensures the Column takes the full width
            .padding(top = MaterialTheme.dimens.DP_21_CompactMedium)
    ) {
        Text(
            text = text,
            fontSize = fontSize, // Use the provided fontSize
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(MaterialTheme.dimens.DP_21_CompactMedium)
        )
    }
}


// Added function for Header Image
@Composable
fun HeaderImage(
    imageName: String
) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally within the Column
        modifier = Modifier
            .fillMaxWidth() // Ensures the Column takes the full width
            .padding(top = MaterialTheme.dimens.DP_21_CompactMedium)
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null, // Decorative image
            modifier = Modifier
                .size(MaterialTheme.dimens.DP_50_CompactMedium) // Default size

        )
    }
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
                Color(0xFFFC7519),
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_24_CompactMedium)
            )
    ) {
        Card(
            elevation =  MaterialTheme.dimens.DP_5_CompactMedium,
            backgroundColor= Color.White,
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
fun SmallSurface(
    modifier: Modifier = Modifier,
    isRefund: Boolean = false,
    isVoid: Boolean = false,
    isAuthcap:Boolean = false,
    content: (@Composable (ColumnScope.() -> Unit))? = null
) {
    val surfaceHeight = if (isRefund) MaterialTheme.dimens.DP_350_CompactMedium else if (isVoid || isAuthcap) MaterialTheme.dimens.DP_440_CompactMedium else MaterialTheme.dimens.DP_250_CompactMedium
    val surfaceWidth = MaterialTheme.dimens.DP_410_CompactMedium

    Surface(
        color = Color.White,
        modifier = modifier
            .padding(MaterialTheme.dimens.DP_24_CompactMedium)
            .fillMaxWidth()
            .height(surfaceHeight)
            .width(surfaceWidth),
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        shadowElevation = MaterialTheme.dimens.DP_20_CompactMedium
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom content, if available
            content?.invoke(this)
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
    contentDescription: String = "group 360",
    contentScale: ContentScale = ContentScale.Crop,
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

@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_28_CompactMedium),
    keyboardType: KeyboardType = KeyboardType.Text,
    onDoneAction: () -> Unit = {},
    isPassword: Boolean = false, // New parameter to indicate if it's a password field
    visualTransformation: VisualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    modifier: Modifier = Modifier,
    amount: Boolean = false // New parameter to indicate if ₹ icon should be shown
) {
    // Create a FocusRequester instance
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle value change with length restriction if `amount` flag is true
    val handleValueChange: (String) -> Unit = { newValue ->
        if (amount && newValue.length > 11) {
            onValueChange(newValue.take(11)) // Restrict input to 12 characters
        } else {
            onValueChange(newValue)
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = handleValueChange,
        label = { Text("") }, // Label is always empty
        placeholder = { Text(placeholder, fontSize = MaterialTheme.dimens.SP_28_CompactMedium) },
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDoneAction() }
        ),
        visualTransformation = visualTransformation, // Use passed visualTransformation
        modifier = modifier
            .focusRequester(focusRequester)
            .padding(MaterialTheme.dimens.DP_2_CompactMedium)
            .width(MaterialTheme.dimens.DP_280_CompactMedium)
            .height(MaterialTheme.dimens.DP_70_CompactMedium),
        leadingIcon = if (amount) {
            {
                Text(
                    text = "\u20B9", // Unicode for ₹ symbol
                    fontSize = MaterialTheme.dimens.SP_29_CompactMedium, // Adjust the size of the icon
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // Set the color to black
                )
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(id = R.color.purple_200), // Orange color for focused state
            unfocusedBorderColor = Color.LightGray, // Light grey color for unfocused state
            focusedLabelColor = colorResource(id = R.color.purple_200), // Orange color for focused label
            unfocusedLabelColor = Color.LightGray // Light grey color for unfocused label
        )
    )
}




@Composable
@Preview
fun abc()
{

}
