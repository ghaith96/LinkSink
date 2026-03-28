package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksink.data.SettingsStore
import com.linksink.data.TopicRepository
import com.linksink.data.remote.DiscordWebhookClient
import com.linksink.model.HookMode
import com.linksink.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopicUiState(
    val selectedTopic: Topic? = null,
    val isEditing: Boolean = false,
    val testingWebhook: Boolean = false,
    val testResult: WebhookTestResult? = null,
    val deleteConfirmation: DeleteConfirmation? = null,
    val error: String? = null
)

sealed class WebhookTestResult {
    data object Success : WebhookTestResult()
    data class Failure(val message: String) : WebhookTestResult()
}

data class DeleteConfirmation(
    val topicId: Long,
    val topicName: String,
    val linkCount: Int
)

class TopicViewModel(
    private val topicRepository: TopicRepository,
    private val discordClient: DiscordWebhookClient,
    private val settingsStore: SettingsStore? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicUiState())
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    val topics: StateFlow<List<Topic>> = topicRepository.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTopic(name: String, hookMode: HookMode, customUrl: String?) {
        viewModelScope.launch {
            try {
                val topic = Topic(
                    name = name,
                    hookMode = hookMode,
                    customWebhookUrl = if (hookMode == HookMode.CUSTOM) customUrl else null
                )
                topicRepository.createTopic(topic)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTopic(topic: Topic) {
        viewModelScope.launch {
            try {
                topicRepository.updateTopic(topic)
                _uiState.update { it.copy(selectedTopic = null, isEditing = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun requestDeleteTopic(topicId: Long) {
        viewModelScope.launch {
            val topicWithCount = topicRepository.getTopicWithLinkCount(topicId)
            if (topicWithCount != null) {
                _uiState.update {
                    it.copy(
                        deleteConfirmation = DeleteConfirmation(
                            topicId = topicWithCount.id,
                            topicName = topicWithCount.name,
                            linkCount = topicWithCount.linkCount
                        )
                    )
                }
            }
        }
    }

    fun confirmDeleteTopic(deleteLinks: Boolean) {
        val confirmation = _uiState.value.deleteConfirmation ?: return
        viewModelScope.launch {
            try {
                topicRepository.deleteTopic(confirmation.topicId, deleteLinks)
                settingsStore?.removeSectionState(confirmation.topicId.toString())
                _uiState.update { it.copy(deleteConfirmation = null, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun cancelDeleteTopic() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    fun testWebhook(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(testingWebhook = true, testResult = null) }
            val result = discordClient.testWebhook(url)
            _uiState.update {
                it.copy(
                    testingWebhook = false,
                    testResult = if (result.isSuccess) {
                        WebhookTestResult.Success
                    } else {
                        WebhookTestResult.Failure(result.exceptionOrNull()?.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun selectTopic(topic: Topic?) {
        _uiState.update { it.copy(selectedTopic = topic, isEditing = topic != null) }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
