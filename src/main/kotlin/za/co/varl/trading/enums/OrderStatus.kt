package za.co.varl.trading.enums

enum class OrderStatus {
    PLACED,

    FAILED,

    CANCELLED,

    FILLED,

    PARTIALLY_FILLED,

    ACTIVE,

    EXPIRED,

    PARTIALLY_FILLED_DUE_TO_SLIPPAGE,

    ORDER_MODIFIED
}