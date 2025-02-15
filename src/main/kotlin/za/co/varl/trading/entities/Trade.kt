package za.co.varl.trading.entities

data class Trade(
    val price: String,
    val quantity: String,
    val currencyPair: String,
    val tradedAt: String,
    val takerSide: String,
    val sequenceId: Double,
    val id: String,
    val quoteVolume: String
)