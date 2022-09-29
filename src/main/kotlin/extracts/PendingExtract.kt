package extracts

import dataClasses.dspResponse.DataSubject
import dataClasses.ExtractSetting

class PendingExtract(extractSetting: ExtractSetting) : Extract(extractSetting) {

    override fun getHeaderLine(): String {
        return "CountryCode,Identifier,Status,SourceSystem,LoggedInUserCWID,FirstTransactionDate,LastTransactionDate,TotalTransactionCount"
    }

    override fun createExtractSpecificLines(dataSubject: DataSubject): List<String> {
        val broadPurpose = dataSubject.purposes?.find { it.name == "IMCM Broad Consent" }
        val line = dataSubject.dataElements?.find { it.name == "CountryCode" }?.value.toString() + "," +
                dataSubject.identifier + "," +
                broadPurpose?.status + "," +
                dataSubject.dataElements?.find { it.name == "SourceSystem" }?.value + "," +
                dataSubject.dataElements?.find { it.name == "LoggedInUserCWID" }?.value + "," +
                broadPurpose?.firstTransactionDate + "," +
                broadPurpose?.lastTransactionDate + "," +
                broadPurpose?.totalTransactionCount
        return listOf(line)
    }

}

