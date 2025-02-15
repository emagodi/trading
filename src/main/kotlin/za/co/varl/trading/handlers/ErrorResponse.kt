package za.co.varl.trading.handlers

import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val error: String,
    val timestamp: Instant,
    val message: String,
    val path: String
) {
    // Secondary constructor for default values
    constructor() : this(0, "", Instant.now(), "", "")
}