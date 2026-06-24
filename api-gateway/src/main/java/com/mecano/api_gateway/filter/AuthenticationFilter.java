package com.mecano.api_gateway.filter;

import com.mecano.api_gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final List<String> publicRoutes;

    public AuthenticationFilter(JwtUtil jwtUtil,
                                @Value("${gateway.public-routes}") String publicRoutes) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.publicRoutes = publicRoutes == null || publicRoutes.isBlank()
                ? List.of()
                : List.of(publicRoutes.split(","));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Routes publiques → passe directement
            if (isPublicRoute(path)) {
                return chain.filter(exchange);
            }

            // Vérification du header Authorization
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("❌ Requête sans token : {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.isTokenValid(token)) {
                log.warn("❌ Token invalide ou expiré pour : {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
                var claims = jwtUtil.extractAllClaims(token);
                // Transmettre le token JWT original aux services en aval
                // pour que leurs propres filtres JWT puissent valider l'authentification
                var mutatedExchange = exchange.mutate()
                        .request(r -> r
                                .header("Authorization", "Bearer " + token)
                                .header("X-User-Email", claims.getSubject())
                                .header("X-User-Role",
                                        claims.get("role", String.class))
                        ).build();
                log.info("✅ Token valide → {} [{}]",
                        claims.getSubject(),
                        claims.get("role", String.class));
                return chain.filter(mutatedExchange);
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicRoute(String path) {
        return publicRoutes.stream().anyMatch(route -> {
            if (route.endsWith("/**")) {
                String prefix = route.replace("/**", "");
                return path.startsWith(prefix);
            }
            return path.equals(route);
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}
