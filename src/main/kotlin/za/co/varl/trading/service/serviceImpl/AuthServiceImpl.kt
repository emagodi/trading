package za.co.varl.trading.service.serviceImpl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import za.co.varl.trading.payload.request.RegisterRequest
import za.co.varl.trading.payload.response.AuthenticationResponse
import za.co.varl.trading.entities.User
import za.co.varl.trading.payload.request.ChangePasswordRequest
import za.co.varl.trading.repository.UserRepository
import za.co.varl.trading.service.AuthService
import za.co.varl.trading.enums.Role
import java.util.Date

@Service
class AuthServiceImpl(private val userRepository: UserRepository) : AuthService {

    @Value("\${jwt.secret}")
    lateinit var secretKey: String

    override fun registerUser(request: RegisterRequest): AuthenticationResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw Exception("User already exists")
        }

        val userRole = Role.valueOf(request.role.name)
        val user = User(
            id = generateUserId(),
            password = hashPassword(request.password),
            role = userRole,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            residentialCountry = request.residentialCountry,
            identityIssuingCountry = request.identityIssuingCountry,
            identityType = request.identityType,
            identityNumber = request.identityNumber,
            identityExpiryDate = request.identityExpiryDate,
            cellNumber = request.cellNumber,
            email = request.email,
            purpose = request.purpose,
            employmentStatus = request.employmentStatus,
            sourceOfFunds = request.sourceOfFunds
        )
        userRepository.save(user)

        val token = generateToken(user)
        return AuthenticationResponse(
            message = "Registration successful",
            token = token,
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            dateOfBirth = user.dateOfBirth,
            residentialCountry = user.residentialCountry,
            identityIssuingCountry = user.identityIssuingCountry,
            identityType = user.identityType,
            identityNumber = user.identityNumber,
            identityExpiryDate = user.identityExpiryDate,
            cellNumber = user.cellNumber,
            email = user.email,
            purpose = user.purpose.toString(),
            employmentStatus = user.employmentStatus.toString(),
            sourceOfFunds = user.sourceOfFunds.toString(),
            role = user.role
        )
    }

    override fun authenticateUser(email: String, password: String): AuthenticationResponse {
        val user = userRepository.findByEmail(email)
        if (user != null && verifyPassword(password, user.password)) {
            val token = generateToken(user)
            return AuthenticationResponse(
                message = "Login successful",
                token = token,
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                dateOfBirth = user.dateOfBirth,
                residentialCountry = user.residentialCountry,
                identityIssuingCountry = user.identityIssuingCountry,
                identityType = user.identityType,
                identityNumber = user.identityNumber,
                identityExpiryDate = user.identityExpiryDate,
                cellNumber = user.cellNumber,
                email = user.email,
                purpose = user.purpose.toString(),
                employmentStatus = user.employmentStatus.toString(),
                sourceOfFunds = user.sourceOfFunds.toString(),
                role = user.role
            )
        } else {
            throw Exception("Invalid email or password")
        }
    }

    private fun generateToken(user: User): String {
        return Jwts.builder()
            .setSubject(user.email)
            .claim("role", user.role.name)
            .claim("permissions", user.role.permissions.map { it.name })
            .claim("email", user.email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 86400000)) // 1 day
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    private fun generateUserId(): Long {
        return userRepository.getAllUsers().size.toLong() + 1
    }

    fun hashPassword(password: String): String {
        // Implement your password hashing logic here
        return password // Placeholder
    }

    fun verifyPassword(inputPassword: String, storedPassword: String): Boolean {
        return inputPassword == storedPassword
    }

    override fun changePassword(email: String, request: ChangePasswordRequest): String {
        val user = userRepository.findByEmail(email) ?: throw Exception("User not found")

        if (!verifyPassword(request.currentPassword, user.password)) {
            throw Exception("Current password is incorrect")
        }

        user.password = hashPassword(request.newPassword)
        userRepository.save(user)

        return "Password changed successfully"
    }
}