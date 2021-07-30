package demo.acme.controller

import com.beust.klaxon.Klaxon
import demo.acme.StoreRepository
import demo.acme.component.UpdateStoreInformationSchedule
import demo.acme.model.Store
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.FileInputStream


@RestController
class StoreController {
    @Autowired
    private lateinit var updateStoreInformationSchedule: UpdateStoreInformationSchedule

    @Autowired
    private lateinit var repository: StoreRepository

    @GetMapping("/api/getStores", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getStores(@RequestParam page: Int, @RequestParam(required = false) storeNameFilter: String?): String {
        val pageSize = 20
        val pageRequest = PageRequest.of(page, pageSize, Sort.by("id"))
        val storesPage = if (storeNameFilter == null || storeNameFilter.isEmpty())
            repository.findAll(pageRequest)
        else repository.findByNameContaining(storeNameFilter, pageRequest)
        return Klaxon().toJsonString(
            object {
                val storesPage = storesPage.get().toList()
                val totalPages = storesPage.totalPages
            })
    }

    @PutMapping("/api/updateStore")
    fun updateStore(@RequestBody storeAsJson: String): String {
        var store: Store? = null
        try {
            store = Klaxon().parse<Store>(storeAsJson)
        } catch (e: Exception) {
        }
        if (store == null)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Couldn't match request body to json of Store object")
        val storeToUpdate = repository.findById(store.id).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Store ID does not exist!")
        storeToUpdate.name = store.name
        repository.saveAndFlush(storeToUpdate)
        return "updated"
    }

    @RequestMapping(
        value = ["/api/csv"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ResponseBody
    fun getFile(): ResponseEntity<InputStreamResource> {
        if (updateStoreInformationSchedule.csvFilePath == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Store information unavailable!")
        }
        val resource = InputStreamResource(FileInputStream(updateStoreInformationSchedule.csvFilePath))

        return ResponseEntity.ok()
            .contentLength(File(updateStoreInformationSchedule.csvFilePath).length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    @GetMapping("/api/isDataAvailable")
    fun isDataAvailable(): Boolean {
        return updateStoreInformationSchedule.csvFilePath != null && repository.count() > 0
    }
}