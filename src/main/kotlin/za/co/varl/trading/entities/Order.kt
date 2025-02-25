package za.co.varl.trading.entities

import za.co.varl.trading.enums.OrderStatus
import za.co.varl.trading.enums.Side
import za.co.varl.trading.enums.TimeInForce

data class Order(
    val id: String,
    val side: Side,
    var quantity: Double,
    var price: Double,
    val pair: String,
    val customerOrderId: String?,
    val timeInForce: TimeInForce,
    var status: OrderStatus = OrderStatus.PLACED
)