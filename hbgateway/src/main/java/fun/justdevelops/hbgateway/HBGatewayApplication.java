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
                .route("hbauth", r -> r
                    .path("/api/auth/**")
                    .uri("lb://hbauth")
                )
                .build();
    }
}
