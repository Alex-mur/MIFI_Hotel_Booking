package fun.justdevelops.hbbooking.rest.dto;

import lombok.Data;

@Data
public class ConfirmResponse {
    Long lockId;
    boolean success;
    String message;
}
