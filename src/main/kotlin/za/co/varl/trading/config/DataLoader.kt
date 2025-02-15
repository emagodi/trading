package za.co.varl.trading.config

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import za.co.varl.trading.entities.Order
import za.co.varl.trading.entities.User
import za.co.varl.trading.enums.OrderStatus
import za.co.varl.trading.enums.Role
import za.co.varl.trading.enums.Side
import za.co.varl.trading.enums.TimeInForce
import za.co.varl.trading.repository.OrderRepository
import za.co.varl.trading.repository.UserRepository
import za.co.varl.trading.enums.EmploymentStatus
import za.co.varl.trading.enums.Purpose
import za.co.varl.trading.enums.SourceOfFunds
import za.co.varl.trading.service.OrderService // Import the OrderService
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class DataLoader : CommandLineRunner {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderService: OrderService // Inject the OrderService

    private val faker = Faker()

    override fun run(vararg args: String?) {
        createTestUsers(100)  // Create 100 test users
        createTestOrders(100) // Create 100 test orders
    }

    private fun createTestUsers(count: Int) {
        for (i in 1..count) {
            val user = User(
                id = i.toLong(),
                password = "Password@123",
                role = Role.USER,
                firstName = faker.name().firstName(),
                lastName = faker.name().lastName(),
                dateOfBirth = faker.date().birthday().toString(),
                residentialCountry = faker.address().country(),
                identityIssuingCountry = faker.address().country(),
                identityType = "Passport",
                identityNumber = faker.idNumber().valid(),
                identityExpiryDate = faker.date().future(10 * 365, TimeUnit.DAYS).toString(), // 10 years in days
                cellNumber = faker.phoneNumber().cellPhone(),
                email = faker.internet().emailAddress(),
                purpose = Purpose.TRADING,
                employmentStatus = EmploymentStatus.EMPLOYED_PART_TIME,
                sourceOfFunds = SourceOfFunds.ALLOWANCE
            )
            userRepository.save(user)
            println("Test users created: ${user.email}")
        }
    }

    private fun createTestOrders(count: Int) {
        val priceRange = 1000.0..10000.0

        for (i in 1..count) {
            val side = if (i % 2 == 0) Side.BUY else Side.SELL // Alternate between BUY and SELL
            val price = faker.number().randomDouble(2, priceRange.start.toInt(), priceRange.endInclusive.toInt())
            val quantity = faker.number().randomDouble(2, 1, 100)
            val order = Order(
                id = UUID.randomUUID().toString(),
                side = side,
                quantity = quantity,
                price = price,
                pair = "BTCZAR",
                customerOrderId = faker.idNumber().valid(),
                timeInForce = TimeInForce.GTC,
                status = OrderStatus.PLACED
            )

            // Save the order
            orderRepository.save(order)
            println("Test orders created: ${order.id} with status: ${order.status}, side: ${order.side}, price: $price, quantity: $quantity")

            // Call the matching logic after each new order is created
            orderService.matchOrders(order.pair)
        }
    }
}