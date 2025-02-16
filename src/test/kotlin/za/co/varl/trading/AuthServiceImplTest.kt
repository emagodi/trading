package za.co.varl.trading

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import za.co.varl.trading.entities.User
import za.co.varl.trading.enums.EmploymentStatus
import za.co.varl.trading.enums.Purpose
import za.co.varl.trading.enums.SourceOfFunds
import za.co.varl.trading.enums.Role
import za.co.varl.trading.payload.request.RegisterRequest
import za.co.varl.trading.payload.response.AuthenticationResponse
import za.co.varl.trading.repository.UserRepository
import za.co.varl.trading.service.serviceImpl.AuthServiceImpl

class AuthServiceImplTest {

    private val userRepository: UserRepository = mock()
    private val authService = AuthServiceImpl(userRepository)

    private val registerRequest = RegisterRequest(
        firstName = "Edwin",
        lastName = "Magodi",
        password = "Password@123",
        dateOfBirth = "1990-01-01",
        residentialCountry = "USA",
        identityIssuingCountry = "USA",
        identityType = "Passport",
        identityNumber = "A123456789",
        identityExpiryDate = "2030-01-01",
        cellNumber = "+1234567890",
        email = "magodiedwin@gmail.com",
        purpose = Purpose.TRADING,
        employmentStatus = EmploymentStatus.EMPLOYED_PART_TIME,
        sourceOfFunds = SourceOfFunds.ALLOWANCE,
        role = Role.USER
    )

    @Test
    fun `test registerUser successfully registers a new user`() {
        // Arrange
        whenever(userRepository.existsByEmail(registerRequest.email)).thenReturn(false)

        val expectedUser = User(
            id = 1L,
            email = registerRequest.email,
            password = "hashed_password", // Assume this is hashed
            role = registerRequest.role,
            firstName = registerRequest.firstName,
            lastName = registerRequest.lastName,
            dateOfBirth = registerRequest.dateOfBirth,
            residentialCountry = registerRequest.residentialCountry,
            identityIssuingCountry = registerRequest.identityIssuingCountry,
            identityType = registerRequest.identityType,
            identityNumber = registerRequest.identityNumber,
            identityExpiryDate = registerRequest.identityExpiryDate,
            cellNumber = registerRequest.cellNumber,
            purpose = registerRequest.purpose,
            employmentStatus = registerRequest.employmentStatus,
            sourceOfFunds = registerRequest.sourceOfFunds
        )

        whenever(userRepository.save(any())).thenReturn(expectedUser)

        // Act
        val response: AuthenticationResponse = authService.registerUser(registerRequest)

        // Assert
        assertEquals("Registration successful", response.message)
        assertEquals(registerRequest.email, response.email)
        assertEquals(expectedUser.firstName, response.firstName)
        assertEquals(expectedUser.lastName, response.lastName)
        assertEquals(expectedUser.dateOfBirth, response.dateOfBirth)
        assertEquals(expectedUser.residentialCountry, response.residentialCountry)
        assertEquals(expectedUser.identityIssuingCountry, response.identityIssuingCountry)
        assertEquals(expectedUser.identityType, response.identityType)
        assertEquals(expectedUser.identityNumber, response.identityNumber)
        assertEquals(expectedUser.identityExpiryDate, response.identityExpiryDate)
        assertEquals(expectedUser.cellNumber, response.cellNumber)
        assertEquals(expectedUser.purpose.toString(), response.purpose)
        assertEquals(expectedUser.employmentStatus.toString(), response.employmentStatus)
        assertEquals(expectedUser.sourceOfFunds.toString(), response.sourceOfFunds)
        assertEquals(expectedUser.role, response.role) // Compare role as is

        verify(userRepository).save(any())
    }

    @Test
    fun `test registerUser throws Exception if user already exists`() {
        // Arrange
        whenever(userRepository.existsByEmail(registerRequest.email)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows<Exception> {
            authService.registerUser(registerRequest)
        }

        // Assert that the exception message is as expected
        assertEquals("User already exists", exception.message)
    }

    @Test
    fun `test authenticateUser successfully logs in a user`() {
        // Arrange
        val user = User(
            id = 1L,
            email = registerRequest.email,
            password = "Password@123", // This should match the password used in the verifyPassword method
            role = registerRequest.role,
            firstName = registerRequest.firstName,
            lastName = registerRequest.lastName,
            dateOfBirth = registerRequest.dateOfBirth,
            residentialCountry = registerRequest.residentialCountry,
            identityIssuingCountry = registerRequest.identityIssuingCountry,
            identityType = registerRequest.identityType,
            identityNumber = registerRequest.identityNumber,
            identityExpiryDate = registerRequest.identityExpiryDate,
            cellNumber = registerRequest.cellNumber,
            purpose = registerRequest.purpose,
            employmentStatus = registerRequest.employmentStatus,
            sourceOfFunds = registerRequest.sourceOfFunds
        )

        whenever(userRepository.findByEmail(registerRequest.email)).thenReturn(user)

        // Act
        val response: AuthenticationResponse = authService.authenticateUser(registerRequest.email, "Password@123")

        // Assert
        assertEquals("Login successful", response.message)
        assertEquals(registerRequest.email, response.email)
    }

    @Test
    fun `test authenticateUser throws Exception for invalid credentials`() {
        // Arrange
        whenever(userRepository.findByEmail(registerRequest.email)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows<Exception> {
            authService.authenticateUser(registerRequest.email, "wrong_password")
        }

        // Assert that the exception message is as expected
        assertEquals("Invalid email or password", exception.message)
    }
}