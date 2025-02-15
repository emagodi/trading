package za.co.varl.trading.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.Date
import java.util.UUID

class JwtUtil(private val secretKey: String) {

    fun generateToken(username: String, role: String, email: String): String {
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role) // Include single role
            .claim("email", email) // Include email in the claims
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false // You might want to log the exception here
        }
    }

    fun extractUsername(token: String): String {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
    }

    fun extractRole(token: String): String {
        val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
        return claims["role"]?.toString() ?: throw IllegalArgumentException("Role claim not found")
    }

    fun extractEmail(token: String): String {
        val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
        return claims["email"]?.toString() ?: throw IllegalArgumentException("Email claim not found")
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration: Date = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.expiration
        return expiration.before(Date())
    }
}