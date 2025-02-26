package za.co.varl.trading.repository

import org.springframework.stereotype.Component
import za.co.varl.trading.entities.Order
import za.co.varl.trading.entities.Trade
import za.co.varl.trading.enums.OrderStatus
import za.co.varl.trading.enums.Side
import java.util.concurrent.ConcurrentHashMap

@Component
class OrderRepository {

    // Existing map for orders
    private val orders = ConcurrentHashMap<String, Order>()

    // New map for trades
    private val trades = ConcurrentHashMap<String, MutableList<Trade>>()

    // Method to save an order
    fun save(order: Order): Order {
        orders[order.id] = order
        return order
    }

    // Method to find an order by ID
    fun findById(id: String): Order? {
        return orders[id]
    }

    // Method to find all orders
    fun findAll(): List<Order> {
        return orders.values.toList()
    }

    // Method to delete an order by ID
    fun deleteById(id: String) {
        orders.remove(id)
    }


    // Method to find orders by pair and side
    fun findByPair(pair: String, side: Side): List<Order> {
        return orders.values.filter { it.pair == pair && it.side == side }
    }

    // Method to update quantity of an order
    fun updateQuantity(orderId: String, newQuantity: Double) {
        val order = orders[orderId]
        if (order != null) {
            order.quantity = newQuantity
            orders[orderId] = order
        }
    }

    // Method to add a trade
    fun addTrade(trade: Trade) {
        trades.computeIfAbsent(trade.currencyPair) { mutableListOf() }.add(trade)
    }

    // Method to find recent trades by pair
    fun findRecentTradesByPair(pair: String): List<Trade> {
        val tradesForPair = trades[pair] ?: return emptyList()
        return tradesForPair.sortedByDescending { it.tradedAt }.take(50)
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        val order = orders[orderId]
        if (order != null) {
            order.status = status // Assuming Order has a status field
            orders[orderId] = order
        }
    }

    fun findOpenOrdersByPair(pair: String): List<Order> {
        return orders.values.filter { it.pair == pair && (it.status == OrderStatus.PLACED || it.status == OrderStatus.ACTIVE || it.status == OrderStatus.PARTIALLY_FILLED) }
    }

    fun findOpenOrdersByCustomerId(customerOrderId: String): List<Order> {
        return orders.values.filter { it.customerOrderId == customerOrderId &&
                (it.status == OrderStatus.PLACED || it.status == OrderStatus.ACTIVE || it.status == OrderStatus.PARTIALLY_FILLED) }
    }

    fun findAllOrdersByCustomerId(customerOrderId: String): List<Order> {
        return orders.values.filter { it.customerOrderId == customerOrderId &&
                (it.status == OrderStatus.PLACED || it.status == OrderStatus.ACTIVE || it.status == OrderStatus.PARTIALLY_FILLED || it.status == OrderStatus.FILLED || it.status == OrderStatus.CANCELLED) }
    }




}