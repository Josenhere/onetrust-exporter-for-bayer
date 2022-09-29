package extracts

import com.fasterxml.jackson.module.kotlin.readValue
import objectsAndInterfaces.Agenda
import objectsAndInterfaces.ExtractManager.mapper
import objectsAndInterfaces.Settings
import objectsAndInterfaces.SftpManager
import dataClasses.dspResponse.DataSubject
import dataClasses.ExtractSetting
import objectsAndInterfaces.ExtractManager.actionFinalCompletionSteps
import objectsAndInterfaces.ExtractManager.log
import java.io.File

fun main() {



}

abstract class Extract(extractSetting: ExtractSetting) {

    val id = extractSetting.id
    val countryCode = extractSetting.countryCode
    var shouldStop = extractSetting.shouldStop
    val extractFileName by lazy { "${Agenda.timestampForFileName()}_$id.csv" }
    val zippedExtractFileName by lazy { extractFileName.substringBeforeLast(".") + ".zip" }
    val logFileName = "${Agenda.timestampForFileName()}_ProgressLog_$id.txt"
    private val quick = extractSetting.quick
    private val pathToCountryDB = "${Settings.getDbPath()}/$countryCode"
    private var percentage: Int = 0
    private var previousPercentage: Int = -1
    private var status: Status = Status.PENDING
    private var run: Int = 0
    private var noDataSubjects = false

    fun setStatus(newStatus: Status) {
        status = newStatus
    }
    fun getStatus(): Status {
        return status
    }

    abstract fun getHeaderLine(): String
    private fun writeHeaderLine(headerLine:String) {

        File("temp/$extractFileName")
            .apply { parentFile.mkdirs() }
            .writeText(headerLine)
    }
    private fun appendLine(line: String) {
        File("temp/$extractFileName").appendText("\n$line")
    }
    private fun clearLocalCountryDB() {
        File(pathToCountryDB).deleteRecursively()
    }

    suspend fun run() {

        if(!quick) clearLocalCountryDB()

        // run two times, the second is to get even the most recent delta since start of first run
        run = 1
        downloadDataSubjectsToDB()

        if(!noDataSubjects) {

            run = 2
            downloadDataSubjectsToDB()

            treeWalkThroughCountryDBAndCreateCsv()
            setStatus(Status.QUEUED_FOR_UPLOAD).also { logProgress() }

        } else {
            log.error("No data subjects found for countryCode $countryCode, extract id $id")
            actionFinalCompletionSteps(this)
        }

    }

    abstract fun createExtractSpecificLines(dataSubject: DataSubject) : List<String>

    suspend fun logProgress() {

        val runningProgress = "Run$run ${percentage.toString().padStart(3,' ')} %"
        val logLine = "${Agenda.timestamp()} - $id $status " + if(status == Status.RUNNING) runningProgress else ""

        suspend fun updateLog() {
            File("temp/$logFileName").apply { parentFile.mkdirs() }.appendText("\n$logLine")
            SftpManager.sftpUpload("temp",logFileName,logFileName,"DataMigration/Extracts/ProgressLogs")
            log.info(logLine)
        }

        // initiate upload to SFTP
        if(status == Status.RUNNING) {
            if(percentage > previousPercentage) {
                updateLog()
                previousPercentage = percentage
            }
        } else {
            updateLog()
        }

    }

    private suspend fun downloadDataSubjectsToDB() {

        val apiRunner = ApiRunner(countryCode)
        var newUpdatedDate: String? = null
        var firstPage = true
        val totalPages = apiRunner.getNumberOfPages()

        percentage = 0
        previousPercentage = -1

        setStatus(Status.RUNNING).also { logProgress() }

        if (totalPages == 0) {
            noDataSubjects = true
        } else {

            do {

                if (shouldStop) { setStatus(Status.STOPPED).also { logProgress() }; break }

                val dspResponse = apiRunner.getNextPageOfDataSubjects()

                if (firstPage) {
                    newUpdatedDate = dspResponse.content.first().lastUpdatedDate
                    firstPage = false
                }

                dspResponse.content.forEach { ds ->
                    val pathToDataSubject = "$pathToCountryDB/${ds.id[0]}/${ds.id[1]}/${ds.id[2]}"
                    File("$pathToDataSubject/${ds.id}.json")
                        .apply { parentFile.mkdirs() }
                        .writeText(mapper.writeValueAsString(ds))
                }

                percentage = 100 * apiRunner.getCurrentPage() / totalPages
                logProgress()
            } while (!dspResponse.last)

        }

        if (newUpdatedDate != null) {
            File("$pathToCountryDB/UpdatedDate.txt")
                .writeText(newUpdatedDate.substringBefore('.'))
        }

    }

    private suspend fun treeWalkThroughCountryDBAndCreateCsv() {

        setStatus(Status.CREATING_CSV).also { logProgress() }

        writeHeaderLine(getHeaderLine())

        File(pathToCountryDB).walk().forEach { file ->
            if(file.isFile && file.name.contains(".json")) {
                val dataSubject: DataSubject = mapper.readValue(file.readText())
                val csvRows = createExtractSpecificLines(dataSubject)
                csvRows.forEach { row -> appendLine(row) }
            }
        }


    }


    fun Any.toCSVString(): String {

        return  if(this.toString().contains(",")) {
            val result = "\"$this\""
            println("added csv modification on INPUT: $this  ///  OUTPUT: $result")
            return result
        }
        else this.toString()

    }

    enum class Status {
        PENDING,
        RUNNING,
        STOPPED,
        CREATING_CSV,
        QUEUED_FOR_UPLOAD,
        UPLOADING,
        COMPLETED,
    }
    enum class TypeOfExtract {
        STANDARD_EXTRACT,
        PENDING_EXTRACT,
        SPECIAL_EXTRACT,
        PURPOSE_BY_ROW_EXTRACT
    }

}


