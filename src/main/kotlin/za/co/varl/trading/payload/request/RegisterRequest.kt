package za.co.varl.trading.payload.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import za.co.varl.trading.enums.EmploymentStatus
import za.co.varl.trading.enums.Purpose
import za.co.varl.trading.enums.Role
import za.co.varl.trading.enums.SourceOfFunds

data class RegisterRequest @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("dateOfBirth") val dateOfBirth: String,
    @JsonProperty("residentialCountry") val residentialCountry: String,
    @JsonProperty("identityIssuingCountry") val identityIssuingCountry: String,
    @JsonProperty("identityType") val identityType: String,
    @JsonProperty("identityNumber") val identityNumber: String,
    @JsonProperty("identityExpiryDate") val identityExpiryDate: String,
    @JsonProperty("cellNumber") val cellNumber: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("purpose") val purpose: Purpose,
    @JsonProperty("employmentStatus") val employmentStatus: EmploymentStatus,
    @JsonProperty("sourceOfFunds") val sourceOfFunds: SourceOfFunds,
    @JsonProperty("role") val role: Role
)