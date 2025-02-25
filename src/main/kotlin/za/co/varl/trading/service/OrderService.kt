package za.co.varl.trading.service

import za.co.varl.trading.entities.Order
import za.co.varl.trading.entities.OrderBook
import za.co.varl.trading.entities.Trade
import za.co.varl.trading.payload.request.CreateOrderRequest
import za.co.varl.trading.payload.request.UpdateOrderRequest

interface OrderService {
    fun createLimitOrder(request: CreateOrderRequest): Order
    fun getOrderById(id: String): Order?
    fun getAllOrders(): List<Order>
    fun matchOrders(pair: String)
    fun getOrderBook(pair: String): OrderBook
    fun getRecentTrades(pair: String): List<Trade>
    fun getOpenOrders(pair: String): List<Order>
    fun getOpenOrdersByCustomerId(customerOrderId: String): List<Order>
    fun modifyOrder(orderId: String, request: UpdateOrderRequest): Order?
}