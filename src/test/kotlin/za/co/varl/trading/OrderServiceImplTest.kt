import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import za.co.varl.trading.entities.Order
import za.co.varl.trading.entities.Trade
import za.co.varl.trading.enums.OrderStatus
import za.co.varl.trading.enums.Side
import za.co.varl.trading.enums.TimeInForce
import za.co.varl.trading.payload.request.CreateOrderRequest
import za.co.varl.trading.payload.request.UpdateOrderRequest
import za.co.varl.trading.repository.OrderRepository
import za.co.varl.trading.service.serviceImpl.OrderServiceImpl
import java.util.UUID

class OrderServiceImplTest {

    private val orderRepository: OrderRepository = mock()
    private val orderService = OrderServiceImpl(orderRepository)

    // Existing test
    @Test
    fun `test createLimitOrder`() {
        val request = CreateOrderRequest(
            side = Side.BUY,
            quantity = 10.0,
            price = 100.0,
            pair = "BTC/USD",
            customerOrderId = "cust123",
            timeInForce = TimeInForce.GTC
        )

        val expectedOrder = Order(
            id = UUID.randomUUID().toString(),
            side = request.side,
            quantity = request.quantity,
            price = request.price,
            pair = request.pair,
            customerOrderId = request.customerOrderId,
            timeInForce = request.timeInForce
        )

        whenever(orderRepository.save(any())).thenReturn(expectedOrder)

        val actualOrder = orderService.createLimitOrder(request)

        assertEquals(expectedOrder.side, actualOrder.side)
        assertEquals(expectedOrder.price, actualOrder.price)

        verify(orderRepository).save(any())
    }

    @Test
    fun `test getOrderById returns order`() {
        // Arrange
        val orderId = UUID.randomUUID().toString()
        val expectedOrder = Order(
            id = orderId,
            side = Side.BUY,
            quantity = 10.0,
            price = 100.0,
            pair = "BTC/USD",
            customerOrderId = "cust123",
            timeInForce = TimeInForce.GTC
        )

        whenever(orderRepository.findById(orderId)).thenReturn(expectedOrder)

        // Act
        val actualOrder = orderService.getOrderById(orderId)

        // Assert
        assertEquals(expectedOrder, actualOrder)
    }

    @Test
    fun `test getOrderById returns null when order not found`() {
        // Arrange
        val orderId = UUID.randomUUID().toString()
        whenever(orderRepository.findById(orderId)).thenReturn(null)

        // Act
        val actualOrder = orderService.getOrderById(orderId)

        // Assert
        assertNull(actualOrder)
    }

    @Test
    fun `test getAllOrders returns all orders`() {
        // Arrange
        val expectedOrders = listOf(
            Order(id = UUID.randomUUID().toString(), side = Side.BUY, quantity = 10.0, price = 100.0, pair = "BTC/USD", customerOrderId = "cust123", timeInForce = TimeInForce.GTC),
            Order(id = UUID.randomUUID().toString(), side = Side.SELL, quantity = 5.0, price = 200.0, pair = "BTC/USD", customerOrderId = "cust456", timeInForce = TimeInForce.GTC)
        )
        whenever(orderRepository.findAll()).thenReturn(expectedOrders)

        // Act
        val actualOrders = orderService.getAllOrders()

        // Assert
        assertEquals(expectedOrders, actualOrders)
    }

    @Test
    fun `test matchOrders correctly matches orders`() {
        // Arrange
        val buyOrder = Order(id = "buyOrderId", side = Side.BUY, quantity = 10.0, price = 200.0, pair = "BTC/USD", customerOrderId = "cust123", timeInForce = TimeInForce.GTC)
        val sellOrder = Order(id = "sellOrderId", side = Side.SELL, quantity = 5.0, price = 100.0, pair = "BTC/USD", customerOrderId = "cust456", timeInForce = TimeInForce.GTC)

        whenever(orderRepository.findByPair("BTC/USD", Side.BUY)).thenReturn(listOf(buyOrder))
        whenever(orderRepository.findByPair("BTC/USD", Side.SELL)).thenReturn(listOf(sellOrder))

        // Act
        orderService.matchOrders("BTC/USD")

        // Assert
        verify(orderRepository).addTrade(any())
        verify(orderRepository).updateQuantity(buyOrder.id, 5.0) // 10 - 5
        verify(orderRepository).updateStatus(buyOrder.id, OrderStatus.PARTIALLY_FILLED)
        verify(orderRepository).updateQuantity(sellOrder.id, 0.0) // 5 - 5
        verify(orderRepository).updateStatus(sellOrder.id, OrderStatus.FILLED)
    }

    @Test
    fun `test getOrderBook returns order book`() {
        // Arrange
        val asks = listOf(Order(id = "sellOrderId", side = Side.SELL, quantity = 5.0, price = 100.0, pair = "BTC/USD", customerOrderId = "cust456", timeInForce = TimeInForce.GTC))
        val bids = listOf(Order(id = "buyOrderId", side = Side.BUY, quantity = 10.0, price = 200.0, pair = "BTC/USD", customerOrderId = "cust123", timeInForce = TimeInForce.GTC))

        whenever(orderRepository.findByPair("BTC/USD", Side.SELL)).thenReturn(asks)
        whenever(orderRepository.findByPair("BTC/USD", Side.BUY)).thenReturn(bids)

        // Act
        val orderBook = orderService.getOrderBook("BTC/USD")

        // Assert
        assertEquals(asks, orderBook.asks)
        assertEquals(bids, orderBook.bids)
    }

    @Test
    fun `test getRecentTrades returns trades`() {
        // Arrange
        val expectedTrades = listOf(
            Trade(price = "100.0", quantity = "5.0", currencyPair = "BTC/USD", tradedAt = "2025-02-11T22:30:06.217Z", takerSide = "BUY", sequenceId = 1.0, id = UUID.randomUUID().toString(), quoteVolume = "500.0")
        )
        whenever(orderRepository.findRecentTradesByPair("BTC/USD")).thenReturn(expectedTrades)

        // Act
        val trades = orderService.getRecentTrades("BTC/USD")

        // Assert
        assertEquals(expectedTrades, trades)
    }

    @Test
    fun `test getOpenOrders returns open orders`() {
        // Arrange
        val expectedOrders = listOf(
            Order(id = UUID.randomUUID().toString(), side = Side.BUY, quantity = 10.0, price = 100.0, pair = "BTC/USD", customerOrderId = "cust123", timeInForce = TimeInForce.GTC)
        )
        whenever(orderRepository.findOpenOrdersByPair("BTC/USD")).thenReturn(expectedOrders)

        // Act
        val actualOrders = orderService.getOpenOrders("BTC/USD")

        // Assert
        assertEquals(expectedOrders, actualOrders)
    }

    @Test
    fun `test getOpenOrdersByCustomerId returns open orders`() {
        // Arrange
        val expectedOrders = listOf(
            Order(id = UUID.randomUUID().toString(), side = Side.BUY, quantity = 10.0, price = 100.0, pair = "BTC/USD", customerOrderId = "cust123", timeInForce = TimeInForce.GTC)
        )
        whenever(orderRepository.findOpenOrdersByCustomerId("cust123")).thenReturn(expectedOrders)

        // Act
        val actualOrders = orderService.getOpenOrdersByCustomerId("cust123")

        // Assert
        assertEquals(expectedOrders, actualOrders)
    }


    @Test
    fun `test modifyOrder cancels order if new total quantity is less than filled`() {
        // Arrange
        val orderId = UUID.randomUUID().toString()
        val existingOrder = Order(
            id = orderId,
            side = Side.BUY,
            quantity = 10.0,
            price = 100.0,
            pair = "BTC/USD",
            customerOrderId = "cust123",
            timeInForce = TimeInForce.GTC
        )
        whenever(orderRepository.findById(orderId)).thenReturn(existingOrder)

        val updateRequest = UpdateOrderRequest(
            newRemainingQuantity = null,
            newTotalQuantity = 5.0, // New total quantity is less than filled
            newPrice = null,
            customerOrderId = "cust123",
            modifyMatchStrategy = "CANCEL_ORIGINAL"
        )

        // Act
        val modifiedOrder = orderService.modifyOrder(orderId, updateRequest)

        // Assert
        assertNull(modifiedOrder) // Order should be cancelled
        verify(orderRepository).deleteById(orderId) // Ensure delete was called
    }

    @Test
    fun `test modifyOrder updates order successfully`() {
        // Arrange
        val orderId = UUID.randomUUID().toString()
        val existingOrder = Order(
            id = orderId,
            side = Side.BUY,
            quantity = 10.0,
            price = 100.0,
            pair = "BTC/USD",
            customerOrderId = "cust123",
            timeInForce = TimeInForce.GTC
        )
        whenever(orderRepository.findById(orderId)).thenReturn(existingOrder)

        val updateRequest = UpdateOrderRequest(
            newRemainingQuantity = 5.0,
            newTotalQuantity = 10.0,
            newPrice = 95.0,
            customerOrderId = "cust123",
            modifyMatchStrategy = "RETAIN_ORIGINAL"
        )

        // Act
        val modifiedOrder = orderService.modifyOrder(orderId, updateRequest)

        // Assert
        assertEquals(10.0, modifiedOrder?.quantity) // Verify quantity is updated
        assertEquals(95.0, modifiedOrder?.price) // Verify price is updated
        if (modifiedOrder != null) {
            verify(orderRepository).save(modifiedOrder)
        } // Ensure save was called
    }

}