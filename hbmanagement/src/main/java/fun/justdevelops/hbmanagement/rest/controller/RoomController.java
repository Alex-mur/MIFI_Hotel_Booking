package fun.justdevelops.hbmanagement.rest.controller;


import fun.justdevelops.hbmanagement.model.entity.Room;
import fun.justdevelops.hbmanagement.rest.dto.*;
import fun.justdevelops.hbmanagement.service.RoomService;
import jakarta.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping()
    public ResponseEntity<Room> createRoom(CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @GetMapping("/recommend/{id}")
    public ResponseEntity<List<Room>> recommendRooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getRecommendedHotelRooms(hotelId));
    }

    @GetMapping("/available/{id}")
    public ResponseEntity<List<Room>> getAvailableRooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getAvailableHotelRooms(hotelId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/update")
    public ResponseEntity<Room> updateRoom(UpdateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(request));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ConfirmResponse> confirmRoomAvailability(@PathVariable Long roomId, @RequestBody ConfirmRequest request) {
        return ResponseEntity.ok(roomService.confirmRoomAvailability(roomId, request));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseRoom(@RequestBody ReleaseRequest request) {
        roomService.releaseRoom(request.getRequestId());
        return ResponseEntity.ok().build();
    }
}
