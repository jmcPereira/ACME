package demo.acme.service

import com.beust.klaxon.Klaxon
import demo.acme.model.CsvEntry
import demo.acme.model.Store
import demo.acme.model.StoreAndSeason
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis


@Service
class DataFetchService {
    @Value("\${datafetchservice.sourceApiUrl:}")
    lateinit var sourceApiUrl: String
    @Value("\${datafetchservice.storePagesEndpoint:}")
    lateinit var storePagesEndpoint: String
    @Value("\${datafetchservice.storeSeasonEndpoint:}")
    lateinit var storeSeasonEndpoint: String
    @Value("\${datafetchservice.extraDataCsv:}")
    lateinit var extraDataCsv: String
    @Value("\${datafetchservice.apiKey:}")
    lateinit var apiKey: String
    @Value("\${datafetchservice.maximumNumberOfFetchedPages:}")
    lateinit var maximumNumberOfFetchedPages:String

    fun fetchStoresPage(page: Int): List<Store>? {
        return fetchData("${sourceApiUrl}${storePagesEndpoint}?page=$page") {
            Klaxon().parseArray( it )
        }
    }

    fun fetchStoresAndSeasons(): List<StoreAndSeason>? {
        var result: List<StoreAndSeason>?
        val elapsed = measureTimeMillis {
            result = fetchData("${sourceApiUrl}${storeSeasonEndpoint}") { Klaxon().parseArray(it) }
        }
        println("Fetched ${result?.size} store season entries in $elapsed milliseconds.")
        return result
    }

    fun fetchCSV(): Map<Long, CsvEntry>? {
        var result : Map<Long, CsvEntry>?
        val elapsed = measureTimeMillis {
            result = fetchData("${sourceApiUrl}${extraDataCsv}") {
                val associate = CSVParser(it, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())
                    .associate { csvRecord ->
                        (csvRecord.get("Store id").toLongOrNull() ?: 0) to CsvEntry()
                            .apply {
                                specialField1 = csvRecord.get("Special field 1")
                                specialField2 = csvRecord.get("Special field 2")
                            }
                    }
                associate
            }
        }
        println("Fetched ${result?.keys?.size} csv entries in $elapsed milliseconds.")
        return result
    }

    fun fetchStores(): Map<Long,Store>{
        val stores = mutableMapOf<Long, Store>()
        val elapsed = measureTimeMillis {
            var page = 0
            while (page < maximumNumberOfFetchedPages.toInt()) {
                val asyncFetches = mutableListOf<Deferred<List<Store>?>>()
                var storesBatch: List<List<Store>>
                runBlocking {
                    repeat(10) {
                        asyncFetches.add(async { fetchStoresPage(page++) })
                    }
                    storesBatch = awaitAll(*asyncFetches.toTypedArray()).filterNotNull()
                }
                stores.putAll(storesBatch.flatten().associateBy { it.id }.toMap())
                if (storesBatch.any { it.isEmpty() })
                    break
            }
        }
        println("Fetched ${stores.size} store entries in $elapsed milliseconds.")
        return stores
    }

    fun <T> fetchData(url: String, bufferReaderHandler: (BufferedReader) -> T): T? {
        try {
            with(URL(url).openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                setRequestProperty("apiKey", apiKey)
                if (responseCode == 200)
                    inputStream.bufferedReader().use {
                        return bufferReaderHandler(it)
                    }
            }
        } catch (e: Exception) {
            println(e.message)
        }
        return null
    }
}


