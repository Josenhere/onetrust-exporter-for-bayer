package objectsAndInterfaces

import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.CompletedExtract
import extracts.Extract
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import dataClasses.ExtractSetting
import kotlinx.coroutines.delay
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun main() {

}

object SftpManager : BasicClass("IOLogger") {

    private fun zipFile(file: File, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { output ->
            FileInputStream(file).use { input ->
                BufferedInputStream(input).use { origin ->
                    val entry = ZipEntry(file.name)
                    output.putNextEntry(entry)
                    origin.copyTo(output, 1024)
                }

            }
        }
    }

    suspend fun manageUploads() {

        ExtractManager.getExtractsWith(Extract.Status.QUEUED_FOR_UPLOAD).forEach { extract ->

            with(extract) {

                setStatus(Extract.Status.UPLOADING).also { logProgress() }

                zipFile(File("temp/$extractFileName"), File("temp/$zippedExtractFileName"))
                val uploadSuccess = sftpUpload(
                    "temp",
                    zippedExtractFileName,
                    zippedExtractFileName,
                    "DataMigration/Extracts/ExtractFiles"
                )

                if(uploadSuccess) {
                    ExtractManager.actionFinalCompletionSteps(extract)
                }

            }

        }

    }

    private fun createSession(): Session {

        val host = "transfer.production.pharma.api.int.bayer.com"
        val user = "onetrust-production"
        val keyLocation = "settings/access_sftp/key.id_rsa"

        val session: Session?
        val jsch = JSch()

        session = jsch.getSession(user, host)

        session.setConfig(
            "PreferredAuthentications",
            "publickey,gssapi-with-mic,keyboard-interactive,password"
        )

        jsch.addIdentity(keyLocation)

        val config = Properties()
        config["StrictHostKeyChecking"] = "no"

        session.setConfig(config)
        session.connect()

        return session
    }

    fun downloadFromSftp(fileName: String, remotePath: String) {

        // prepare scope of variable outside of try
        var session: Session? = null

        val maxAttempts = 2
        for (attempt in 1..maxAttempts) try {
            // make channel from session and connect
            session = createSession()
            val channelSftp = session.openChannel("sftp") as ChannelSftp
            channelSftp.connect()

            // copy remote log file to localhost.
            channelSftp["$remotePath/$fileName", "temp"]
            channelSftp.exit()
            session.disconnect()

            break

        } catch (e: Exception) {
            if (attempt == maxAttempts) {
                log.error("Fetch $fileName from SFTP failed $maxAttempts, maybe it doesn't exist. Continue.")
            }
            Thread.sleep(3000)
            continue
        } finally {
            session?.disconnect()
        }

    }

    suspend fun sftpUpload(localPath: String, localFileName: String, remoteFileName: String, remotePath: String): Boolean {

        var session: Session? = null
        var success = false

        val maxAttempts = 5
        for (attempt in 1..maxAttempts) try {

            // make channel from session and connect
            session = createSession()
            val channelSftp = session.openChannel("sftp") as ChannelSftp
            channelSftp.connect()

            // upload
            channelSftp.put("$localPath/$localFileName", "$remotePath/$remoteFileName")
            channelSftp.exit()

            success = true

            break

        } catch (e: Exception) {
            if (attempt == maxAttempts) log.error("Failed upload $localPath/$localFileName after $maxAttempts attempts")
            delay(10000)
            continue
        } finally {
            session?.disconnect()
        }

        return success
    }


    object CompletedList {
        private var completedExtracts = mutableListOf<CompletedExtract>()

        fun get(): MutableList<CompletedExtract> {
            return completedExtracts
        }

        suspend fun add(extract: Extract) {

            val completedExtract = CompletedExtract(
                id = extract.id,
                completedDate = Agenda.dateToday(),
            )

            completedExtracts.add(completedExtract)

            sftpUpload()

        }

        fun sftpDownload() {
            val fileName = "CompletedExtractsLog.json"
            downloadFromSftp(fileName, "DataMigration/Extracts/ProgressLogs/GeneralLogs")
            completedExtracts = try {
                mapper.readValue(File("temp/$fileName").readText())
            } catch (e: Exception) {
                emptyList<CompletedExtract>().toMutableList()
            }

        }

        private suspend fun sftpUpload() {
            val fileName = "CompletedExtractsLog.json"
            File("temp/$fileName").writeText(mapper.writeValueAsString(completedExtracts))
            sftpUpload("temp",fileName,fileName,"DataMigration/Extracts/ProgressLogs/GeneralLogs")
        }
    }

    object Settings {
        private var extractSettings = listOf<ExtractSetting>()
        fun get(): List<ExtractSetting> {
            return extractSettings
        }

        fun sftpDownload() {

            val fileName = "ExtractSettings.json"

            downloadFromSftp(fileName, "DataMigration/Extracts/ExtractSettings")

            extractSettings = try {
                mapper.readValue(File("temp/$fileName").readText())
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

        }
    }

    suspend fun uploadGeneralLogs() {

            sftpUpload("logs","extracts.log","${Agenda.dateToday()} extracts.log","DataMigration/Extracts/ProgressLogs/GeneralLogs")

    }
}
