package fun.justdevelops.hbmanagement.rest.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConfirmRequest {
    String requestId;
    LocalDate dateStart;
    LocalDate dateEnd;
}
