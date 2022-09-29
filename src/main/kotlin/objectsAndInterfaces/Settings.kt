package objectsAndInterfaces

import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.SettingsJson
import java.io.File

object Settings: BasicClass("Logger") {
    private val parsedSettings: SettingsJson = mapper.readValue(File("settings/GlobalSettings.json").readText())
    fun getDbPath() = parsedSettings.dbPath
    fun getPageSize() = parsedSettings.numberOfDataSubjectsPerPageOfApiResponse
    fun getEnvironment() = parsedSettings.environment
}