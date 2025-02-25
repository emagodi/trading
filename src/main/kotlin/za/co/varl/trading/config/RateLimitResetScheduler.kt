package za.co.varl.trading.config


import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import za.co.varl.trading.service.RateLimitService

@Component
class RateLimitResetScheduler(private val rateLimitService: RateLimitService) {

    @Scheduled(fixedRate = 60000)
    fun reset() {
        rateLimitService.resetAllCounts()
    }
}