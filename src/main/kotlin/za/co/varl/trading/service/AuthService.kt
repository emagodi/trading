package za.co.varl.trading.service

import za.co.varl.trading.payload.request.RegisterRequest
import za.co.varl.trading.payload.response.AuthenticationResponse


interface AuthService {
    fun registerUser(request: RegisterRequest): AuthenticationResponse
    fun authenticateUser(email: String, password: String): AuthenticationResponse

}