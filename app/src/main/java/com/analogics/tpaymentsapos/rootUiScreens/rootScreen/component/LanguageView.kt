import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar

@Composable
fun LanguageView(navHostController: NavHostController) {
    // State to manage the selected language
    var selectedLanguage by remember { mutableStateOf("Hindi") }

    Column {
        CommonTopAppBar(
            title = "Set Language",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Surface with rounded corners, centered content, and shadow
        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(25.dp) // Padding around the surface
                .fillMaxWidth() // Fills the available width
                .height(250.dp) // Set a fixed height
                .clip(RoundedCornerShape(18.dp)), // Apply rounded corners with a radius of 18.dp
            shape = RoundedCornerShape(18.dp), // Ensure shape is consistent with clip
            elevation = 8.dp // Adds shadow effect with specified elevation
        ) {
            // Column for top-centered text
            Column(
                modifier = Modifier
                    .fillMaxSize() // Fills the available space
                    .padding(top = 16.dp), // Padding at the top for spacing
                verticalArrangement = Arrangement.Top, // Aligns children at the top
                horizontalAlignment = Alignment.CenterHorizontally // Centers children horizontally
            ) {
                // Centered text at the top
                Text(
                    text = "Select Your Language:",
                    style = MaterialTheme.typography.body1.copy(
                        fontSize = 18.sp // Increase font size if needed
                    ),
                    color = Color.Black
                )

                // Image at the center
                Image(
                    painter = painterResource(id = R.drawable.language), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(55.dp)
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally) // Center the image
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Row for "Hindi" with RadioButton
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Hindi",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 18.sp
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(190.dp))
                    RadioButton(
                        selected = selectedLanguage == "Hindi",
                        onClick = { selectedLanguage = "Hindi" }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Divider after "Hindi"
                Divider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Row for "English" with RadioButton
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    Text(
                        text = "English",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 18.sp
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(170.dp))

                    RadioButton(
                        selected = selectedLanguage == "English",
                        onClick = { selectedLanguage = "English" }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Divider after "English"
                Divider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}
