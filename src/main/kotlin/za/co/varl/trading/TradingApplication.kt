package za.co.varl.trading

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TradingApplication

fun main(args: Array<String>) {
	runApplication<TradingApplication>(*args)
}
