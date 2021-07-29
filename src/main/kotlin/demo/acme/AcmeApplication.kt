package demo.acme

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
    runApplication<AcmeApplication>(*args)
}

@SpringBootApplication
class AcmeApplication



