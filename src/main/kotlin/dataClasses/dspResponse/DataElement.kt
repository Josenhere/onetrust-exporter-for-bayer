package dataClasses.dspResponse


import com.fasterxml.jackson.annotation.JsonProperty

data class DataElement(
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("Value")
    val value: String
)