package com.javanapps.moneymanager.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.ui.R
import com.javanapps.moneymanager.core.ui.format.PersianNumber

/**
 * A fully Persian/Shamsi date picker dialog. Year and month use steppers; the day is chosen from a
 * grid that always respects the selected month's real length (leap-year aware), so an out-of-range
 * day can never be produced.
 */
@Composable
fun ShamsiDatePickerDialog(
    initialDate: ShamsiDate,
    onConfirm: (ShamsiDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var year by remember { mutableStateOf(initialDate.year) }
    var month by remember { mutableStateOf(initialDate.month) }
    var day by remember { mutableStateOf(initialDate.day) }

    val maxDay = ShamsiCalendar.monthLength(year, month)
    if (day > maxDay) day = maxDay

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(ShamsiDate(year, month, day, initialDate.hour, initialDate.minute)) }) {
                Text(stringResource(R.string.core_ui_date_picker_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.core_ui_date_picker_cancel)) } },
        title = { Text(stringResource(R.string.core_ui_date_picker_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Stepper(
                    label = PersianNumber.toPersianDigits(year.toLong()),
                    onPrevious = { year-- },
                    onNext = { year++ },
                )
                Stepper(
                    label = ShamsiCalendar.monthName(month),
                    onPrevious = { month = if (month == 1) 12 else month - 1 },
                    onNext = { month = if (month == 12) 1 else month + 1 },
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (d in 1..maxDay) {
                        FilterChip(
                            selected = d == day,
                            onClick = { day = d },
                            label = { Text(PersianNumber.toPersianDigits(d.toLong())) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun Stepper(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.core_ui_date_stepper_previous))
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.core_ui_date_stepper_next))
        }
    }
}
