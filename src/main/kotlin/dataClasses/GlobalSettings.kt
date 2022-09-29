package dataClasses

data class SettingsJson(
    val dbPath: String,
    val numberOfDataSubjectsPerPageOfApiResponse: Int,
    val environment: String,
)
