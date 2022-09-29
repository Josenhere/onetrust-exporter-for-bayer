package extracts

import dataClasses.dspResponse.DataSubject
import dataClasses.ExtractSetting

class PurposeByRowExtract(extractSetting: ExtractSetting) : Extract(extractSetting) {

    override fun getHeaderLine(): String {
        return "DataSubject," +
                "CountryCode," +
                "SourceSystem," +
                "CustomerId," +
                "Language," +
                "LoggedInUserCWID," +

                "Purpose Name," +
                "Purpose Version," +
                "Status," +
                "Consent Date," +
                "lastTransactionCollectionPointId," +
                "First Transaction Date," +
                "Last Transaction Date," +
                "Withdrawal Date"
    }

    override fun createExtractSpecificLines(dataSubject: DataSubject): List<String> {

        val noTopicPurposes = listOf(
            "DP Processing", "DP Market Research", "DP Third Parties",
            "Conditions of Use"
        )

        val lines = dataSubject.purposes?.map { purpose ->

                val status =
                    if (purpose.name !in noTopicPurposes) purpose.topicStatus() else purpose.status

                dataSubject.identifier.toCSVString() + "," +
                dataSubject.dataElements?.find { it.name == "CountryCode" }?.value?.toCSVString() + "," +
                dataSubject.dataElements?.find { it.name == "SourceSystem" }?.value?.toCSVString() + "," +
                dataSubject.dataElements?.find { it.name == "CustomerId" }?.value?.toCSVString() + "," +
                dataSubject.dataElements?.find { it.name == "Language" }?.value?.toCSVString() + "," +
                dataSubject.dataElements?.find { it.name == "LoggedInUserCWID" }?.value?.toCSVString() + "," +

                purpose.name.toCSVString() + "," +
                purpose.version + "," +

                status + "," +
                purpose.consentDate + "," +
                purpose.lastTransactionCollectionPointId + "," +
                purpose.firstTransactionDate + "," +
                purpose.lastTransactionDate + "," +
                purpose.withdrawalDate

            }

        return lines ?: listOf()

    }
}
