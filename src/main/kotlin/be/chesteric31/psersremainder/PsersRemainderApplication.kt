package be.chesteric31.psersremainder

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PsersRemainderApplication

fun main(args: Array<String>) {
    SpringApplication.run(PsersRemainderApplication::class.java, *args)
}
