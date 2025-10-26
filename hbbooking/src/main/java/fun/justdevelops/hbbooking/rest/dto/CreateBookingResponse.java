package fun.justdevelops.hbbooking.rest.dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreateBookingResponse {
    private Long bookingId;
    private Long roomId;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private LocalDateTime createdAt;
}
