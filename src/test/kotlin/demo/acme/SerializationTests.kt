package demo.acme

import com.beust.klaxon.Klaxon
import demo.acme.model.Store
import org.junit.jupiter.api.Test

class SerializationTests {
    @Test
    fun deserialization() {
        var json = "{\n" +
                "        \"id\": 99998,\n" +
                "        \"code\": \"COalu-3CChAgwXVaj04i82R9FvWjYAl:571\",\n" +
                "        \"description\": null,\n" +
                "        \"name\": \"Store 8968118\",\n" +
                "        \"openingDate\": \"2021-07-19\",\n" +
                "        \"storeType\": \"RETAIL\"\n" +
                "    }"
        var store = Klaxon().parse<Store>(json)
        assert(store?.id == 99998L)
        assert(store?.code == "COalu-3CChAgwXVaj04i82R9FvWjYAl:571")
        assert(store?.description == null)
        assert(store?.name == "Store 8968118")
        assert(store?.openingDate.toString() == "2021-07-19")
        assert(store?.storeType == "RETAIL")
    }
}