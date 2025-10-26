package fun.justdevelops.hbbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class HBBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(HBBookingApplication.class, args);
    }
}
