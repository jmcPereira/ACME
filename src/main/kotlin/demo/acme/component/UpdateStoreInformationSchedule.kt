package demo.acme.component

import demo.acme.model.Season
import demo.acme.model.Store
import demo.acme.StoreRepository
import demo.acme.service.DataFetchService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.system.measureTimeMillis

@Component
class UpdateStoreInformationSchedule {
    @Autowired
    lateinit var repository: StoreRepository

    @Autowired
    lateinit var dataFetchService: DataFetchService

    @Scheduled(initialDelay = 0, fixedDelay = 3600000L)
    fun writeCurrentTime() = try {
        val elapsed = measureTimeMillis {
            runBlocking {
                val stores = async { dataFetchService.fetchStores().distinctBy { it.id } }
                val csv = async { dataFetchService.fetchCSV() }
                val storesAndSeasons = async { dataFetchService.fetchStoresAndSeasons() }
                val addSeasons = async {
                    storesAndSeasons.await()?.forEach { storeAndSeason ->
                        val matchingStore = stores.await().firstOrNull { it.id == storeAndSeason.storeId }
                        matchingStore?.seasons?.add(Season().apply { season = storeAndSeason.season })
                    }
                }
                val addInfo = async {
                    csv.await()?.forEach { csvInfo ->
                        val matchingStore = stores.await().firstOrNull { it.id == csvInfo.key }
                        if (matchingStore != null)
                            matchingStore.additionalInfo = csvInfo.value
                    }
                }
                addSeasons.await()
                addInfo.await()
                var timeToSaveRecords = measureTimeMillis {
                    repository.saveAll(stores.await())
                }
                println("Saved ${stores.await().size} new Store records in $timeToSaveRecords milliseconds.")
                exportToCSV(stores.await())
            }
        }
        println("Fetched data and updated DB in $elapsed milliseconds.")
    } catch (e: Exception) {
        println(e.message)
    }

    fun exportToCSV(stores: List<Store>) = try {
        var elapsed = measureTimeMillis {
            val file = File.createTempFile("ACME-data-", ".csv")
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
        }
        println("Exported database to .csv in $elapsed milliseconds.")
    } catch (e: Exception) {
        println(e.message)
    }
}