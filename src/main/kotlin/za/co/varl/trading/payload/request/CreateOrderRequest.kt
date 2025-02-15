package za.co.varl.trading.payload.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import za.co.varl.trading.enums.OrderStatus
import za.co.varl.trading.enums.Side
import za.co.varl.trading.enums.TimeInForce

data class CreateOrderRequest @JsonCreator constructor(
    @JsonProperty("side") val side: Side, // "BUY" or "SELL"
    @JsonProperty("quantity") val quantity: Double,
    @JsonProperty("price") val price: Double,
    @JsonProperty("pair") val pair: String,
    @JsonProperty("customerOrderId") val customerOrderId: String?,
    @JsonProperty("timeInForce") val timeInForce: TimeInForce, // e.g., "GTC", "FOK", "IOC"

    @JsonProperty("postOnly") val postOnly: Boolean? = false,
    @JsonProperty("allowMargin") val allowMargin: Boolean? = false,
    @JsonProperty("reduceOnly") val reduceOnly: Boolean? = false,

    @JsonProperty("status") val status: OrderStatus = OrderStatus.PLACED // Default status
)