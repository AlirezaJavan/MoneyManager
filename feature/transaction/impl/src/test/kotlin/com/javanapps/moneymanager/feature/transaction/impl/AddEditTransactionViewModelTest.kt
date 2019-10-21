package com.javanapps.moneymanager.feature.transaction.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.domain.category.GetCategoriesUseCase
import com.javanapps.moneymanager.core.domain.transaction.AddTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.DeleteTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.GetTransactionUseCase
import com.javanapps.moneymanager.core.domain.transaction.UpdateTransactionUseCase
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import com.javanapps.moneymanager.core.testing.repository.TestCategoryRepository
import com.javanapps.moneymanager.core.testing.repository.TestTransactionRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditTransactionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository = TestCategoryRepository()
    private val transactionRepository = TestTransactionRepository()
    private lateinit var viewModel: AddEditTransactionViewModel

    @Before
    fun setup() {
        categoryRepository.setCategories(
            listOf(
                Category(1, "خوراک", TransactionType.EXPENSE),
                Category(2, "درآمد متفرقه", TransactionType.INCOME),
            ),
        )
        viewModel =
            AddEditTransactionViewModel(
                getCategories = GetCategoriesUseCase(categoryRepository),
                getTransaction = GetTransactionUseCase(transactionRepository),
                addTransaction = AddTransactionUseCase(transactionRepository),
                updateTransaction = UpdateTransactionUseCase(transactionRepository),
                deleteTransaction = DeleteTransactionUseCase(transactionRepository),
            )
    }

    @Test
    fun onAmountChange_latinDigits_formatsAsPersian() =
        runTest {
            viewModel.onAmountChange("1500")
            assertThat(viewModel.uiState.value.amountText).isEqualTo("۱٬۵۰۰")
        }

    @Test
    fun onAmountChange_persianDigits_normalizes() =
        runTest {
            viewModel.onAmountChange("۱۵۰۰")
            assertThat(viewModel.uiState.value.amountText).isEqualTo("۱٬۵۰۰")
        }

    @Test
    fun onAmountChange_withSeparator_stripsAndReformats() =
        runTest {
            viewModel.onAmountChange("1٬500")
            assertThat(viewModel.uiState.value.amountText).isEqualTo("۱٬۵۰۰")
        }

    @Test
    fun save_withValidAmount_addsTransaction() =
        runTest {
            viewModel.onAmountChange("2000")
            viewModel.onTitleChange("ناهار")

            viewModel.uiState.test {
                while (!awaitItem().saved) {
                    viewModel.save()
                }
            }

            assertThat(transactionRepository.get(1L)).isNotNull()
            assertThat(transactionRepository.get(1L)?.amountToman).isEqualTo(2000L)
        }

    @Test
    fun save_withEmptyAmount_setsError() =
        runTest {
            viewModel.onAmountChange("")
            viewModel.save()
            assertThat(viewModel.uiState.value.amountError).isTrue()
        }

    @Test
    fun save_withZeroAmount_setsError() =
        runTest {
            viewModel.onAmountChange("0")
            viewModel.save()
            assertThat(viewModel.uiState.value.amountError).isTrue()
        }

    @Test
    fun load_existingTransaction_populatesFields() =
        runTest {
            val tx =
                Transaction(
                    id = 5L,
                    amountToman = 3000L,
                    type = TransactionType.EXPENSE,
                    categoryName = "خوراک",
                    title = "شام",
                    note = "رستوران",
                    date = ShamsiDate(1403, 1, 1),
                    createdAtEpochMillis = 0L,
                    source = TransactionSource.MANUAL,
                )
            transactionRepository.setTransactions(listOf(tx))

            viewModel.load(5L)

            viewModel.uiState.test {
                var state = awaitItem()
                while (state.amountText.isEmpty()) state = awaitItem()
                assertThat(state.amountText).isEqualTo("۳٬۰۰۰")
                assertThat(state.title).isEqualTo("شام")
                assertThat(state.isEditing).isTrue()
            }
        }

    @Test
    fun delete_existingTransaction_removesIt() =
        runTest {
            val tx =
                Transaction(
                    id = 7L,
                    amountToman = 500L,
                    type = TransactionType.EXPENSE,
                    categoryName = "خوراک",
                    title = "",
                    note = "",
                    date = ShamsiDate(1403, 1, 1),
                    createdAtEpochMillis = 0L,
                    source = TransactionSource.MANUAL,
                )
            transactionRepository.setTransactions(listOf(tx))
            viewModel.load(7L)

            viewModel.uiState.test {
                while (!awaitItem().isEditing) { /* wait */ }
            }

            viewModel.delete()

            viewModel.uiState.test {
                while (!awaitItem().saved) { /* wait */ }
            }

            assertThat(transactionRepository.get(7L)).isNull()
        }

    @Test
    fun onTypeChange_resetsCategory() =
        runTest {
            viewModel.onTypeChange(TransactionType.INCOME)
            viewModel.uiState.test {
                assertThat(expectMostRecentItem().type).isEqualTo(TransactionType.INCOME)
            }
        }
}
