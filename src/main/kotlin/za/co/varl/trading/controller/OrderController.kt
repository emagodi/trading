package za.co.varl.trading.controller

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.RoutingContext
import za.co.varl.trading.entities.Order
import za.co.varl.trading.entities.OrderBook
import za.co.varl.trading.payload.request.CreateOrderRequest
import za.co.varl.trading.service.OrderService
import za.co.varl.trading.service.RateLimitService
import za.co.varl.trading.service.EmailService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import za.co.varl.trading.config.JwtUtil
import za.co.varl.trading.entities.Trade

class OrderController(
    private val orderService: OrderService,
    private val vertx: Vertx,
    private val jwtUtil: JwtUtil,
    private val rateLimitService: RateLimitService,
    private val emailService: EmailService // Inject EmailService
) {

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())

    fun setupRoutes(router: Router) {
        router.route().handler(BodyHandler.create())
        router.post("/api/orders/limit").handler(this::authenticate).handler(this::createLimitOrder)
        router.get("/api/orders").handler(this::authenticate).handler(this::getAllOrders)
        router.get("/api/orders/:id").handler(this::authenticate).handler(this::getOrderById)
        router.get("/:pair/orderbook").handler(this::authenticate).handler(this::getOrderBook)
        router.get("/:pair/tradehistory").handler(this::authenticate).handler(this::getRecentTrades)
        router.get("/:pair/openorders").handler(this::authenticate).handler(this::getOpenOrders) // Restricted to ADMIN
        router.get("/customerOrderId/:customerOrderId/openorders").handler(this::authenticate).handler(this::getOpenOrdersByCustomerId)
    }

    private fun authenticate(ctx: RoutingContext) {
        val authHeader = ctx.request().getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            if (jwtUtil.validateToken(token)) {
                if (jwtUtil.isTokenExpired(token)) {
                    ctx.response().setStatusCode(401).end("Token has expired")
                    return
                }

                val role = try {
                    jwtUtil.extractRole(token)
                } catch (e: IllegalArgumentException) {
                    ctx.response().setStatusCode(401).end("Invalid token: ${e.message}")
                    return
                }

                val email = try {
                    jwtUtil.extractEmail(token)
                } catch (e: IllegalArgumentException) {
                    ctx.response().setStatusCode(401).end("Invalid token: ${e.message}")
                    return
                }

                // Store email in context for later use
                ctx.put("email", email)

                // Check rate limit here
                if (rateLimitService.isRateLimited(email)) {
                    ctx.response().setStatusCode(429).end("Too Many Requests")
                    return
                }

                // Add rate limit headers
                ctx.response().putHeader("X-RateLimit-Limit", "5") // Max requests
                ctx.response().putHeader("X-RateLimit-Remaining", rateLimitService.getRemainingRequests(email).toString())
                ctx.response().putHeader("X-RateLimit-Reset", (System.currentTimeMillis() + 60000).toString()) // Reset time

                // Store role and permissions in context
                ctx.put("role", role)
                ctx.put("permissions", jwtUtil.extractPermissions(token)) // New extraction for permissions
                ctx.next() // Continue to the next handler
            } else {
                ctx.response().setStatusCode(401).end("Invalid token")
            }
        } else {
            ctx.response().setStatusCode(401).end("Authorization header missing")
        }
    }

    private fun createLimitOrder(ctx: RoutingContext) {
        val permissions = ctx.get<List<String>>("permissions")
        if (!permissions.contains("TRADE")) {
            ctx.response().setStatusCode(403).end("Access denied: You do not have permission to create orders.")
            return
        }

        try {
            val request = ctx.body().asJsonObject().mapTo(CreateOrderRequest::class.java)
            val order: Order = orderService.createLimitOrder(request)
            orderService.matchOrders(order.pair)

            // Retrieve the email from the context
            val email = ctx.get<String>("email") ?: "default@example.com" // Fallback if email is not present

            // Prepare order details for email notification
            val orderDetails = "Order ID: ${order.id}, Side: ${order.side}, Quantity: ${order.quantity}, Price: ${order.price}, Pair: ${order.pair}"
            emailService.sendOrderNotification(email, orderDetails) // Use the email from the context

            ctx.response()
                .setStatusCode(201)
                .end(objectMapper.writeValueAsString(order))
        } catch (e: Exception) {
            ctx.response()
                .setStatusCode(400) // Bad Request
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(mapOf("error" to e.message)))
        }
    }

    private fun getAllOrders(ctx: RoutingContext) {
        val orders: List<Order> = orderService.getAllOrders()
        ctx.response()
            .setStatusCode(200)
            .putHeader("Cache-Control", "max-age=120, public") // Cache for 120 seconds
            .end(objectMapper.writeValueAsString(orders))
    }

    private fun getOrderById(ctx: RoutingContext) {
        val id = ctx.pathParam("id")
        val order: Order? = orderService.getOrderById(id)
        if (order != null) {
            ctx.response()
                .setStatusCode(200)
                .putHeader("Cache-Control", "max-age=60, public") // Cache for 60 seconds
                .end(objectMapper.writeValueAsString(order))
        } else {
            ctx.response().setStatusCode(404).end("Order not found")
        }
    }

    private fun getOrderBook(ctx: RoutingContext) {
        val pair = ctx.pathParam("pair")
        val orderBook: OrderBook = orderService.getOrderBook(pair)
        ctx.response()
            .setStatusCode(200)
            .putHeader("Cache-Control", "max-age=90, public") // Cache for 90 seconds
            .end(objectMapper.writeValueAsString(orderBook))
    }

    private fun getRecentTrades(ctx: RoutingContext) {
        val pair = ctx.pathParam("pair")
        val trades: List<Trade> = orderService.getRecentTrades(pair)
        ctx.response()
            .setStatusCode(200)
            .putHeader("Cache-Control", "max-age=120, public") // Cache for 120 seconds
            .end(objectMapper.writeValueAsString(trades))
    }

    private fun getOpenOrders(ctx: RoutingContext) {
        val permissions = ctx.get<List<String>>("permissions")
        if (!permissions.contains("VIEW")) {
            ctx.response().setStatusCode(403).end("Access denied: You do not have permission to view open orders.")
            return
        }

        val pair = ctx.pathParam("pair")
        val openOrders: List<Order> = orderService.getOpenOrders(pair)

        if (openOrders.isNotEmpty()) {
            ctx.response()
                .setStatusCode(200)
                .putHeader("Cache-Control", "max-age=60, public") // Cache for 60 seconds
                .end(objectMapper.writeValueAsString(openOrders))
        } else {
            ctx.response().setStatusCode(404).end("No open orders found for the specified pair.")
        }
    }

    private fun getOpenOrdersByCustomerId(ctx: RoutingContext) {
        val customerOrderId = ctx.pathParam("customerOrderId")
        val openOrders: List<Order> = orderService.getOpenOrdersByCustomerId(customerOrderId)

        if (openOrders.isNotEmpty()) {
            ctx.response()
                .setStatusCode(200)
                .putHeader("Cache-Control", "max-age=60, public") // Cache for 60 seconds
                .end(objectMapper.writeValueAsString(openOrders))
        } else {
            ctx.response().setStatusCode(404).end("No open orders found for the specified customer order ID.")
        }
    }
}