package fun.justdevelops.hbbooking.rest.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    String username;
    String email;
    boolean enabled;
}
