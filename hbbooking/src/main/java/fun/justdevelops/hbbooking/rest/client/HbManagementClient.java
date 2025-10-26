package fun.justdevelops.hbbooking.rest.client;

import fun.justdevelops.hbbooking.rest.dto.ConfirmRequest;
import fun.justdevelops.hbbooking.rest.dto.ConfirmResponse;
import fun.justdevelops.hbbooking.rest.dto.ReleaseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${hb.services.management}")
public interface HbManagementClient {

    @PostMapping("/api/rooms/{id}/confirm")
    ConfirmResponse confirmRoomAvailability(@PathVariable Long id, @RequestBody ConfirmRequest request);

    @PostMapping("/api/rooms/{id}/release")
    void releaseRoom(@PathVariable Long id, @RequestBody ReleaseRequest request);
}
