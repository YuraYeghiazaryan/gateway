package com.sovats.lunch.config

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtClaimsHeaderFilter : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        // TODO
        val path = exchange.request.path.toString()

        // Skip public endpoints (login/signup/etc.)
        if (path.startsWith("/api/v1/auth/")) {
            return chain.filter(exchange)
        }

        val principalMono = exchange.getPrincipal<JwtAuthenticationToken>()

        return principalMono.flatMap { principal ->
            val jwt: Jwt = principal.token
            val userId = jwt.subject ?: jwt.claims["sub"]?.toString()

            val mutatedRequest = exchange.request.mutate()
                .header("X-User-Id", userId)
                .build()

            val mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build()

            chain.filter(mutatedExchange)
        }
    }

    override fun getOrder(): Int = -1 // ensure runs before routing
}
