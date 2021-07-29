package demo.acme

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "datafetchservice")
class AppConfiguration {
    @Value("sourceApiUrl")
    lateinit var sourceApiUrl: String
    @Value("storePagesEndpoint")
    lateinit var storePagesEndpoint: String
    @Value("storeSeasonEndpoint")
    lateinit var storeSeasonEndpoint: String
    @Value("extraDataCsv")
    lateinit var extraDataCsv: String
    @Value("apiKey")
    lateinit var apiKey: String
    @Value("maximumNumberOfPages")
    lateinit var maximumNumberOfPages:String
}