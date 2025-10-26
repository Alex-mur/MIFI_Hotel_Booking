package fun.justdevelops.hbbooking.rest.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    private Long hotelId;
    private Long roomId;
    private boolean autoSelect;
    private LocalDate dateStart;
    private LocalDate dateEnd;
}
