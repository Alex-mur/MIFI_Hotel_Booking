package fun.justdevelops.hbauth.rest.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String password;
    private String email;
}


