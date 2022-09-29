package dataClasses

import extracts.Extract


data class ExtractSetting(
    val id: String,
    val typeOfExtract: Extract.TypeOfExtract,
    val countryCode: String,
    val plannedDayOfWeek: String,
    val plannedStartTime: String,
    val quick: Boolean,
    val shouldStop: Boolean,
    val cutoffDate: String
)