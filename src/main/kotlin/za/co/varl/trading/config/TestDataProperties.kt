package za.co.varl.trading.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "test.data")
class TestDataProperties {
    var userCount: Int = 100
    var orderCount: Int = 100
}