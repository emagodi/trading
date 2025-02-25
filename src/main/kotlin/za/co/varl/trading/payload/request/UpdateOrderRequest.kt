package za.co.varl.trading.payload.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateOrderRequest @JsonCreator constructor(
    @JsonProperty("newRemainingQuantity") val newRemainingQuantity: Double? = null,
    @JsonProperty("newTotalQuantity") val newTotalQuantity: Double? = null,
    @JsonProperty("newPrice") val newPrice: Double? = null,
    @JsonProperty("customerOrderId") val customerOrderId: String? = null,
    @JsonProperty("modifyMatchStrategy") val modifyMatchStrategy: String
)