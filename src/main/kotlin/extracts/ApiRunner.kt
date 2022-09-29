package extracts

import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.dspResponse.DspResponse
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import objectsAndInterfaces.BasicClass
import objectsAndInterfaces.Oauth
import objectsAndInterfaces.Settings
import java.io.File


class ApiRunner(private val countryCode: String) : BasicClass("ApiManagerLogger") {

    private var delayBetweenTries = 0L
    private var page = 0
    private val updatedDateFileName = "${Settings.getDbPath()}/$countryCode/UpdatedDate.txt"
    private val lastUpdatedDate = when {
        File(updatedDateFileName).exists() -> File(updatedDateFileName).readText()
        else -> null
    }

    fun getCurrentPage(): Int {
        return page
    }

    suspend fun getNumberOfPages(): Int {
        return get(false).totalPages
    }

    suspend fun get(ignoreCount: Boolean): DspResponse {

        var parsedResponse: DspResponse

        // make the call
        while (true) {

            val client = HttpClient {
                install(HttpTimeout) {
                    requestTimeoutMillis = 120000
                    socketTimeoutMillis = 120000
                }
            }

            try {

                delay(delayBetweenTries)

                val responseJson: String = client.request {

                    url("https://${Settings.getEnvironment()}/api/consentmanager/v1/datasubjects/profiles")

                    @Suppress("SpellCheckingInspection")
                    header("Authorization", "Bearer ${Oauth.getToken()}")
                    header("DataElementName", "CountryCode")
                    header("DataElementValue", countryCode)

                    parameter("sort", "desc")
                    parameter("size", Settings.getPageSize())
                    parameter("page", page)
                    parameter("sort", "lastModifiedDate,desc")

                    if (lastUpdatedDate != null) parameter("updatedSince", lastUpdatedDate)

                    if (ignoreCount) parameter("properties", "ignoreCount,includeAllPreferences,ignoreCustomPreferences")
                    else parameter("properties", "ignoreCustomPreferences,ignoreTopics")

                }

                parsedResponse = mapper.readValue(responseJson)
                if(delayBetweenTries > 0) delayBetweenTries /= 2

            } catch (e: Exception) {
                println("current delay: $delayBetweenTries $e")

                if(delayBetweenTries == 0L) delayBetweenTries = 1000L
                else if(delayBetweenTries < 120000L) delayBetweenTries *= 2

                continue

            } finally {
                client.close()
            }

            break

        }

        return parsedResponse

    }

    suspend fun getNextPageOfDataSubjects(): DspResponse {
        val response = get(true)
        page ++
        return response
    }

}


