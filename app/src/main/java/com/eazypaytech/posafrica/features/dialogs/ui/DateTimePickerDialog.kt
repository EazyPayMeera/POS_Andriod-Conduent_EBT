package com.eazypaytech.posafrica.features.dialogs.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Create DatePicker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // When a date is selected, show TimePicker
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    // Handle the time selection
                    val selectedDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                    onDateTimeSelected(selectedDateTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Show the DatePicker dialog
    LaunchedEffect(Unit) {
        datePickerDialog.show()
    }

    // Handle dialog dismissal
    DisposableEffect(Unit) {
        onDispose {
            datePickerDialog.dismiss()
            onDismissRequest.invoke()
        }
    }
}
