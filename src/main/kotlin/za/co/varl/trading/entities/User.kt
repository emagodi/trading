package za.co.varl.trading.entities

import za.co.varl.trading.enums.EmploymentStatus
import za.co.varl.trading.enums.Purpose
import za.co.varl.trading.enums.Role
import za.co.varl.trading.enums.SourceOfFunds

data class User(
    val id: Long,  // Ensure this property exists
    var password: String,
    val role: Role,
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
    val purpose: Purpose,
    val employmentStatus: EmploymentStatus,
    val sourceOfFunds: SourceOfFunds
)