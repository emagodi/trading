package za.co.varl.trading.service.serviceImpl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import za.co.varl.trading.entities.Order
import za.co.varl.trading.entities.OrderBook
import za.co.varl.trading.entities.Trade
import za.co.varl.trading.enums.Side
import za.co.varl.trading.payload.request.CreateOrderRequest
import za.co.varl.trading.repository.OrderRepository
import za.co.varl.trading.service.OrderService
import za.co.varl.trading.enums.OrderStatus
import za.co.varl.trading.payload.request.UpdateOrderRequest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository
) : OrderService {

    private val logger = LoggerFactory.getLogger(OrderServiceImpl::class.java)

    override fun createLimitOrder(request: CreateOrderRequest): Order {
        val newOrder = Order(
            id = UUID.randomUUID().toString(),
            side = request.side,
            quantity = request.quantity,
            price = request.price,
            pair = request.pair,
            customerOrderId = request.customerOrderId,
            timeInForce = request.timeInForce
        )

        orderRepository.save(newOrder)
        matchOrders(newOrder.pair)

        return newOrder
    }

    override fun getOrderById(id: String): Order? {
        return orderRepository.findById(id)
    }

    override fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    override fun getOpenOrders(pair: String): List<Order> {
        return orderRepository.findOpenOrdersByPair(pair)
    }

    override fun getRecentTrades(pair: String): List<Trade> {
        return orderRepository.findRecentTradesByPair(pair)
    }

    override fun getOrderBook(pair: String): OrderBook {
        val asks = orderRepository.findByPair(pair, side = Side.SELL).sortedBy { it.price }
        val bids = orderRepository.findByPair(pair, side = Side.BUY).sortedByDescending { it.price }

        return OrderBook(
            asks = asks,
            bids = bids,
            lastChange = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            sequenceNumber = 6902150864
        )
    }

    override fun matchOrders(pair: String) {
        val buyOrders = orderRepository.findByPair(pair, side = Side.BUY).sortedByDescending { it.price }
        val sellOrders = orderRepository.findByPair(pair, side = Side.SELL).sortedBy { it.price }

        for (buyOrder in buyOrders) {
            for (sellOrder in sellOrders) {
                logger.info("Attempting to match: BUY(${buyOrder.id}, ${buyOrder.quantity}, ${buyOrder.price}) with SELL(${sellOrder.id}, ${sellOrder.quantity}, ${sellOrder.price})")

                if (buyOrder.price >= sellOrder.price && buyOrder.quantity > 0 && sellOrder.quantity > 0) {
                    val quantityMatched = minOf(buyOrder.quantity, sellOrder.quantity)

                    val trade = Trade(
                        price = sellOrder.price.toString(),
                        quantity = quantityMatched.toString(),
                        currencyPair = pair,
                        tradedAt = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                        takerSide = buyOrder.side.toString(),
                        sequenceId = System.nanoTime().toDouble(),
                        id = UUID.randomUUID().toString(),
                        quoteVolume = (sellOrder.price * quantityMatched).toString()
                    )

                    orderRepository.addTrade(trade)

                    val newBuyQuantity = (buyOrder.quantity - quantityMatched).toInt()
                    val newSellQuantity = (sellOrder.quantity - quantityMatched).toInt()

                    updateOrderStatus(buyOrder, newBuyQuantity)
                    updateOrderStatus(sellOrder, newSellQuantity)

                    logger.info("Matched $quantityMatched of ${buyOrder.id} with ${sellOrder.id}")

                    if (newBuyQuantity <= 0) break
                }
            }
        }
    }

    private fun updateOrderStatus(order: Order, newQuantity: Int) {
        if (newQuantity <= 0) {
            orderRepository.updateStatus(order.id, OrderStatus.FILLED)
        } else {
            orderRepository.updateStatus(order.id, OrderStatus.PARTIALLY_FILLED)
        }
        orderRepository.updateQuantity(order.id, newQuantity.toDouble())
    }

    override fun getOpenOrdersByCustomerId(customerOrderId: String): List<Order> {
        return orderRepository.findOpenOrdersByCustomerId(customerOrderId)
    }



    override fun modifyOrder(orderId: String, request: UpdateOrderRequest): Order? {
        val existingOrder = orderRepository.findById(orderId) ?: return null

        // Apply modifications
        request.newRemainingQuantity?.let {
            existingOrder.quantity = it // Update remaining quantity directly
        }

        // Calculate filled quantity after updating newRemainingQuantity
        val filledQuantity = existingOrder.quantity - (request.newRemainingQuantity ?: 0.0)

        request.newTotalQuantity?.let {
            // Check if the new total quantity can be set
            if (it < filledQuantity) {
                // Cancel order if new total quantity is less than filled
                orderRepository.deleteById(orderId)
                return null
            } else {
                existingOrder.quantity = it // Update total quantity if valid
            }
        }

        request.newPrice?.let {
            existingOrder.price = it // Update price
        }

        // Save the modified order
        orderRepository.save(existingOrder)

        // Log modification
        logger.info("Order modified: $existingOrder")
        return existingOrder // Return the modified order
    }

}