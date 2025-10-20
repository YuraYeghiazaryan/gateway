package com.sovats.lunch.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private val jwtSecret: String
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers(
                    "/api/v1/auth/**",      // public Auth endpoints
                ).permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }
            .build()
    }

    @Bean
    fun jwtDecoder(): NimbusReactiveJwtDecoder {
        val key = SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
        return NimbusReactiveJwtDecoder.withSecretKey(key).build()
    }
}
