package dataClasses.dspResponse


import com.fasterxml.jackson.annotation.JsonProperty

data class DataSubject(
    @JsonProperty("Id")
    val id: String,
    @JsonProperty("Language")
    val language: String?,
    @JsonProperty("Identifier")
    val identifier: String,
    @JsonProperty("LastUpdatedDate")
    val lastUpdatedDate: String?,
    @JsonProperty("CreatedDate")
    val createdDate: String,
    @JsonProperty("DataElements")
    val dataElements: List<DataElement>?,
    @JsonProperty("Purposes")
    val purposes: List<Purpose>?,
    @JsonProperty("TestDataSubject")
    val testDataSubject: Boolean?
)