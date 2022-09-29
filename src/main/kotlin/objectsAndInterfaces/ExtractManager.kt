package objectsAndInterfaces

import extracts.Extract
import extracts.ExtractFactory
import java.io.File

fun main() {

}

object ExtractManager : BasicClass("ManagerLogger") {

    private val runningExtractsList = mutableListOf<Extract>()

    fun manageExtractStopping() {

        SftpManager.Settings.get().forEach { settingExtract ->

            val runningExtract = runningExtractsList.find { it.id == settingExtract.id }

            if ( runningExtract != null && settingExtract.shouldStop ) {
                runningExtract.shouldStop = true
            }

        }

    }

    fun manageDatabaseCleaning() {

        // Check if DB is needed for any scheduled quick exports
        val countryCodesWithScheduledQuickExport = SftpManager.Settings.get().filter {
            it.quick && it.cutoffDate > Agenda.dateToday()
        }.map { it.countryCode }.distinct()

        // find local country DB directories
        val listOfCountryDbCountryCodes = File(Settings.getDbPath()).listFiles()?.map { it.name }

        val stillRunningCountryCodes = runningExtractsList.map { it.countryCode }.distinct()

        // delete country DB without scheduled quick export
        listOfCountryDbCountryCodes
            ?.filter { it !in countryCodesWithScheduledQuickExport }
            ?.filter { it !in stillRunningCountryCodes }
            ?.forEach { countryDbToDelete ->
                File("${Settings.getDbPath()}/$countryDbToDelete").deleteRecursively()
        }


    }

    suspend fun newExtractsToRun(): List<Extract> {

        SftpManager.Settings.get().forEach { extractSetting ->

            if (
                !extractSetting.shouldStop &&
                extractSetting.countryCode !in runningExtractsList.map { it.countryCode } &&
                extractSetting.plannedDayOfWeek == Agenda.dayToday() &&
                extractSetting.plannedStartTime <= Agenda.timeNow() &&
                extractSetting.cutoffDate >= Agenda.dateToday() &&
                SftpManager.CompletedList.get().none {
                    it.id == extractSetting.id &&
                            it.completedDate == Agenda.dateToday()
                }
            ) {
                val extract = ExtractFactory.create(extractSetting)
                runningExtractsList.add(extract)
                extract.logProgress()
            }

        }

        return runningExtractsList.filter { it.getStatus() == Extract.Status.PENDING }

    }

    fun getExtractsWith(status: Extract.Status): List<Extract> {
        return runningExtractsList.filter { it.getStatus() == status }
    }

    suspend fun actionFinalCompletionSteps(extract: Extract) {

        with(extract) {

            setStatus(Extract.Status.COMPLETED).also { logProgress() }

            File("temp/$extractFileName").delete()
            File("temp/$logFileName").delete()
            File("temp/$zippedExtractFileName").delete()

            if (!shouldStop) SftpManager.CompletedList.add(extract)

            runningExtractsList.remove(extract)

        }

    }

}






