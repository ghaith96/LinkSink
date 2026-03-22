package com.linksink.data.remote

import com.linksink.model.Link
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

class DiscordWebhookClient {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    suspend fun sendLink(webhookUrl: String, link: Link): Result<String> {
        return try {
            val payload = buildPayload(link)
            val response = client.post("$webhookUrl?wait=true") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            if (response.status.isSuccess()) {
                val webhookResponse = response.body<DiscordWebhookResponse>()
                Result.success(webhookResponse.id)
            } else {
                Result.failure(
                    DiscordWebhookException(
                        statusCode = response.status.value,
                        message = "Webhook failed: ${response.bodyAsText()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                DiscordWebhookException(
                    statusCode = 0,
                    message = e.message ?: "Unknown error"
                )
            )
        }
    }

    suspend fun testWebhook(webhookUrl: String): Result<Unit> {
        return try {
            val payload = DiscordTestPayload(
                content = "LinkSink connection test successful! You can delete this message."
            )
            val response = client.post(webhookUrl) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(
                    DiscordWebhookException(
                        statusCode = response.status.value,
                        message = "Test failed: ${response.bodyAsText()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                DiscordWebhookException(
                    statusCode = 0,
                    message = e.message ?: "Connection failed"
                )
            )
        }
    }

    private fun buildPayload(link: Link): DiscordWebhookPayload {
        val embed = DiscordEmbed(
            title = link.title ?: link.domain,
            url = link.url,
            description = link.description ?: link.note,
            color = 0x5865F2,
            thumbnail = link.thumbnailUrl?.let { DiscordThumbnail(it) },
            timestamp = DateTimeFormatter.ISO_INSTANT.format(link.savedAt)
        )

        return DiscordWebhookPayload(
            embeds = listOf(embed)
        )
    }

    fun close() {
        client.close()
    }
}

class DiscordWebhookException(
    val statusCode: Int,
    override val message: String
) : Exception(message)
