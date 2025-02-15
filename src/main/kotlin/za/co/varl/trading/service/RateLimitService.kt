package za.co.varl.trading.service

import org.springframework.stereotype.Service


@Service
class RateLimitService {
    private val rateLimits = mutableMapOf<String, RateLimit>()

    fun isRateLimited(email: String): Boolean {
        val rateLimit = rateLimits.getOrPut(email) { RateLimit() }
        return rateLimit.isLimitExceeded()
    }

    fun getRemainingRequests(email: String): Int {
        val rateLimit = rateLimits.getOrPut(email) { RateLimit() }
        return rateLimit.maxRequests - rateLimit.requests
    }

    fun resetCounts(email: String) {
        val rateLimit = rateLimits[email]
        rateLimit?.reset()
    }

    // New method to reset all counts
    fun resetAllCounts() {
        rateLimits.values.forEach { it.reset() }
    }

    private class RateLimit {
        var requests: Int = 0
        val maxRequests: Int = 5 // Max requests allowed in the time window
        var lastRequestTime: Long = System.currentTimeMillis()

        fun isLimitExceeded(): Boolean {
            val currentTime = System.currentTimeMillis()
            // Reset the count if the time window has passed
            if (currentTime - lastRequestTime > 60000) { // 1 minute window
                requests = 0
                lastRequestTime = currentTime
            }
            requests++
            return requests > maxRequests
        }

        fun reset() {
            requests = 0
            lastRequestTime = System.currentTimeMillis()
        }
    }
}