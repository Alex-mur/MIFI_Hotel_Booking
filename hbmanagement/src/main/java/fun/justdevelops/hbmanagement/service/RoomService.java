package fun.justdevelops.hbmanagement.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private final RoomRepo repo;
    private final HotelRepo hotelRepo;
    private final ReservationLockRepo reservationRepo;

    private final ConcurrentHashMap<Long, ConfirmRequest> blockedDates = new ConcurrentHashMap<>();

    @Autowired
    public RoomService(RoomRepo repo, HotelRepo hotelRepo, ReservationLockRepo reservationRepo) {
        this.repo = repo;
        this.hotelRepo = hotelRepo;
        this.reservationRepo = reservationRepo;
    }

    public List<Room> getAvailableHotelRooms(Long hotelId) {
        return repo.findByHotelId(hotelId).stream().filter(Room::isAvailable).toList();
    }

    public List<Room> getRecommendedHotelRooms(Long hotelId) {
        return repo.findByHotelId(hotelId).stream().sorted(Comparator.comparingInt(Room::getTimesBooked)).toList();
    }

    public Room createRoom(CreateRoomRequest request) {
        Hotel hotel = hotelRepo.findById(request.getHotelId()).orElseThrow(() -> new RequestException("Hotel not founded"));
        Room room = new Room(hotel, request.getNumber());
        return repo.save(room);
    }

    public Room updateRoom(UpdateRoomRequest request) {
        Room room = getRoomById(request.getId());
        room.setAvailable(request.isAvailable());
        room.setNumber(request.getNumber());
        room.setTimesBooked(request.getTimesBooked());
        return repo.save(room);
    }

    public Room getRoomById(Long roomId) {
        return repo.findById(roomId).orElseThrow(() -> new RequestException("Room not found"));
    }

    @Transactional
    public ConfirmResponse confirmRoomAvailability(Long roomId, ConfirmRequest request) {
        try {
            // Проверяем есть ли лок с таким requestId (на случай если запрос задублирован)
            var existentLock = reservationRepo.findByRequestId(request.getRequestId()).orElse(null);
            if (existentLock != null) {
                throw new RuntimeException("Lock for this request already created");
            }

            // Находим комнату с указанным id
            Room room = getRoomById(roomId);

            // Проверяем есть ли локи на запрошенные даты
            List<ReservationLock> currentLocks = reservationRepo
                    .findOverlappingLocks(roomId, request.getDateStart(), request.getDateEnd());

            // Если есть возвращаем отказ
            if (!currentLocks.isEmpty()) {
                throw new RuntimeException("Dates are temporary locked by another booking");
            }

            // Создаём лок
            ReservationLock lock = new ReservationLock(request.getRequestId(), room, request.getDateStart(), request.getDateEnd());
            var savedLock = reservationRepo.save(lock);

            //Возвращаем ответ с id созданного лока
            var response = new ConfirmResponse();
            response.setSuccess(true);
            response.setMessage("Room booking locked for dates");
            return response;

        } catch (Exception e) {
            var response = new ConfirmResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    // Удаление лока
    @Transactional
    public void releaseRoom(String requestId) {
        ReservationLock lock = reservationRepo.findByRequestId(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        Room room = lock.getRoom();
        room.setTimesBooked(room.getTimesBooked() + 1);
        repo.save(room);
        reservationRepo.deleteById(lock.getId());
    }
}
