package com.eazypaytech.posafrica.core.themes

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.eazypaytech.posafrica.features.activity.ui.MainActivity
import com.eazypaytech.posafrica.core.themes.colors.ApprovedGreenBright
import com.eazypaytech.posafrica.core.themes.colors.Black
import com.eazypaytech.posafrica.core.themes.colors.Grey
import com.eazypaytech.posafrica.core.themes.colors.LightBoarder
import com.eazypaytech.posafrica.core.themes.colors.OnPrimary
import com.eazypaytech.posafrica.core.themes.colors.OrangeThemeColor
import com.eazypaytech.posafrica.core.themes.colors.Pink80
import com.eazypaytech.posafrica.core.themes.colors.PrimaryContainer
import com.eazypaytech.posafrica.core.themes.colors.PurpleGrey80
import com.eazypaytech.posafrica.core.themes.colors.White
import com.eazypaytech.posafrica.core.themes.colors.error
import com.eazypaytech.posafrica.core.themes.dimens.CompactDimens
import com.eazypaytech.posafrica.core.themes.dimens.CompactMediumDimens
import com.eazypaytech.posafrica.core.themes.dimens.CompactSmallDimens
import com.eazypaytech.posafrica.core.themes.dimens.ExpandedDimens
import com.eazypaytech.posafrica.core.themes.dimens.LocalAppDimens
import com.eazypaytech.posafrica.core.themes.dimens.MediumDimens
import com.eazypaytech.posafrica.core.themes.dimens.TPDimensProvider
import com.eazypaytech.posafrica.core.themes.fonts.CompactMediumTypography
import com.eazypaytech.posafrica.core.themes.fonts.CompactSmallTypography
import com.eazypaytech.posafrica.core.themes.fonts.CompactTypography
import com.eazypaytech.posafrica.core.themes.fonts.ExpandedTypography
import com.eazypaytech.posafrica.core.themes.fonts.MediumTypography

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeThemeColor,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = White,
    onSecondary = Grey,
    tertiary = Black,
    onTertiary = LightBoarder,
    error = error,
    outline = ApprovedGreenBright

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = MaterialTheme.colorScheme.onPrimary,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun TPaymentsAPOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val window = calculateWindowSizeClass(activity = LocalContext.current as MainActivity)
    val config = LocalConfiguration.current

    var typography = CompactTypography
    var appDimens = CompactDimens

    when (window.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            if (config.screenWidthDp <= 360) {
                appDimens = CompactSmallDimens
                typography = CompactSmallTypography
            } else if (config.screenWidthDp < 599) {
                appDimens = CompactMediumDimens
                typography = CompactMediumTypography
            } else {
                appDimens = CompactDimens
                typography = CompactTypography
            }
        }

        WindowWidthSizeClass.Medium -> {
            appDimens = MediumDimens
            typography = MediumTypography
        }

        WindowWidthSizeClass.Expanded -> {
            appDimens = ExpandedDimens
            typography = ExpandedTypography
        }

        else -> {
            appDimens = ExpandedDimens
            typography = ExpandedTypography
        }
    }
    TPDimensProvider(appDimens = appDimens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
val MaterialTheme.dimens
    @Composable
    get() = LocalAppDimens.current


