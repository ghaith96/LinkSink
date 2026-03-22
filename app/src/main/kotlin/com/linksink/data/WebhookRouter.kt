package com.linksink.data

import com.linksink.model.HookMode
import com.linksink.model.Topic
import com.linksink.model.WebhookResolution

object WebhookRouter {
    fun resolve(
        topic: Topic?,
        globalWebhookUrl: String?
    ): WebhookResolution = when {
        topic == null ->
            globalWebhookUrl?.let { WebhookResolution.Send(it) }
                ?: WebhookResolution.NoWebhookConfigured

        topic.hookMode == HookMode.LOCAL_ONLY ->
            WebhookResolution.LocalOnly

        topic.hookMode == HookMode.USE_GLOBAL ->
            globalWebhookUrl?.let { WebhookResolution.Send(it) }
                ?: WebhookResolution.NoWebhookConfigured

        topic.hookMode == HookMode.CUSTOM ->
            topic.customWebhookUrl?.let { WebhookResolution.Send(it) }
                ?: WebhookResolution.NoWebhookConfigured

        else -> WebhookResolution.NoWebhookConfigured
    }
}
