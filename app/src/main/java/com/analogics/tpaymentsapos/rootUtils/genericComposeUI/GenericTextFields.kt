package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.analogics.tpaymentsapos.R


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
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        modifier = modifier
            .padding(25.dp)
            .fillMaxWidth()
            .height(250.dp)
            .width(430.dp),
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDoneAction() }
                ),
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth()
            )
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



