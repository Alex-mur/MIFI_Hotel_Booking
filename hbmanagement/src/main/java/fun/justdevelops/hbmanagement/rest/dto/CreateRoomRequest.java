package fun.justdevelops.hbmanagement.rest.dto;

import lombok.Data;

@Data
public class CreateRoomRequest {
    private Long hotelId;
    private int number;
}
