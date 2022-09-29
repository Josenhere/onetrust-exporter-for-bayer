import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import objectsAndInterfaces.ExtractManager
import objectsAndInterfaces.SftpManager
import org.apache.logging.log4j.LogManager
import java.io.InputStreamReader
import java.util.*
import kotlin.system.exitProcess

val log = LogManager.getLogger("SupervisorLogger")!!

fun main(args: Array<String>) {
    val versionProperties = Properties()
    val url = object {}.javaClass.getResource("version.properties")
    if (url == null) {
        log.error("failed to get version property URL")
        exitProcess(0)
    }
    versionProperties.load(InputStreamReader(url.openStream()))
    log.info("starting exporter version " + versionProperties.getProperty("version"))
    if (args.isNotEmpty()) {
        log.warn("unexpected arguments were supplied, they will be ignored")
    }
    startExtractTool()
}


fun startExtractTool() {

    runBlocking {

        // List can be downloaded only once at the start due to concurrency
        log.info("download completed list")
        SftpManager.CompletedList.sftpDownload()

        while (true) {

            log.info("upload general logs")
            SftpManager.uploadGeneralLogs()

            // download settings and completed list
            SftpManager.Settings.sftpDownload()
            ExtractManager.manageDatabaseCleaning()

            // stop extracts if shouldStop == true
            ExtractManager.manageExtractStopping()

            // start new extract if adequate, will run in parallel in separate coroutines
            for( newExtract in ExtractManager.newExtractsToRun() ) {
                launch { newExtract.run() }
            }

            // manage uploads
            SftpManager.manageUploads()

            // wait
            delay(60000L)

        }
    }

}


