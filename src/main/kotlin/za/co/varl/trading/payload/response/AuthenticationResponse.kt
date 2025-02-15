package za.co.varl.trading.payload.response

import za.co.varl.trading.enums.Role

data class AuthenticationResponse(
    val message: String,
    val token: String?,
    val id: Long,  // Ensure the field is present
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val residentialCountry: String,
    val identityIssuingCountry: String,
    val identityType: String,
    val identityNumber: String,
    val identityExpiryDate: String,
    val cellNumber: String,
    val email: String,
    val purpose: String,
    val employmentStatus: String,
    val sourceOfFunds: String,
    val role: Role
)