package demo.acme

import com.beust.klaxon.Klaxon
import demo.acme.model.Store
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@TestPropertySource(properties = ["app.scheduling.enable=false"])
@AutoConfigureMockMvc
@SpringBootTest
class AcmeApplicationTests {

    @Autowired
    lateinit var repository: StoreRepository

    @Autowired
    lateinit var mvc: MockMvc

    @AfterEach
    fun clearDBEntries(){
        repository.deleteAll()
    }
    @Test
    fun givenStore_whenGetStores_thenStatus200() {
        repository.save(Store().apply { name = "Store Test" })

        mvc.perform(
            get("/api/getStores").queryParam("page", "0")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath("$['storesPage'].[0].name").value("Store Test"))
    }

    @Test
    fun givenEmptyRepository_whenGetStores_thenStatus200() {
        mvc.perform(
            get("/api/getStores").queryParam("page", "0")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath("$['storesPage']").isEmpty)
    }
    @Test
    fun lackingPageRequestParam_whenGetStores_thenStatus400() {
        mvc.perform(
            get("/api/getStores")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun withStore_whenGetStoresWithFilter_thenStatus200() {
        val stores = listOf(
            Store().apply { name = "Store 1" },
            Store().apply { name = "Store 2" },
            Store().apply { name = "Store 3" })
        repository.saveAll(stores)

        mvc.perform(
            get("/api/getStores").queryParam("page", "0")
                .queryParam("storeNameFilter", "Store 2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath<Collection<String>>("$['storesPage']", hasSize(1)))
    }

    @Test
    fun withStore_whenGetStoresWithPartialFilter_thenStatus200() {
        val stores = listOf(
            Store().apply { name = "Store 1" },
            Store().apply { name = "Store 2" },
            Store().apply { name = "Store 3" })
        repository.saveAll(stores)

        mvc.perform(
            get("/api/getStores").queryParam("page", "0")
                .queryParam("storeNameFilter", "tore")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath<Collection<String>>("$['storesPage']", hasSize(3)))
    }

    @Test
    fun withStore_whenUpdateExistingStore_thenStatus200() {
        val store = Store().apply { name = "Store 1" }
        repository.save(store)
        mvc.perform(
            put("/api/updateStore")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Klaxon().toJsonString(store.apply { name = "New name" }))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("updated"))
        assert(repository.findById(store.id).get().name == "New name")
    }

    @Test
    fun withStore_whenUpdateWithBadJson_thenStatus400() {
        val store = Store().apply { name = "Store 1" }
        repository.save(store)
        mvc.perform(
            put("/api/updateStore")
                .contentType(MediaType.APPLICATION_JSON)
                .content("Not JSON!")
        )
            .andExpect(status().isBadRequest)
        assert(repository.findById(store.id).get().name == "Store 1")
    }

    @Test
    fun withoutStore_whenUpdate_thenStatus404() {
        mvc.perform(
            put("/api/updateStore")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Klaxon().toJsonString(Store().apply { name = "Some name" }))
        )
            .andExpect(status().isNotFound)
            .andExpect(status().reason(containsString("Store ID does not exist!")))
        assert(repository.count() == 0L)
    }

    //TODO: Missing tests for "/api/isDataAvailable" && "/api/csv". Figure out how to mock UpdateStoreInformationSchedule
}
