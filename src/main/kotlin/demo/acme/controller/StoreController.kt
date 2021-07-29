package demo.acme.controller

import com.beust.klaxon.Klaxon
import demo.acme.model.Store
import demo.acme.StoreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@RestController
class StoreController {
    @Autowired
    private val repository: StoreRepository? = null
    @GetMapping("/api/getStores")
    fun getStores(@RequestParam page: Int, @RequestParam(required = false) storeNameFilter: String?) : String {
        val pageSize = 20
        val pageRequest = PageRequest.of(page, pageSize, Sort.by("id"))
        val recordList =
            if(storeNameFilter == null || storeNameFilter.isEmpty())
                repository?.findAll(pageRequest)?.toList() ?: listOf()
            else
                repository?.findByNameContaining(storeNameFilter, pageRequest) ?: listOf()
        val storeCount =
            if(storeNameFilter == null || storeNameFilter.isEmpty())
                repository?.findAll()?.count() ?: 0
            else
                repository?.findAll()?.filterNotNull()?.filter { it.name == storeNameFilter }?.count() ?: 0

        return Klaxon().toJsonString(
            object {
                val storesPage = recordList.toList()
                val totalStores = storeCount
            })
    }
    @PutMapping("/api/updateStore")
    fun updateStore(@RequestBody store: String) : String {
        val store = Klaxon().parse<Store>(store)
        if(store != null && repository != null){
            var storeToUpdate = repository.findById(store.id).get()
            if(storeToUpdate != null){
                storeToUpdate.name = store.name
                repository.save(storeToUpdate)
                return "updated";
            }
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Store ID does not exist!")
    }

    @RequestMapping(value = ["/api/csv"], method = [RequestMethod.GET], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseBody
    fun getFile(@PathVariable("file_name") fileName: String?): FileSystemResource? {
        //return FileSystemResource(myService.getFileFor(fileName))
        return null
    }
}