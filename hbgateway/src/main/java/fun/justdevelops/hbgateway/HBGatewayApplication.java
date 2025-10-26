package fun.justdevelops.hbgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HBGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(HBGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r
                    .path("/api/auth/**")
                    .uri("lb://hbbooking")
                )
                .route("user", r -> r
                        .path("/api/user/**")
                        .uri("lb://hbbooking")
                )
                .route("booking", r -> r
                        .path("/api/booking/**")
                        .uri("lb://hbbooking")
                )
                .route("hotel", r -> r
                        .path("/api/hotel/**")
                        .uri("lb://hbmanagement")
                )
                .route("room", r -> r
                        .path(
                                "/api/room",
                                "/api/room/recommend/**",
                                "/api/room/available/**",
                                "/api/room/update"
                        )
                        .uri("lb://hbmanagement")
                )
                .build();
    }
}
