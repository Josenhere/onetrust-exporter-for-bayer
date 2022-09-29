package extracts

import dataClasses.dspResponse.DataSubject
import dataClasses.ExtractSetting

class StandardExtract(extractSetting: ExtractSetting) : Extract(extractSetting) {

    override fun getHeaderLine(): String {
        return      "Identifier," +
                    "FirstName," +
                    "LastName," +
                    "CountryCode," +

                    "CustomerId," +
                    "Division," +
                    "InstanceId," +
                    "Language," +

                    "LegacyConsentReference," +
                    "LoggedInUserCWID," +
                    "SourceSystem," +

                    "BroadStatus," +

                    "Pending," +
                    "Active," +
                    "Withdrawn," +

                    "Total," +

                    "FirstTransactionDate," +
                    "LastTransactionDate," +

                    "NumberOfActiveBroadTopics"
    }

    override fun createExtractSpecificLines(dataSubject: DataSubject): List<String> {

        val nrPendingPurposes = dataSubject.purposes?.count { it.status == "PENDING" }?: 0
        val nrActivePurposes = dataSubject.purposes?.count { it.status == "ACTIVE" }?: 0
        val nrWithdrawnPurposes = dataSubject.purposes?.count { it.status == "WITHDRAWN" }?: 0

        val total = nrPendingPurposes + nrActivePurposes + nrWithdrawnPurposes

        val nrBroadActiveTopics = dataSubject.purposes?.find { it.name == "IMCM Broad Consent" }?.topics?.count {
            it.isConsented
        }

        val line =
            dataSubject.identifier + "," +
            dataSubject.dataElements?.find { it.name == "FirstName" }?.value?.toCSVString() + "," +
            dataSubject.dataElements?.find { it.name == "LastName" }?.value?.toCSVString() + "," +
            dataSubject.dataElements?.find { it.name == "CountryCode" }?.value + "," +

            dataSubject.dataElements?.find { it.name == "CustomerId" }?.value + "," +
            dataSubject.dataElements?.find { it.name == "Division" }?.value + "," +
            dataSubject.dataElements?.find { it.name == "InstanceId" }?.value + "," +
            dataSubject.dataElements?.find { it.name == "Language" }?.value + "," +

            dataSubject.dataElements?.find { it.name == "LegacyConsentReference" }?.value + "," +
            dataSubject.dataElements?.find { it.name == "LoggedInUserCWID" }?.value + "," +
            dataSubject.dataElements?.find { it.name == "SourceSystem" }?.value + "," +

            dataSubject.purposes?.find { it.name == "IMCM Broad Consent" }?.status + "," +

            nrPendingPurposes + "," +
            nrActivePurposes + "," +
            nrWithdrawnPurposes + "," +

            total + "," +

            dataSubject.purposes?.find { it.name == "IMCM Broad Consent" }?.firstTransactionDate + "," +
            dataSubject.purposes?.find { it.name == "IMCM Broad Consent" }?.lastTransactionDate + "," +

            nrBroadActiveTopics

        return listOf(line)
    }

}
