package za.co.varl.trading

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import za.co.varl.trading.service.RateLimitService

class RateLimitServiceTest {

    private val rateLimitService = RateLimitService()

    @Test
    fun `test initial request count for a new user`() {
        // Arrange
        val email = "test@example.com"

        // Act
        val remainingRequests = rateLimitService.getRemainingRequests(email)

        // Assert
        assertEquals(5, remainingRequests) // Should allow 5 requests initially
    }

    @Test
    fun `test request limit not exceeded after 4 requests`() {
        // Arrange
        val email = "test@example.com"
        repeat(4) {
            rateLimitService.isRateLimited(email) // Simulate 4 requests
        }

        // Act
        val remainingRequests = rateLimitService.getRemainingRequests(email)

        // Assert
        assertEquals(1, remainingRequests) // Should allow 1 request remaining
    }

    @Test
    fun `test request limit exceeded after 5 requests`() {
        // Arrange
        val email = "test@example.com"
        repeat(5) {
            rateLimitService.isRateLimited(email) // Simulate 5 requests
        }

        // Act
        val isRateLimited = rateLimitService.isRateLimited(email)

        // Assert
        assertEquals(true, isRateLimited) // Should be rate limited after 5 requests
    }

    @Test
    fun `test resetting counts for a specific user`() {
        // Arrange
        val email = "test@example.com"
        repeat(5) {
            rateLimitService.isRateLimited(email) // Simulate 5 requests
        }

        // Act
        rateLimitService.resetCounts(email)
        val remainingRequestsAfterReset = rateLimitService.getRemainingRequests(email)

        // Assert
        assertEquals(5, remainingRequestsAfterReset) // Should reset to 5 requests
    }

    @Test
    fun `test resetting all counts`() {
        // Arrange
        val email1 = "user1@example.com"
        val email2 = "user2@example.com"

        repeat(5) {
            rateLimitService.isRateLimited(email1) // Simulate 5 requests for user 1
        }
        repeat(3) {
            rateLimitService.isRateLimited(email2) // Simulate 3 requests for user 2
        }

        // Act
        rateLimitService.resetAllCounts()

        // Assert
        assertEquals(5, rateLimitService.getRemainingRequests(email1)) // Should reset to 5 requests for user 1
        assertEquals(5, rateLimitService.getRemainingRequests(email2)) // Should reset to 5 requests for user 2
    }
}