package be.chesteric31.psersremainder

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduledTasks {

    @Scheduled(fixedRate = 5000)
    fun reportCurrentTime() {
        val log = LoggerFactory.getLogger(ScheduledTasks::class.java)
        val now = LocalDateTime.now()
        log.info("The time is now {$now}")
    }
}