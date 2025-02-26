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
import za.co.varl.trading.service.OrderService
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class DataLoader(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val orderRepository: OrderRepository,
    @Autowired private val orderService: OrderService,
    @Autowired private val testDataProperties: TestDataProperties
) : CommandLineRunner {

    private val faker = Faker()

    override fun run(vararg args: String?) {
        createTestUsers(testDataProperties.userCount)
        createTestOrders(testDataProperties.orderCount)
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
                identityExpiryDate = faker.date().future(10 * 365, TimeUnit.DAYS).toString(),
                cellNumber = faker.phoneNumber().cellPhone(),
                email = faker.internet().emailAddress(),
                purpose = Purpose.TRADING,
                employmentStatus = EmploymentStatus.EMPLOYED_PART_TIME,
                sourceOfFunds = SourceOfFunds.ALLOWANCE
            )
            userRepository.save(user)
           // println("Test user created: ${user.email}")
        }
    }

    private fun createTestOrders(count: Int) {
        val priceRange = 1000.0..10000.0

        for (i in 1..count) {
            val side = if (i % 2 == 0) Side.BUY else Side.SELL
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

            orderRepository.save(order)
           // println("Test order created: ${order.id} with status: ${order.status}, side: ${order.side}, price: $price, quantity: $quantity")

            orderService.matchOrders(order.pair)
        }
    }
}