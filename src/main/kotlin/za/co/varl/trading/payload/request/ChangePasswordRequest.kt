package za.co.varl.trading.payload.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ChangePasswordRequest @JsonCreator constructor(
    @JsonProperty("currentPassword") val currentPassword: String,
    @JsonProperty("newPassword") val newPassword: String
)