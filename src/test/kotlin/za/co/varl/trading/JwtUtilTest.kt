package za.co.varl.trading

import io.jsonwebtoken.MalformedJwtException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import za.co.varl.trading.config.JwtUtil
import za.co.varl.trading.enums.Permission
import za.co.varl.trading.enums.Role

class JwtUtilTest {

    private val secretKey = "testSecret"
    private val jwtUtil = JwtUtil(secretKey)

    @Test
    fun `test generateToken`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        assertNotNull(token)
        assertFalse(jwtUtil.isTokenExpired(token))
    }

    @Test
    fun `test validateToken with valid token`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        assertTrue(jwtUtil.validateToken(token))
    }

    @Test
    fun `test validateToken with invalid token`() {
        val invalidToken = "invalidToken"
        assertFalse(jwtUtil.validateToken(invalidToken))
    }

    @Test
    fun `test extractUsername`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        assertEquals("testUser", jwtUtil.extractUsername(token))
    }

    @Test
    fun `test extractRole`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        assertEquals(Role.USER.name, jwtUtil.extractRole(token))
    }

    @Test
    fun `test extractEmail`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        assertEquals("test@example.com", jwtUtil.extractEmail(token))
    }

    @Test
    fun `test isTokenExpired`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        assertFalse(jwtUtil.isTokenExpired(token))
    }

    @Test
    fun `test extractPermissions for USER role`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        val permissions = jwtUtil.extractPermissions(token)
        assertEquals(
            listOf(Permission.VIEW.name, Permission.TRADE.name),
            permissions
        )
    }

    @Test
    fun `test extractPermissions for ADMIN role`() {
        val token = jwtUtil.generateToken("adminUser", Role.ADMIN, "admin@example.com")
        val permissions = jwtUtil.extractPermissions(token)
        assertEquals(
            listOf(Permission.VIEW.name, Permission.WITHDRAW.name, Permission.TRANSFER.name),
            permissions
        )
    }

    @Test
    fun `test validateToken handles exceptions gracefully`() {
        val token = jwtUtil.generateToken("testUser", Role.USER, "test@example.com")
        // Tamper with the token to simulate an exception
        val tamperedToken = token.substring(0, token.length - 1) + "x"
        assertFalse(jwtUtil.validateToken(tamperedToken))
    }

    @Test
    fun `test extractRole throws MalformedJwtException if token is invalid`() {
        val exception = assertThrows(MalformedJwtException::class.java) {
            jwtUtil.extractRole("invalidToken")
        }
        assertNotNull(exception.message) // Optionally check the message
    }

    @Test
    fun `test extractEmail throws MalformedJwtException if token is invalid`() {
        val exception = assertThrows(MalformedJwtException::class.java) {
            jwtUtil.extractEmail("invalidToken")
        }
        assertNotNull(exception.message) // Optionally check the message
    }
}