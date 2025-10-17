package com.sovats.lunch.config

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtClaimsHeaderFilter: GlobalFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {

        return exchange.getPrincipal<Authentication>()
            .flatMap { principal ->
                if (principal is JwtAuthenticationToken) {
                    val jwt: Jwt = principal.token
                    val userId = jwt.subject ?: jwt.claims["sub"]?.toString()

                    // Mutate the request to add the X-User-Id header
                    val mutatedRequest = exchange.request.mutate()
                        .header("X-User-Id", userId)
                        .build()

                    val mutatedExchange = exchange.mutate()
                        .request(mutatedRequest)
                        .build()

                    chain.filter(mutatedExchange)
                } else {
                    // No JWT authentication → skip header injection
                    chain.filter(exchange)
                }
            }
            .switchIfEmpty(chain.filter(exchange)) // In case there's no principal at all
    }

    override fun getOrder(): Int = -1 // ensure runs before routing
}
