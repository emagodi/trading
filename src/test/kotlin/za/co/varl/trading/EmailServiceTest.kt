package za.co.varl.trading

import io.vertx.core.Vertx
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.SimpleMailMessage
import za.co.varl.trading.service.EmailService
import kotlin.test.assertEquals
import kotlin.test.fail

class EmailServiceTest {

    private lateinit var mailSender: JavaMailSender
    private lateinit var vertx: Vertx
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        mailSender = mock(JavaMailSender::class.java)
        vertx = Vertx.vertx()
        emailService = EmailService(mailSender, vertx)
    }

    @Test
    fun `sendOrderNotification sends email successfully`() {
        val email = "test@example.com"
        val orderDetails = "Order details here"

        // Call the method
        val future = emailService.sendOrderNotification(email, orderDetails)

        // Wait for the future to complete
        future.onComplete { ar ->
            if (ar.succeeded()) {
                // Verify that the mailSender.send() method was called
                verify(mailSender, times(1)).send(any(SimpleMailMessage::class.java))
            } else {
                fail("Expected to send email successfully, but it failed.")
            }
        }

        // Block the main thread until the future completes (for test purposes)
        future.result() // This will throw if the future fails
    }

    @Test
    fun `sendOrderNotification fails when mailSender throws an exception`() {
        val email = "test@example.com"
        val orderDetails = "Order details here"

        // Setup the mailSender to throw an exception when send is called
        doThrow(RuntimeException("Mail server not available")).whenever(mailSender).send(any(SimpleMailMessage::class.java))

        // Call the method and capture the Future result
        val future = emailService.sendOrderNotification(email, orderDetails)

        // Wait for the future to complete and assert that it failed with the expected exception
        future.onComplete { ar ->
            if (ar.failed()) {
                // Capture the exception thrown
                val exception = ar.cause()
                // Verify that the exception message is as expected
                assertEquals("Mail server not available", exception.message)
            } else {
                fail("Expected future to fail, but it succeeded.")
            }
        }

        // Block the main thread until the future completes (for test purposes)
        future.result() // This will throw if the future fails
    }
}