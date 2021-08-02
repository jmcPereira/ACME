package demo.acme.component

import demo.acme.StoreRepository
import demo.acme.model.Season
import demo.acme.model.Store
import demo.acme.service.DataFetchService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.tomcat.util.http.fileupload.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.measureTimeMillis
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class UpdateStoreInformationSchedule {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    var csvFilePath: String? = null

    @Autowired
    lateinit var repository: StoreRepository

    @Autowired
    lateinit var dataFetchService: DataFetchService
    @Scheduled(initialDelay = 0, fixedDelay = 3600000L)
    fun writeCurrentTime() = try {
        val elapsed = measureTimeMillis {
            runBlocking {
                val stores = async { dataFetchService.fetchStores() }
                val csv = async { dataFetchService.fetchCSV() }
                val storesAndSeasons = async { dataFetchService.fetchStoresAndSeasons() }
                val addSeasons = async {
                    storesAndSeasons.await()?.forEach { storeAndSeason ->
                        val matchingStore = stores.await()[storeAndSeason.storeId]
                        matchingStore?.seasons?.add(Season().apply { season = storeAndSeason.season })
                    }
                }
                val addInfo = async {
                    csv.await()?.forEach { csvInfo ->
                        val matchingStore = stores.await()[csvInfo.key]
                        if (matchingStore != null)
                            matchingStore.additionalInfo = csvInfo.value
                    }
                }
                addSeasons.await()
                addInfo.await()
                val timeToSaveRecords = measureTimeMillis {
                    repository.deleteAll()
                    repository.saveAllAndFlush(stores.await().values)
                }
                logger.info("Saved ${stores.await().size} new Store records in $timeToSaveRecords milliseconds.")
                exportToCSV(stores.await().values)
            }
        }
        logger.info("Fetched data and updated DB in $elapsed milliseconds.")
    } catch (e: Exception) {
        logger.error("Error thrown while fetching data.", e)
    }

    fun exportToCSV(stores: Collection<Store>) = try {
        val elapsed = measureTimeMillis {
            val tempPath = System.getProperty("java.io.tmpdir")
            val acmeDir = Paths.get(tempPath, "ACME_CSV")
            if (!Files.exists(acmeDir))
                Files.createDirectory(acmeDir)
            FileUtils.cleanDirectory(acmeDir.toFile())
            val file = Paths.get(acmeDir.toString(), "ACME_data.csv").toFile()
            val writer = BufferedWriter(FileWriter(file))
            CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "Store Id", "Store Code", "Store Description", "Store Name",
                        "Store Opening Date", "Store Type", "Seasons", "Additional Information"
                    )
            ).use {
                for (store in stores) {
                    it.printRecord(
                        listOf(
                            store.id,
                            store.code,
                            store.description,
                            store.name,
                            store.openingDate,
                            store.storeType,
                            store.seasons,
                            store.additionalInfo
                        )
                    )
                }
            }
            csvFilePath = file.toString()
        }
        logger.info("Exported database to .csv in $elapsed milliseconds.")
    } catch (e: Exception) {
        logger.error("Error thrown while exporting data to CSV",e)
    }
}