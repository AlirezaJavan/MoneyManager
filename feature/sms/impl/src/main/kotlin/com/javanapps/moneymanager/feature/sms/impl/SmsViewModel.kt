package com.javanapps.moneymanager.feature.sms.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.moneymanager.core.data.repository.BankSmsRuleRepository
import com.javanapps.moneymanager.core.data.sms.SmsHeuristicParser
import com.javanapps.moneymanager.core.model.BankSmsRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsViewModel
    @Inject
    constructor(
        private val ruleRepository: BankSmsRuleRepository,
        private val parser: SmsHeuristicParser,
    ) : ViewModel() {
        private val teachState =
            MutableStateFlow(
                SmsUiState().copy(teachResult = TeachResult.Idle),
            )

        val uiState: StateFlow<SmsUiState> =
            combine(
                ruleRepository.observeAll(),
                teachState,
            ) { rules, teach ->
                teach.copy(rules = rules)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SmsUiState())

        fun setTeachBody(body: String) {
            teachState.update { it.copy(teachSampleBody = body, teachResult = TeachResult.Idle) }
        }

        fun setTeachSender(sender: String) {
            teachState.update { it.copy(teachSender = sender, teachResult = TeachResult.Idle) }
        }

        fun setTeachBankName(name: String) {
            teachState.update { it.copy(teachBankName = name, teachResult = TeachResult.Idle) }
        }

        fun previewLearnedRule() {
            val state = teachState.value
            val rule =
                parser.learnFromSample(
                    state.teachSampleBody,
                    state.teachSender,
                    state.teachBankName.ifBlank { state.teachSender },
                )
            teachState.update {
                it.copy(
                    teachResult =
                        if (rule != null) {
                            TeachResult.Preview(rule.copy(id = state.editingRuleId ?: BankSmsRule.NO_ID))
                        } else {
                            TeachResult.NoMatch
                        },
                )
            }
        }

        fun saveLearnedRule() {
            val preview = (teachState.value.teachResult as? TeachResult.Preview) ?: return
            viewModelScope.launch {
                ruleRepository.upsert(preview.rule)
                teachState.update {
                    it.copy(
                        teachSampleBody = "",
                        teachSender = "",
                        teachBankName = "",
                        teachResult = TeachResult.Saved,
                        editingRuleId = null,
                    )
                }
            }
        }

        fun deleteRule(id: Long) {
            viewModelScope.launch { ruleRepository.delete(id) }
        }

        fun toggleRule(rule: BankSmsRule) {
            viewModelScope.launch { ruleRepository.upsert(rule.copy(enabled = !rule.enabled)) }
        }

        fun startEditRule(rule: BankSmsRule) {
            teachState.update {
                it.copy(
                    teachSampleBody = rule.sampleBody,
                    teachSender = rule.senderPattern,
                    teachBankName = rule.bankName,
                    editingRuleId = rule.id,
                    teachResult = TeachResult.Idle,
                )
            }
        }

        fun resetTeach() {
            teachState.update {
                it.copy(
                    teachSampleBody = "",
                    teachSender = "",
                    teachBankName = "",
                    teachResult = TeachResult.Idle,
                    editingRuleId = null,
                )
            }
        }
    }
