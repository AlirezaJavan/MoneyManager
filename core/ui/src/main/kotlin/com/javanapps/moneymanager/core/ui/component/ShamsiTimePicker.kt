package com.javanapps.moneymanager.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.javanapps.moneymanager.core.ui.R
import com.javanapps.moneymanager.core.ui.format.PersianNumber

/** A simple Persian time picker (hour & minute steppers, 24-hour). */
@Composable
fun ShamsiTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var hour by remember { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableStateOf(initialMinute.coerceIn(0, 59)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(hour, minute) },
            ) { Text(stringResource(R.string.core_ui_time_picker_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.core_ui_time_picker_cancel)) } },
        title = { Text(stringResource(R.string.core_ui_time_picker_title)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TwoDigitStepper(value = hour, onChange = { hour = (it + 24) % 24 })
                Text(" : ", style = MaterialTheme.typography.headlineMedium)
                TwoDigitStepper(value = minute, onChange = { minute = (it + 60) % 60 })
            }
        },
    )
}

@Composable
private fun TwoDigitStepper(
    value: Int,
    onChange: (Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onChange(value + 1) }) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = stringResource(R.string.core_ui_time_stepper_increase))
        }
        Text(
            text = PersianNumber.toPersianDigits(value.toString().padStart(2, '0')),
            style = MaterialTheme.typography.headlineMedium,
        )
        IconButton(onClick = { onChange(value - 1) }) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.core_ui_time_stepper_decrease))
        }
    }
}
