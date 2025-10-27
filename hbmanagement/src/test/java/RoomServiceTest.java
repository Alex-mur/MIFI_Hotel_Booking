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

    @Test
    void getAvailableHotelRooms_ShouldReturnOnlyAvailableRooms() {
        Long hotelId = 1L;
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        hotel.setId(hotelId);

        List<Room> allRooms = List.of(createRoom(1L, hotel, 101, true, 5), createRoom(2L, hotel, 102, false, 3), createRoom(3L, hotel, 103, true, 10));

        when(roomRepo.findByHotelId(hotelId)).thenReturn(allRooms);

        List<Room> result = roomService.getAvailableHotelRooms(hotelId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Room::isAvailable));
        verify(roomRepo, times(1)).findByHotelId(hotelId);
    }

    @Test
    void getAvailableHotelRooms_ShouldReturnEmptyList_WhenNoAvailableRooms() {
        Long hotelId = 1L;
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        hotel.setId(hotelId);

        List<Room> allRooms = List.of(createRoom(1L, hotel, 101, false, 5), createRoom(2L, hotel, 102, false, 3));

        when(roomRepo.findByHotelId(hotelId)).thenReturn(allRooms);

        List<Room> result = roomService.getAvailableHotelRooms(hotelId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomRepo, times(1)).findByHotelId(hotelId);
    }

    @Test
    void getRecommendedHotelRooms_ShouldReturnRoomsSortedByTimesBooked() {
        Long hotelId = 1L;
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        hotel.setId(hotelId);

        List<Room> allRooms = List.of(createRoom(1L, hotel, 101, true, 15), createRoom(2L, hotel, 102, true, 5), createRoom(3L, hotel, 103, true, 10));

        when(roomRepo.findByHotelId(hotelId)).thenReturn(allRooms);

        List<Room> result = roomService.getRecommendedHotelRooms(hotelId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getTimesBooked());
        assertEquals(10, result.get(1).getTimesBooked());
        assertEquals(15, result.get(2).getTimesBooked());
        verify(roomRepo, times(1)).findByHotelId(hotelId);
    }

    @Test
    void createRoom_ShouldSaveRoom_WhenHotelExists() {
        Long hotelId = 1L;
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        hotel.setId(hotelId);

        CreateRoomRequest request = new CreateRoomRequest();
        request.setHotelId(hotelId);
        request.setNumber(101);

        Room expectedRoom = new Room(hotel, 101);
        expectedRoom.setId(1L);

        when(hotelRepo.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomRepo.save(any(Room.class))).thenReturn(expectedRoom);

        Room result = roomService.createRoom(request);

        assertNotNull(result);
        assertEquals(expectedRoom.getId(), result.getId());
        assertEquals(hotel, result.getHotel());
        assertEquals(101, result.getNumber());

        verify(hotelRepo, times(1)).findById(hotelId);
        verify(roomRepo, times(1)).save(any(Room.class));
    }

    @Test
    void createRoom_ShouldThrowException_WhenHotelNotFound() {
        Long nonExistentHotelId = 999L;
        CreateRoomRequest request = new CreateRoomRequest();
        request.setHotelId(nonExistentHotelId);
        request.setNumber(101);

        when(hotelRepo.findById(nonExistentHotelId)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> roomService.createRoom(request));

        assertEquals("Hotel not founded", exception.getMessage());
        verify(hotelRepo, times(1)).findById(nonExistentHotelId);
        verify(roomRepo, never()).save(any(Room.class));
    }

    @Test
    void updateRoom_ShouldUpdateRoom_WhenRoomExists() {
        Long roomId = 1L;
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        Room existingRoom = createRoom(roomId, hotel, 101, true, 5);

        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setId(roomId);
        request.setNumber(102);
        request.setAvailable(false);
        request.setTimesBooked(10);

        when(roomRepo.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(roomRepo.save(existingRoom)).thenReturn(existingRoom);

        Room result = roomService.updateRoom(request);

        assertNotNull(result);
        assertEquals(102, result.getNumber());
        assertFalse(result.isAvailable());
        assertEquals(10, result.getTimesBooked());

        verify(roomRepo, times(1)).findById(roomId);
        verify(roomRepo, times(1)).save(existingRoom);
    }

    @Test
    void updateRoom_ShouldThrowException_WhenRoomNotFound() {
        Long nonExistentRoomId = 999L;
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setId(nonExistentRoomId);

        when(roomRepo.findById(nonExistentRoomId)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> roomService.updateRoom(request));

        assertEquals("Room not found", exception.getMessage());
        verify(roomRepo, times(1)).findById(nonExistentRoomId);
        verify(roomRepo, never()).save(any(Room.class));
    }

    @Test
    void getRoomById_ShouldReturnRoom_WhenRoomExists() {
        Long roomId = 1L;
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        Room expectedRoom = createRoom(roomId, hotel, 101, true, 5);

        when(roomRepo.findById(roomId)).thenReturn(Optional.of(expectedRoom));

        Room result = roomService.getRoomById(roomId);

        assertNotNull(result);
        assertEquals(expectedRoom, result);
        verify(roomRepo, times(1)).findById(roomId);
    }

    @Test
    void getRoomById_ShouldThrowException_WhenRoomNotFound() {
        Long nonExistentRoomId = 999L;
        when(roomRepo.findById(nonExistentRoomId)).thenReturn(Optional.empty());

        RequestException exception = assertThrows(RequestException.class, () -> roomService.getRoomById(nonExistentRoomId));

        assertEquals("Room not found", exception.getMessage());
        verify(roomRepo, times(1)).findById(nonExistentRoomId);
    }

    @Test
    void confirmRoomAvailability_ShouldSuccess_WhenNoConflicts() {
        Long roomId = 1L;
        String requestId = "test-request-id";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        ConfirmRequest confirmRequest = new ConfirmRequest(requestId, startDate, endDate);
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        Room room = createRoom(roomId, hotel, 101, true, 5);
        ReservationLock lock = new ReservationLock(requestId, room, startDate, endDate);
        lock.setId(1L);

        when(reservationLockRepo.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));
        when(reservationLockRepo.findOverlappingLocks(roomId, startDate, endDate)).thenReturn(List.of());
        when(reservationLockRepo.save(any(ReservationLock.class))).thenReturn(lock);

        ConfirmResponse response = roomService.confirmRoomAvailability(roomId, confirmRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Room booking locked for dates", response.getMessage());

        verify(reservationLockRepo, times(1)).findByRequestId(requestId);
        verify(roomRepo, times(1)).findById(roomId);
        verify(reservationLockRepo, times(1)).findOverlappingLocks(roomId, startDate, endDate);
        verify(reservationLockRepo, times(1)).save(any(ReservationLock.class));
    }

    @Test
    void confirmRoomAvailability_ShouldFail_WhenLockAlreadyExists() {
        Long roomId = 1L;
        String requestId = "duplicate-request-id";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        ConfirmRequest confirmRequest = new ConfirmRequest(requestId, startDate, endDate);
        ReservationLock existingLock = new ReservationLock();

        when(reservationLockRepo.findByRequestId(requestId)).thenReturn(Optional.of(existingLock));

        ConfirmResponse response = roomService.confirmRoomAvailability(roomId, confirmRequest);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Lock for this request already created", response.getMessage());

        verify(reservationLockRepo, times(1)).findByRequestId(requestId);
        verify(roomRepo, never()).findById(anyLong());
        verify(reservationLockRepo, never()).findOverlappingLocks(anyLong(), any(), any());
        verify(reservationLockRepo, never()).save(any(ReservationLock.class));
    }

    @Test
    void confirmRoomAvailability_ShouldFail_WhenRoomNotFound() {
        Long nonExistentRoomId = 999L;
        String requestId = "test-request-id";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        ConfirmRequest confirmRequest = new ConfirmRequest(requestId, startDate, endDate);

        when(reservationLockRepo.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(roomRepo.findById(nonExistentRoomId)).thenReturn(Optional.empty());

        ConfirmResponse response = roomService.confirmRoomAvailability(nonExistentRoomId, confirmRequest);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Room not found"));

        verify(reservationLockRepo, times(1)).findByRequestId(requestId);
        verify(roomRepo, times(1)).findById(nonExistentRoomId);
        verify(reservationLockRepo, never()).findOverlappingLocks(anyLong(), any(), any());
        verify(reservationLockRepo, never()).save(any(ReservationLock.class));
    }

    @Test
    void confirmRoomAvailability_ShouldFail_WhenOverlappingLocksExist() {
        Long roomId = 1L;
        String requestId = "test-request-id";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        ConfirmRequest confirmRequest = new ConfirmRequest(requestId, startDate, endDate);
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        Room room = createRoom(roomId, hotel, 101, true, 5);
        ReservationLock overlappingLock = new ReservationLock();

        when(reservationLockRepo.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));
        when(reservationLockRepo.findOverlappingLocks(roomId, startDate, endDate)).thenReturn(List.of(overlappingLock));

        ConfirmResponse response = roomService.confirmRoomAvailability(roomId, confirmRequest);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Dates are temporary locked by another booking", response.getMessage());

        verify(reservationLockRepo, times(1)).findByRequestId(requestId);
        verify(roomRepo, times(1)).findById(roomId);
        verify(reservationLockRepo, times(1)).findOverlappingLocks(roomId, startDate, endDate);
        verify(reservationLockRepo, never()).save(any(ReservationLock.class));
    }

    @Test
    void confirmRoomAvailability_ShouldHandleExceptionDuringLockCreation() {
        Long roomId = 1L;
        String requestId = "test-request-id";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        ConfirmRequest confirmRequest = new ConfirmRequest(requestId, startDate, endDate);
        Hotel hotel = new Hotel("Test Hotel", "Test Address");
        Room room = createRoom(roomId, hotel, 101, true, 5);

        when(reservationLockRepo.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));
        when(reservationLockRepo.findOverlappingLocks(roomId, startDate, endDate)).thenReturn(List.of());
        when(reservationLockRepo.save(any(ReservationLock.class))).thenThrow(new RuntimeException("Database connection failed"));

        ConfirmResponse response = roomService.confirmRoomAvailability(roomId, confirmRequest);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Database connection failed", response.getMessage());

        verify(reservationLockRepo, times(1)).findByRequestId(requestId);
        verify(roomRepo, times(1)).findById(roomId);
        verify(reservationLockRepo, times(1)).findOverlappingLocks(roomId, startDate, endDate);
        verify(reservationLockRepo, times(1)).save(any(ReservationLock.class));
    }

    @Test
    void releaseRoom_ShouldDeleteLockByRequestId() {
        String requestId = "test-request-id";
        doNothing().when(reservationLockRepo).deleteByRequestId(requestId);

        roomService.releaseRoom(requestId);

        verify(reservationLockRepo, times(1)).deleteByRequestId(requestId);
    }

    @Test
    void releaseRoom_ShouldHandleNullRequestId() {
        String nullRequestId = null;
        doNothing().when(reservationLockRepo).deleteByRequestId(nullRequestId);

        roomService.releaseRoom(nullRequestId);

        verify(reservationLockRepo, times(1)).deleteByRequestId(nullRequestId);
    }

    private Room createRoom(Long id, Hotel hotel, int number, boolean available, int timesBooked) {
        Room room = new Room(hotel, number);
        room.setId(id);
        room.setAvailable(available);
        room.setTimesBooked(timesBooked);
        return room;
    }
}