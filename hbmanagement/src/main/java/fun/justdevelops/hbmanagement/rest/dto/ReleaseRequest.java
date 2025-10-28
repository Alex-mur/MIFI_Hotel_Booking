package fun.justdevelops.hbmanagement.rest.dto;

import lombok.Data;

@Data
public class ReleaseRequest {
    String requestId;
    boolean isBooked;

    public ReleaseRequest(String requestId, boolean isBooked) {
        this.requestId = requestId;
        this.isBooked = isBooked;
    }
}
