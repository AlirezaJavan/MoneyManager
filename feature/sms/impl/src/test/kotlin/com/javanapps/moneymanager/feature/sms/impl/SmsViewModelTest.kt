package com.javanapps.moneymanager.feature.sms.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.data.sms.SmsHeuristicParser
import com.javanapps.moneymanager.core.model.BankSmsRule
import com.javanapps.moneymanager.core.testing.repository.TestBankSmsRuleRepository
import com.javanapps.moneymanager.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SmsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ruleRepository = TestBankSmsRuleRepository()
    private val parser = SmsHeuristicParser()
    private lateinit var viewModel: SmsViewModel

    @Before
    fun setup() {
        viewModel =
            SmsViewModel(
                ruleRepository = ruleRepository,
                parser = parser,
            )
    }

    private fun bankSmsRule(
        id: Long,
        bankName: String,
        senderPattern: String,
        enabled: Boolean = true,
    ) = BankSmsRule(
        id = id,
        bankName = bankName,
        senderPattern = senderPattern,
        incomeKeywords = emptyList(),
        expenseKeywords = emptyList(),
        amountInRial = false,
        defaultCategory = "",
        sampleBody = "",
        enabled = enabled,
    )

    @Test
    fun uiState_reflectsRulesFromRepository() =
        runTest {
            val rule = bankSmsRule(id = 1L, bankName = "ملت", senderPattern = "ملت")
            ruleRepository.setRules(listOf(rule))

            viewModel.uiState.test {
                var state = awaitItem()
                while (state.rules.isEmpty()) state = awaitItem()
                assertThat(state.rules).hasSize(1)
                assertThat(state.rules[0].bankName).isEqualTo("ملت")
            }
        }

    @Test
    fun setTeachBody_updatesState() =
        runTest {
            viewModel.setTeachBody("اعتبار ۱۰۰۰ تومان")
            assertThat(viewModel.uiState.value.teachSampleBody).isEqualTo("اعتبار ۱۰۰۰ تومان")
        }

    @Test
    fun setTeachSender_updatesState() =
        runTest {
            viewModel.setTeachSender("Melli")
            assertThat(viewModel.uiState.value.teachSender).isEqualTo("Melli")
        }

    @Test
    fun resetTeach_clearsTeachFields() =
        runTest {
            viewModel.setTeachBody("some body")
            viewModel.setTeachSender("some sender")
            viewModel.setTeachBankName("some bank")

            viewModel.resetTeach()

            val state = viewModel.uiState.value
            assertThat(state.teachSampleBody).isEmpty()
            assertThat(state.teachSender).isEmpty()
            assertThat(state.teachBankName).isEmpty()
            assertThat(state.teachResult).isEqualTo(TeachResult.Idle)
        }

    @Test
    fun deleteRule_removesFromRepository() =
        runTest {
            val rule = bankSmsRule(id = 3L, bankName = "صادرات", senderPattern = "saderat")
            ruleRepository.setRules(listOf(rule))

            viewModel.deleteRule(3L)

            viewModel.uiState.test {
                var state = awaitItem()
                while (state.rules.isNotEmpty()) state = awaitItem()
                assertThat(state.rules).isEmpty()
            }
        }

    @Test
    fun toggleRule_flipsEnabledState() =
        runTest {
            val rule = bankSmsRule(id = 2L, bankName = "ملی", senderPattern = "melli", enabled = true)
            ruleRepository.setRules(listOf(rule))

            viewModel.toggleRule(rule)

            viewModel.uiState.test {
                var state = awaitItem()
                while (state.rules.isEmpty() || state.rules[0].enabled) state = awaitItem()
                assertThat(state.rules[0].enabled).isFalse()
            }
        }
}
