package dataClasses.dspResponse


import com.fasterxml.jackson.annotation.JsonProperty

data class Purpose(
    @JsonProperty("Id")
    val id: String,
    @JsonProperty("LastReceiptId")
    val lastReceiptId: String?,
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("Version")
    val version: Int,
    @JsonProperty("Status")
    val status: String,
    @JsonProperty("FirstTransactionDate")
    val firstTransactionDate: String,
    @JsonProperty("LastTransactionDate")
    val lastTransactionDate: String,
    @JsonProperty("WithdrawalDate")
    val withdrawalDate: Any?,
    @JsonProperty("ConsentDate")
    val consentDate: String?,
    @JsonProperty("ExpiryDate")
    val expiryDate: String?,
    @JsonProperty("TotalTransactionCount")
    val totalTransactionCount: Int,
    @JsonProperty("Topics")
    val topics: List<Topic>?,
    @JsonProperty("CustomPreferences")
    val customPreferences: List<CustomPreference>?,
    @JsonProperty("LastTransactionCollectionPointId")
    val lastTransactionCollectionPointId: String?,
    @JsonProperty("LastTransactionCollectionPointVersion")
    val lastTransactionCollectionPointVersion: Int?,
    @JsonProperty("PurposeNote")
    val purposeNote: Any?,
    @JsonProperty("LastUpdatedDate")
    val lastUpdatedDate: String?,
    @JsonProperty("LastInteractionDate")
    val lastInteractionDate: String?
    ) {
    fun topicStatus(): String {

        fun getTopicStatus(topicName: String): String {
            return if(this.topics != null) {
                when {
                    this.topics.any { it.name == topicName && it.isConsented } -> "Y"
                    this.topics.any { it.name == topicName } -> "N"
                    else -> "X"
                }
            } else ""

        }

        val statuses = listOf(
            getTopicStatus("Approved E-mail"),
            getTopicStatus("Call"),
            getTopicStatus("Fax"),
            getTopicStatus("Messaging"),
            getTopicStatus("Newsletter")
        )


        return status + " " + statuses.joinToString(";")

    }

}

class CustomPreference (
    @JsonProperty("Options")
    val options: List<CustomPrefOption>
)

class CustomPrefOption (
    @JsonProperty("Id")
    val id: String,
    @JsonProperty("Name")
    val name: String
)

data class Topic(
    @JsonProperty("Id")
    val id: String,
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("IsConsented")
    val isConsented: Boolean
)