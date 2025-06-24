package com.usjchatapp.infrastructure.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtils {
    
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)
    
    @Value("\${spring.security.jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${spring.security.jwt.expiration}")
    private var jwtExpiration: Long = 0
    
    private fun getSigningKey(): Key {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
    
    fun generateToken(userDetails: UserDetails): String {
        return generateToken(mapOf(), userDetails)
    }
    
    fun generateToken(extraClaims: Map<String, Any>, userDetails: UserDetails): String {
        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }
    
    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }
    
    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }
    
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }
    
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }
    
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
    
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
            return true
        } catch (ex: SecurityException) {
            logger.error("Invalid JWT signature: {}", ex.message)
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", ex.message)
        } catch (ex: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", ex.message)
        } catch (ex: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", ex.message)
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", ex.message)
        }
        return false
    }
} 