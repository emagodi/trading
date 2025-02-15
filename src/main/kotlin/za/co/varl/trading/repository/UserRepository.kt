package za.co.varl.trading.repository

import org.springframework.stereotype.Repository
import za.co.varl.trading.entities.User
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserRepository {
    private val userStorage = ConcurrentHashMap<String, User>()

    fun save(user: User): User {
        userStorage[user.email] = user
        return user
    }

    fun findByEmail(email: String): User? {
        return userStorage[email]
    }

    fun existsByEmail(email: String): Boolean {
        return userStorage.containsKey(email)
    }

    fun getAllUsers(): Collection<User> {
        return userStorage.values
    }
}