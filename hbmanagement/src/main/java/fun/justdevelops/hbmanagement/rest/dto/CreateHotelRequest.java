package fun.justdevelops.hbmanagement.rest.dto;

import lombok.Data;

@Data
public class CreateHotelRequest {
    private String name;
    private String address;
}
