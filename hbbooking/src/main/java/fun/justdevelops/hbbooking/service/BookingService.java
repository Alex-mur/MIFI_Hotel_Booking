package fun.justdevelops.hbbooking.service;


import fun.justdevelops.hbbooking.configuration.exception.RequestException;
import fun.justdevelops.hbbooking.model.entity.Booking;
import fun.justdevelops.hbbooking.model.entity.BookingStatus;
import fun.justdevelops.hbbooking.model.repo.BookingRepo;
import fun.justdevelops.hbbooking.rest.client.HbManagementClient;
import fun.justdevelops.hbbooking.rest.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final AuthenticationService authService;
    private final HbManagementClient managementClient;
    private final BookingRepo repo;

    @Autowired
    public BookingService(AuthenticationService authService, HbManagementClient managementClient, BookingRepo repo) {
        this.authService = authService;
        this.managementClient = managementClient;
        this.repo = repo;
    }

    @Transactional
    public CreateBookingResponse createBooking(CreateBookingRequest request) {

        // Проверяем наличие бронирований на указанные даты
        List<Booking> currentBookings = repo.findOverlappingBookings(request.getDateStart(), request.getDateEnd());
        if (!currentBookings.isEmpty()) throw new RuntimeException("Number id busy on the specified dates");

        // Создаем новое бронирование со статусом PENDING
        String requestId = UUID.randomUUID().toString();
        Long userId = authService.getCurrentUserId();
        Booking booking = new Booking(userId, request.getRoomId(), request.getDateStart(), request.getDateEnd(), BookingStatus.PENDING);
        booking = repo.save(booking);

        try {
            // Пробуем заблокировать
            ConfirmRequest confirmRequest = new ConfirmRequest(UUID.randomUUID().toString(), request.getDateStart(), request.getDateEnd());
            ConfirmResponse confirmResponse = managementClient.confirmRoomAvailability(request.getRoomId(), confirmRequest);
            if (!confirmResponse.isSuccess()) throw new RuntimeException("Can't block number for booking");

            // Если удалось заблокировать меняем статус бронирования
            booking.setStatus(BookingStatus.CONFIRMED);
            repo.save(booking);

            // Удаляем блокировку из management сервиса
            managementClient.releaseRoom(booking.getRoomId(), new ReleaseRequest(requestId));

            //Возвращаем ответ
            CreateBookingResponse response = new CreateBookingResponse();
            response.setBookingId(booking.getId());
            response.setCreatedAt(booking.getCreatedAt());
            response.setRoomId(booking.getRoomId());
            response.setDateStart(booking.getDateStart());
            response.setDateEnd(booking.getDateEnd());
            return response;

        } catch (Exception e) {
            // При любой ошибке удаляем лок и отменяем бронирование
            managementClient.releaseRoom(request.getRoomId(), new ReleaseRequest(requestId));
            booking.setStatus(BookingStatus.CANCELLED);
            repo.save(booking);
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Booking> getUserBookings() {
        long userId = authService.getCurrentUserId();
        return repo.findByUserId(userId);
    }

    public Booking getBookingById(Long bookingId) {
        long userId = authService.getCurrentUserId();
        Booking booking = repo.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getUserId() != userId) throw new AuthorizationDeniedException("You have no rights to delete this booking");
        return booking;
    }

    public void deleteBooking(Long bookingId) {
        long userId = authService.getCurrentUserId();
        Booking booking = repo.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getUserId() != userId) throw new AuthorizationDeniedException("You have no rights to delete this booking");
        booking.setStatus(BookingStatus.CANCELLED);
        repo.save(booking);
    }
}
