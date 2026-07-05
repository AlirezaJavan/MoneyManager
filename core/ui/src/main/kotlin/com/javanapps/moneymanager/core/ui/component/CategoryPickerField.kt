package com.javanapps.moneymanager.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.ui.R

/**
 * A category picker that doubles as a search field: typing filters the dropdown, and if no
 * existing category matches the typed name exactly, an "add" row lets the user create it inline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerField(
    categories: List<Category>,
    selectedName: String?,
    onSelect: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onEditCategory: ((Category) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selectedName) { mutableStateOf(selectedName.orEmpty()) }

    val filtered =
        remember(categories, query) {
            if (query.isBlank()) categories else categories.filter { it.name.contains(query, ignoreCase = true) }
        }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { open ->
            expanded = open
            // Opening: clear the field so the user can type from scratch and sees every category.
            // Closing without picking anything: fall back to whatever was selected before.
            query = if (open) "" else selectedName.orEmpty()
        },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                query = selectedName.orEmpty()
            },
        ) {
            // Always first, in every state, so "add a category" is never buried in the list.
            DropdownMenuItem(
                text = {
                    Text(
                        if (query.isBlank()) {
                            stringResource(R.string.core_ui_category_picker_add_new)
                        } else {
                            stringResource(R.string.core_ui_category_picker_add, query.trim())
                        },
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                enabled = query.isNotBlank(),
                onClick = {
                    onAddCategory(query.trim())
                    expanded = false
                },
            )
            filtered.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelect(category.name)
                        query = category.name
                        expanded = false
                    },
                    trailingIcon =
                        onEditCategory?.let { editCategory ->
                            {
                                IconButton(onClick = { editCategory(category) }) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.core_ui_category_picker_edit_desc),
                                    )
                                }
                            }
                        },
                )
            }
        }
    }
}
