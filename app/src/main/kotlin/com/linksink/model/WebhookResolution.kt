package com.linksink.model

sealed class WebhookResolution {
    data class Send(val webhookUrl: String) : WebhookResolution()
    data object LocalOnly : WebhookResolution()
    data object NoWebhookConfigured : WebhookResolution()
}
