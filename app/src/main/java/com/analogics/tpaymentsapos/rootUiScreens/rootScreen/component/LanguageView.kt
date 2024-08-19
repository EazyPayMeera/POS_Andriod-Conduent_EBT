import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.HeaderImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TopBoldText
import com.analogics.tpaymentsapos.ui.theme.dashboardOrangeColor
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun LanguageView(navHostController: NavHostController) {
    // State to manage the selected language
    var selectedLanguage by remember { mutableStateOf("Hindi") }


    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.set_lang),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Surface with rounded corners, centered content, and shadow
        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium) // Padding around the surface
                .fillMaxWidth() // Fills the available width
                .height(androidx.compose.material3.MaterialTheme.dimens.DP_300_CompactMedium) // Set a fixed height
                .clip(RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_18_CompactMedium)), // Apply rounded corners with a radius of 18.dp
            shape = RoundedCornerShape(androidx.compose.material3.MaterialTheme.dimens.DP_18_CompactMedium), // Ensure shape is consistent with clip
            elevation = androidx.compose.material3.MaterialTheme.dimens.DP_20_CompactMedium // Adds shadow effect with specified elevation
        ) {
            // Column for top-centered text
            Column(
                modifier = Modifier
                    .fillMaxSize() // Fills the available space
                    .padding(top = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium), // Padding at the top for spacing
                verticalArrangement = Arrangement.Top, // Aligns children at the top
                horizontalAlignment = Alignment.CenterHorizontally // Centers children horizontally
            ) {
                TopBoldText(
                    text = stringResource(id = R.string.select_lang),
                    fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_19_CompactMedium // Custom font size
                )

                HeaderImage(
                    imageName = "language" // Name of the drawable resource (without the file extension)
                )
                //Spacer(modifier = Modifier.height(20.dp))

                // Row for "Hindi" with RadioButton
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                ) {
                    Text(
                        text = stringResource(id = R.string.hindi),
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_21_CompactMedium
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_190_CompactMedium))
                    RadioButton(
                        selected = selectedLanguage == stringResource(id = R.string.hindi),
                        onClick = { selectedLanguage = "Hindi" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorResource(id = R.color.Orange),
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium))
                // Divider after "Hindi"
                Divider(
                    color = Color.Black,
                    thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                )
                Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium))

                // Row for "English" with RadioButton
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                ) {

                    Text(
                        text = stringResource(id = R.string.english),
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = androidx.compose.material3.MaterialTheme.dimens.SP_21_CompactMedium
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(androidx.compose.material3.MaterialTheme.dimens.DP_170_CompactMedium))

                    RadioButton(
                        selected = selectedLanguage == stringResource(id = R.string.english),
                        onClick = { selectedLanguage = "English" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = dashboardOrangeColor,
                            unselectedColor = Color.Gray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(androidx.compose.material3.MaterialTheme.dimens.DP_11_CompactMedium))
                // Divider after "English"
                Divider(
                    color = Color.Black,
                    thickness = androidx.compose.material3.MaterialTheme.dimens.DP_1_CompactMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = androidx.compose.material3.MaterialTheme.dimens.DP_24_CompactMedium)
                )
            }
        }
    }
}
