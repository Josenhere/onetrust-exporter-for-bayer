package extracts

import dataClasses.dspResponse.DataSubject
import dataClasses.ExtractSetting

class SpecialExtract(extractSetting: ExtractSetting) : Extract(extractSetting) {

    override fun getHeaderLine(): String {
        return      "DataSubject," +
                    "CountryCode," +
                    "SourceSystem," +
                    "CustomerId," +
                    "LoggedInUserCWID," +

                    "Broad Consent Status," +
                    "Broad Consent Date," +
                    "Broad Last Transaction Date," +

                    "DP Processing Status," +
                    "DP Processing Consent Date," +
                    "DP Processing Last Transaction Date," +

                    "DP Market Research Status," +
                    "DP Market Research Consent Date," +
                    "DP Market Research Last Transaction Date," +

                    "DP Third Parties Status," +
                    "DP Third Parties Consent Date," +
                    "DP Third Parties Last Transaction Date," +

                    "Narrow Purposes"
    }

    override fun createExtractSpecificLines(dataSubject: DataSubject): List<String> {

        val dEs = dataSubject.dataElements?.associate { dataElement -> dataElement.name to dataElement.value }
        val broad = dataSubject.purposes?.find { it.name == "IMCM Broad Consent" }
        val dpProcessing = dataSubject.purposes?.find { it.name == "DP Processing" }
        val dpMarket = dataSubject.purposes?.find { it.name == "DP Market Research" }
        val dpThirdP = dataSubject.purposes?.find { it.name == "DP Third Parties" }

        val narrowPurposes = dataSubject.purposes?.filterNot {
            it.name in listOf("IMCM Broad Consent","DP Processing","DP Market Research","DP Third Parties","Conditions of Use")
        }

        val line =
            dataSubject.identifier + "," +
                    dEs?.get("CountryCode") + "," +
                    dEs?.get("SourceSystem") + "," +
                    dEs?.get("CustomerId") + "," +
                    dEs?.get("LoggedInUserCWID") + "," +

                    broad?.status + "," +
                    broad?.consentDate + "," +
                    broad?.lastTransactionDate + "," +

                    dpProcessing?.status + "," +
                    dpProcessing?.consentDate + "," +
                    dpProcessing?.lastTransactionDate + "," +

                    dpMarket?.status + "," +
                    dpMarket?.consentDate + "," +
                    dpMarket?.lastTransactionDate + "," +

                    dpThirdP?.status + "," +
                    dpThirdP?.consentDate + "," +
                    dpThirdP?.lastTransactionDate + "," +

                    narrowPurposes?.joinToString(" | ") { it.name + ": " + it.topicStatus() }

        return listOf(line)
    }

}
