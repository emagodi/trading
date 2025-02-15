package za.co.varl.trading.entities

data class OrderBook(
    val asks: List<Order>,
    val bids: List<Order>,
    val lastChange: String,
    val sequenceNumber: Long
)