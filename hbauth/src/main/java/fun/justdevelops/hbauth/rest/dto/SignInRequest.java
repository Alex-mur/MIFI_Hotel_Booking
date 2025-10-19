package fun.justdevelops.hbauth.rest.dto;

import lombok.Data;

@Data
public class SignInRequest {
    private String username;
    private String password;
}
