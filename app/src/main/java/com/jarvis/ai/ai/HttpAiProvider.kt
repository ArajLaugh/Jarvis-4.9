package com.jarvis.ai.ai

import com.jarvis.ai.AiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpAiProvider(
    private val cfg: AiConfig
) : AiProvider {

    override suspend fun chat(
        history: List<Pair<String, String>>
    ): AiReply = withContext(Dispatchers.IO) {

        require(cfg.url.startsWith("https://")) {
            "AI endpoint must be https://"
        }

        val endpoint = cfg.url.trimEnd('/') + "/chat/completions"

        val messages = JSONArray()

        history.forEach { (role, content) ->
            messages.put(
                JSONObject()
                    .put("role", role)
                    .put("content", content)
            )
        }

        val body = JSONObject()
            .put("model", cfg.model)
            .put("messages", messages)
            .put("temperature", cfg.temp)

        val connection =
            URL(endpoint).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 30000
            connection.readTimeout = 120000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "Authorization",
                "Bearer ${cfg.token}"
            )

            connection.outputStream.use { output ->
                output.write(
                    body.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val status = connection.responseCode

            val stream =
                if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response =
                stream?.use {
                    BufferedReader(
                        InputStreamReader(it)
                    ).readText()
                } ?: ""

            if (status !in 200..299) {
                throw Exception(
                    "AI HTTP $status: ${response.take(500)}"
                )
            }

            val json = JSONObject(response)

            val reply =
                json.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    ?: ""

            if (reply.isBlank()) {
                throw Exception("AI returned an empty response")
            }

            AiReply(text = reply)

        } finally {
            connection.disconnect()
        }
    }
}