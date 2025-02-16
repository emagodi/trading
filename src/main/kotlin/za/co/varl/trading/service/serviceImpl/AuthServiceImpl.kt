package za.co.varl.trading.service.serviceImpl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Service
import za.co.varl.trading.payload.request.RegisterRequest
import za.co.varl.trading.payload.response.AuthenticationResponse
import za.co.varl.trading.entities.User
import za.co.varl.trading.payload.request.ChangePasswordRequest
import za.co.varl.trading.repository.UserRepository
import za.co.varl.trading.service.AuthService
import za.co.varl.trading.enums.Role // Ensure this import is present
import java.util.Date

@Service
class AuthServiceImpl(private val userRepository: UserRepository) : AuthService {

    private val secretKey = "586B633834416E396D7436753879382F423F4428482B4C6250655367566B5970" // Use a strong secret key

    override fun registerUser(request: RegisterRequest): AuthenticationResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw Exception("User already exists")
        }

        val userRole = Role.valueOf(request.role.name) // Convert string to Role enum
        val user = User(
            id = generateUserId(),
            password = hashPassword(request.password),
            role = userRole, // Store as Role enum
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
            role = user.role // Convert Role enum to string for response
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
                role = user.role // Convert Role enum to string for response
            )
        } else {
            throw Exception("Invalid email or password")
        }
    }

    private fun generateToken(user: User): String {
        return Jwts.builder()
            .setSubject(user.email) // Subject is set to the user's email
            .claim("role", user.role.name) // Include role as string
            .claim("permissions", user.role.permissions.map { it.name }) // Include permissions
            .claim("email", user.email) // Include email in the claims
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    private fun generateUserId(): Long {
        return userRepository.getAllUsers().size.toLong() + 1 // Simple ID generation
    }

    private fun hashPassword(password: String): String {
        // Implement password hashing logic (e.g., BCrypt)
        return password // Replace with actual hashing
    }

    fun verifyPassword(inputPassword: String, storedPassword: String): Boolean {
        // Implement password verification logic
        return inputPassword == storedPassword // Replace with actual verification
    }

    override fun changePassword(email: String, request: ChangePasswordRequest): String {
        val user = userRepository.findByEmail(email) ?: throw Exception("User not found")

        // Verify the current password
        if (!verifyPassword(request.currentPassword, user.password)) {
            throw Exception("Current password is incorrect")
        }

        // Optionally validate new password here (length, complexity, etc.)
        user.password = hashPassword(request.newPassword) // Update to the new password
        userRepository.save(user)

        return "Password changed successfully"
    }
}