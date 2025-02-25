package za.co.varl.trading.service

import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender, private val vertx: Vertx) {

    fun sendOrderNotification(email: String, orderDetails: String): Future<Void> {
        val promise: Promise<Void> = Promise.promise()

        vertx.executeBlocking<Void>({ blockingPromise ->
            try {
                val mailMessage = SimpleMailMessage().apply {
                    setTo(email)
                    subject = "VARL Order Created"
                    text = orderDetails
                }

                mailSender.send(mailMessage)
                println("Email sent successfully to: $email")
                blockingPromise.complete()
            } catch (e: Exception) {
                println("Failed to send email: ${e.message}")
                blockingPromise.fail(e)
            }
        }, { res ->
            if (res.succeeded()) {
                promise.complete()
            } else {
                promise.fail(res.cause())
            }
        })

        return promise.future()
    }
}