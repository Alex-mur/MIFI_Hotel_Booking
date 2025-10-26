package fun.justdevelops.hbbooking.rest.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReleaseRequest {
    String requestId;

    public ReleaseRequest(String requestId) {
        this.requestId = requestId;
    }
}
