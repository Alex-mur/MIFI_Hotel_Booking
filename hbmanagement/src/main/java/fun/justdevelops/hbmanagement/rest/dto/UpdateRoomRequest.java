package fun.justdevelops.hbmanagement.rest.dto;

import fun.justdevelops.hbmanagement.model.entity.Hotel;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class UpdateRoomRequest {
    private Long id;
    private int number;
    private boolean available;
    private int timesBooked;
}
