package fun.justdevelops.hbmanagement.rest.dto;

import lombok.Data;

@Data
public class RoomResponse {
    private Long id;
    private Long hotelId;
    private int number;
    private boolean available;
    private int timesBooked;
}
