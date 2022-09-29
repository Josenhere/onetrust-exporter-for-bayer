package objectsAndInterfaces

import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.BearerResponse
import dataClasses.Creds
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import java.io.File
import java.time.Instant

object Oauth : BasicClass("ApiManagerLogger") {

    private var token = ""
    private var expiresIn = 0
    private var latestCheck = 0L
    private val creds: Creds = mapper.readValue(File("settings/access_onetrust/OneTrustCredentials.json").readText())

    private fun isTokenExpired(): Boolean {
        val timeNow = Instant.now().epochSecond
        return (timeNow - latestCheck) > expiresIn - 120
    }
    private suspend fun generateNewToken(): String {

        var parsedResponse: BearerResponse
        var responseJson: String

        while (true) {

            val client = HttpClient {
                install(HttpTimeout) {
                    requestTimeoutMillis = 120000
                    socketTimeoutMillis = 120000
                }
            }

            try {
                log.info("Retrieve new bearer token from OneTrust")
                responseJson = client.post {

                    url("https://${Settings.getEnvironment()}/api/access/v1/oauth/token")

                    @Suppress("SpellCheckingInspection")
                    body = FormDataContent(Parameters.build {
                        append("grant_type", "client_credentials")
                        append("client_id", creds.clientId)
                        append("client_secret", creds.clientSecret)
                    })

                }


                parsedResponse = mapper.readValue(responseJson)

            } catch (e: Exception) {
                println("Error getting the bearer token")
                e.printStackTrace()
                delay(2000)
                continue
            } finally {
                client.close()
            }
            break
        }

        token = parsedResponse.accessToken
        expiresIn = parsedResponse.expiresIn

        latestCheck = Instant.now().epochSecond

        return token
    }

    suspend fun getToken(): String {
        return if (isTokenExpired()) generateNewToken() else token
    }


}
