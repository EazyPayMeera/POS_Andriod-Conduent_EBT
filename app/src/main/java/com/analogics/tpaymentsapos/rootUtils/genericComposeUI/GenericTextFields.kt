package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import OrangeColor
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.analogics.tpaymentsapos.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Composable

fun InputTextField(
    inputValue: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Username",
    placeHolder: String = "Username",
    icon: ImageVector = Icons.Default.Person,
    keyboardType: KeyboardType
)
{
    val focusManager = LocalFocusManager.current
    val leadingIcon = @Composable {
        Icon(
            Icons.Default.Person,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    OutlinedTextField(
        value = inputValue,
         onValueChange = onChange,
        modifier = modifier,
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        placeholder = { Text(placeHolder) },
        label = { Text(label) },
        singleLine = true,
        visualTransformation = VisualTransformation.None
    )
}


@Composable
fun AppButton(onClick:()->Unit,
              title:String)
{
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(248.dp)
            .padding( bottom = 20.dp)
            .background(colorResource(R.color.purple_200),shape = RoundedCornerShape(10.dp))

    )
    {
        Button(modifier = Modifier.wrapContentSize(),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                containerColor = colorResource(R.color.purple_200)
            ),
            onClick = onClick) {
            Text(
                text = title,
            )
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
    // Define height and width based on the isRefund flag
    val surfaceHeight = if (isRefund) 380.dp else if (isVoid || isAuthcap) 540.dp else 250.dp
    val surfaceWidth = if (isRefund) 430.dp else if (isVoid || isAuthcap) 430.dp else 430.dp

    Surface(
        color = Color.White,
        modifier = modifier
            .padding(25.dp)
            .fillMaxWidth()
            .height(surfaceHeight)
            .width(surfaceWidth),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDoneAction() }
                ),
                visualTransformation = visualTransformation,
                modifier = modifier
                    .padding(2.dp)
                    .width(280.dp)
                    .height(70.dp)
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
                    fontSize = 20.sp, // Adjust font size if needed
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
                    .padding(horizontal = 12.dp)
                    .clickable { onBackButtonClick() }
            )
        },
        modifier = modifier
    )
}


@Composable
fun CommonLayout(
    title: String,
    imageResId: Int,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        CommonTopAppBar(
            title = title,
            onBackButtonClick = { /* Handle back button click */ }
        )

        Surface(
            color = Color(0xFFF7931E), // Orange color for the outer Surface
            modifier = Modifier
                .padding(25.dp) // Padding for the outer Surface
                .height(540.dp) // Adjust the height as per your requirement
                .width(430.dp), // Adjust the width as per your requirement
            shape = RoundedCornerShape(18.dp) // Rounded corners for the outer Surface
        ) {
            Box( // Use Box to apply shadow before Surface
                modifier = Modifier
                    .padding(10.dp) // Padding for the outer Surface
                    .height(440.dp) // Adjust the height as per your requirement
                    .width(390.dp) // Adjust the width as per your requirement
                    .shadow( // Apply shadow using Modifier.shadow
                        elevation = 8.dp, // Elevation height for shadow
                        shape = RoundedCornerShape(16.dp), // Match shape to Surface
                        clip = false // Do not clip to the shape
                    )
            ) {
                Surface(
                    color = Color.White, // White color for the inner Surface
                    modifier = Modifier.fillMaxSize(), // Fill available space
                    shape = RoundedCornerShape(16.dp) // Rounded corners for the inner Surface
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp) // Padding for the content inside the inner Surface
                            .fillMaxSize(), // Fill the entire available space
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start // Align content to the start
                    ) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = contentDescription,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(bottom = 16.dp)
                                .align(Alignment.End) // Align the image to the end
                        )
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
            .width(248.dp)
            .padding( bottom = 20.dp)
            .background(colorResource(R.color.grey),shape = RoundedCornerShape(10.dp))

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
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
    height: Dp, // Added customizable height parameter
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .height(height) // Applying customizable height
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
    bottomStartRadius: Dp = 16.dp,
    bottomEndRadius: Dp = 16.dp,
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
fun ConfirmationButton(
    onClick: () -> Unit,
    title: String
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(126.dp) // Set the width of the Box
            .padding(bottom = 20.dp) // Bottom padding
            .shadow(4.dp, shape = RoundedCornerShape(10.dp)) // Add shadow with rounded corners
            .background(
                color = colorResource(R.color.white),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth() // Make the button fill the width of the Box
                .height(48.dp), // Set a fixed height for the button
            shape = RoundedCornerShape(10.dp), // Rounded corners for the button
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black, // Text color
                containerColor = colorResource(R.color.grey) // Background color
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp, // Default elevation
                pressedElevation = 12.dp, // Elevation when the button is pressed
                hoveredElevation = 6.dp, // Elevation when the button is hovered
                focusedElevation = 10.dp // Elevation when the button is focused
            )
        ) {
            Text(
                text = title,
                color = Color.Black, // Text color
                style = MaterialTheme.typography.bodyMedium // Use a common typography style
            )
        }
    }
}


object TransactionState {
    var isRefund: Boolean = false
    var isVoid: Boolean = false
    var isPurchase: Boolean = false
    var isPreauth: Boolean = false
    var isTransaction: Boolean = false
}

object Authorisation {
    var isNewauth: Boolean = false
    var isAuthcap: Boolean = false
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
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .padding(2.dp)
            .width(280.dp)
            .height(70.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
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
            .padding(25.dp)
            .width(430.dp)
            .height(400.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .padding(bottom = 16.dp)
            )

            ScannerButton(
                text = firstButtonText,
                onClick = onFirstButtonClick,
                backgroundColor = Color(0xFFEDEDED),
                contentColor = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "---------------or----------------",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .align(Alignment.CenterHorizontally)
            )

            ScannerButton(
                text = secondButtonText,
                onClick = onSecondButtonClick,
                backgroundColor = Color(0xFFEDEDED),
                contentColor = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
fun IconButtonWithText(
    text: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material.Button(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .padding(8.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) OrangeColor else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        colors = androidx.compose.material.ButtonDefaults.buttonColors(
            backgroundColor = Color.White // Use backgroundColor parameter for compatibility
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            androidx.compose.material.Icon(
                painter = icon,
                contentDescription = text,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 4.dp),
                tint = Color.Black
            )
            androidx.compose.material.Text(text = text, color = Color.Black, fontSize = 14.sp)
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
fun MenuTopAppBar(
    title: String,
    onMenuItemClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(text = title, color = Color.Black)
        },
        navigationIcon = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onMenuItemClick("Settings")
                }) {
                    Text("Settings")
                }
                DropdownMenuItem(onClick = {
                    expanded = false
                    onMenuItemClick("Option 2")
                }) {
                    Text("Option 2")
                }
            }
        },
        backgroundColor = Color.White
    )
}








