package fun.justdevelops.hbmanagement.rest.dto;

import lombok.Data;

@Data
public class ReleaseRequest {
    String requestId;

    public ReleaseRequest(String requestId) {
        this.requestId = requestId;
    }
}
