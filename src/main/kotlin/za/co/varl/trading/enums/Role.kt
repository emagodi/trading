package za.co.varl.trading.enums

enum class Role(val permissions: List<Permission>) {
    USER(listOf(Permission.VIEW, Permission.TRADE)),
    ADMIN(listOf(Permission.VIEW, Permission.WITHDRAW, Permission.TRANSFER));
}