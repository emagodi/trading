package za.co.varl.trading

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.beans.factory.annotation.Value
import za.co.varl.trading.config.JwtUtil
import za.co.varl.trading.controller.AuthController
import za.co.varl.trading.controller.OrderController
import za.co.varl.trading.service.AuthService
import za.co.varl.trading.service.OrderService
import za.co.varl.trading.service.RateLimitService
import za.co.varl.trading.service.EmailService

@SpringBootApplication
@EnableCaching // Enable caching in the Spring application
class TradingApplication {

	@Value("\${jwt.secret}")
	lateinit var jwtSecret: String // Inject the JWT secret from properties

	@Bean
	fun vertx(): Vertx {
		return Vertx.vertx() // Create a Vertx instance as a bean
	}

	@Bean
	fun vertxRouter(
		authService: AuthService,
		orderService: OrderService,
		rateLimitService: RateLimitService,
		emailService: EmailService // Inject EmailService
	): Router {
		val vertx = vertx() // Get the Vertx instance
		val router = Router.router(vertx)

		// Create an instance of JwtUtil using the injected secret
		val jwtUtil = JwtUtil(jwtSecret)

		// Add BodyHandler to parse the body of incoming requests
		router.route().handler(BodyHandler.create())

		// Set up the AuthController routes
		val authController = AuthController(authService, jwtUtil)
		authController.setupRoutes(router)

		// Set up the OrderController routes, passing emailService
		val orderController = OrderController(orderService, vertx, jwtUtil, rateLimitService, emailService)
		orderController.setupRoutes(router)

		// Start the Vert.x HTTP server
		vertx.createHttpServer()
			.requestHandler(router)
			.listen(8080) { result ->
				if (result.succeeded()) {
					println("Vert.x server is now listening on port 8080!")
				} else {
					println("Failed to bind Vert.x server: ${result.cause()}")
				}
			}
		return router
	}

	@Bean
	fun cacheManager(): CacheManager {
		return ConcurrentMapCacheManager("orders", "trades") // Define your cache names here
	}
}

fun main(args: Array<String>) {
	runApplication<TradingApplication>(*args)
}