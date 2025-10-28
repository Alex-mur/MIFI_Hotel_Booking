import fun.justdevelops.hbmanagement.configuration.exception.RequestException;
import fun.justdevelops.hbmanagement.model.entity.Hotel;
import fun.justdevelops.hbmanagement.model.entity.ReservationLock;
import fun.justdevelops.hbmanagement.model.entity.Room;
import fun.justdevelops.hbmanagement.model.repo.HotelRepo;
import fun.justdevelops.hbmanagement.model.repo.ReservationLockRepo;
import fun.justdevelops.hbmanagement.model.repo.RoomRepo;
import fun.justdevelops.hbmanagement.rest.dto.ConfirmRequest;
import fun.justdevelops.hbmanagement.rest.dto.ConfirmResponse;
import fun.justdevelops.hbmanagement.rest.dto.CreateRoomRequest;
import fun.justdevelops.hbmanagement.rest.dto.UpdateRoomRequest;
import fun.justdevelops.hbmanagement.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock
    private RoomRepo roomRepo;
    @Mock
    private HotelRepo hotelRepo;
    @Mock
    private ReservationLockRepo reservationLockRepo;
    @InjectMocks
    private RoomService roomService;
    private Hotel testHotel;
    private Room testRoom;
    private ReservationLock testLock;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");

        testRoom = new Room(testHotel, 101);
        testRoom.setId(1L);
        testRoom.setAvailable(true);
        testRoom.setTimesBooked(5);

        testLock = new ReservationLock("test-request-id", testRoom, LocalDate.now(), LocalDate.now().plusDays(2));
        testLock.setId(1L);
    }

    @Test
    void getAvailableHotelRooms_ShouldReturnAvailableRooms() {
        Room unavailableRoom = new Room(testHotel, 102);
        unavailableRoom.setAvailable(false);

        when(roomRepo.findByHotelId(1L)).thenReturn(List.of(testRoom, unavailableRoom));

        List<Room> result = roomService.getAvailableHotelRooms(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
        verify(roomRepo).findByHotelId(1L);
    }

    @Test
    void getRecommendedHotelRooms_ShouldReturnSortedByTimesBooked() {
        Room rarelyBookedRoom = new Room(testHotel, 103);
        rarelyBookedRoom.setTimesBooked(1);

        when(roomRepo.findByHotelId(1L)).thenReturn(List.of(testRoom, rarelyBookedRoom));

        List<Room> result = roomService.getRecommendedHotelRooms(1L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getTimesBooked());
        assertEquals(5, result.get(1).getTimesBooked());
        verify(roomRepo).findByHotelId(1L);
    }

    @Test
    void createRoom_WithValidHotel_ShouldSaveRoom() {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setHotelId(1L);
        request.setNumber(201);

        when(hotelRepo.findById(1L)).thenReturn(Optional.of(testHotel));
        when(roomRepo.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.createRoom(request);

        assertNotNull(result);
        verify(hotelRepo).findById(1L);
        verify(roomRepo).save(any(Room.class));
    }

    @Test
    void createRoom_WithInvalidHotel_ShouldThrowException() {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setHotelId(999L);

        when(hotelRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RequestException.class, () -> roomService.createRoom(request));
        verify(hotelRepo).findById(999L);
        verify(roomRepo, never()).save(any());
    }

    @Test
    void updateRoom_WithValidData_ShouldUpdateRoom() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setId(1L);
        request.setNumber(101);
        request.setAvailable(false);
        request.setTimesBooked(10);

        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepo.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.updateRoom(request);

        assertEquals(101, result.getNumber());
        assertFalse(result.isAvailable());
        assertEquals(10, result.getTimesBooked());
        verify(roomRepo).findById(1L);
        verify(roomRepo).save(testRoom);
    }

    @Test
    void getRoomById_WithExistingId_ShouldReturnRoom() {
        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));

        Room result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(roomRepo).findById(1L);
    }

    @Test
    void getRoomById_WithNonExistingId_ShouldThrowException() {
        when(roomRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RequestException.class, () -> roomService.getRoomById(999L));
        verify(roomRepo).findById(999L);
    }

    @Test
    void confirmRoomAvailability_WithNoExistingLock_ShouldCreateNewLock() {
        ConfirmRequest request = new ConfirmRequest();
        request.setRequestId("new-request-id");
        request.setDateStart(LocalDate.now());
        request.setDateEnd(LocalDate.now().plusDays(3));

        when(reservationLockRepo.findByRequestId("new-request-id")).thenReturn(Optional.empty());
        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationLockRepo.findOverlappingLocks(any(), any(), any())).thenReturn(List.of());
        when(reservationLockRepo.save(any(ReservationLock.class))).thenReturn(testLock);

        ConfirmResponse response = roomService.confirmRoomAvailability(1L, request);

        assertTrue(response.isSuccess());
        assertEquals("Room booking locked for dates", response.getMessage());
        verify(reservationLockRepo).findByRequestId("new-request-id");
        verify(reservationLockRepo).findOverlappingLocks(1L, request.getDateStart(), request.getDateEnd());
        verify(reservationLockRepo).save(any(ReservationLock.class));
    }

    @Test
    void confirmRoomAvailability_WithExistingLock_ShouldReturnFailure() {
        ConfirmRequest request = new ConfirmRequest();
        request.setRequestId("existing-request-id");

        when(reservationLockRepo.findByRequestId("existing-request-id")).thenReturn(Optional.of(testLock));

        ConfirmResponse response = roomService.confirmRoomAvailability(1L, request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Lock for this request already created"));
        verify(reservationLockRepo, never()).save(any());
    }

    @Test
    void confirmRoomAvailability_WithOverlappingLock_ShouldReturnFailure() {
        ConfirmRequest request = new ConfirmRequest();
        request.setRequestId("new-request-id");
        request.setDateStart(LocalDate.now());
        request.setDateEnd(LocalDate.now().plusDays(3));

        when(reservationLockRepo.findByRequestId("new-request-id")).thenReturn(Optional.empty());
        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationLockRepo.findOverlappingLocks(any(), any(), any())).thenReturn(List.of(testLock));

        ConfirmResponse response = roomService.confirmRoomAvailability(1L, request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Dates are temporary locked by another booking"));
        verify(reservationLockRepo, never()).save(any());
    }

    @Test
    void releaseRoom_WithValidRequestId_ShouldDeleteLockAndUpdateRoom() {
        when(reservationLockRepo.findByRequestId("test-request-id")).thenReturn(Optional.of(testLock));
        when(roomRepo.save(any(Room.class))).thenReturn(testRoom);

        roomService.releaseRoom("test-request-id");

        verify(roomRepo).save(testRoom);
        verify(reservationLockRepo).deleteById(1L);
        assertEquals(6, testRoom.getTimesBooked());
    }

    @Test
    void releaseRoom_WithInvalidRequestId_ShouldThrowException() {
        when(reservationLockRepo.findByRequestId("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roomService.releaseRoom("invalid-id"));
        verify(roomRepo, never()).save(any());
        verify(reservationLockRepo, never()).deleteById(any());
    }
}