package za.co.varl.trading.controller

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import za.co.varl.trading.payload.request.ChangePasswordRequest
import za.co.varl.trading.payload.request.LoginRequest
import za.co.varl.trading.payload.request.RegisterRequest
import za.co.varl.trading.payload.response.AuthenticationResponse
import za.co.varl.trading.service.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

class AuthController(private val authService: AuthService) {

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())

    fun setupRoutes(router: Router) {
        router.post("/api/auth/register").handler(this::register)
        router.post("/api/auth/login").handler(this::login)
        router.post("/api/auth/changepassword").handler(this::changePassword)
    }

    private fun register(ctx: RoutingContext) {
        try {
            val request = ctx.body().asJsonObject().mapTo(RegisterRequest::class.java)
            val response: AuthenticationResponse = authService.registerUser(request)

            ctx.response()
                .setStatusCode(201)
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(response))
        } catch (e: Exception) {
            ctx.response()
                .setStatusCode(400)
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(mapOf("error" to e.message)))
        }
    }

    private fun login(ctx: RoutingContext) {
        try {
            val request = ctx.body().asJsonObject().mapTo(LoginRequest::class.java)
            val response: AuthenticationResponse = authService.authenticateUser(request.email, request.password)

            ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(response))
        } catch (e: Exception) {
            ctx.response()
                .setStatusCode(401)
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(mapOf("error" to e.message)))
        }
    }

    private fun changePassword(ctx: RoutingContext) {
        try {
            val request = ctx.body().asJsonObject().mapTo(ChangePasswordRequest::class.java)
            val email = ctx.request().getParam("email")
            val responseMessage = authService.changePassword(email, request)

            ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(mapOf("message" to responseMessage)))
        } catch (e: Exception) {
            ctx.response()
                .setStatusCode(400)
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(mapOf("error" to e.message)))
        }
    }
}