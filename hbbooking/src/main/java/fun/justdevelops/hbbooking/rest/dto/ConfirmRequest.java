package fun.justdevelops.hbbooking.rest.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConfirmRequest {
    String requestId;
    LocalDate dateStart;
    LocalDate dateEnd;

    public ConfirmRequest(String requestId, LocalDate dateStart, LocalDate dateEnd) {
        this.requestId = requestId;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }
}
