import fun.justdevelops.hbbooking.model.entity.Booking;
import fun.justdevelops.hbbooking.model.entity.BookingStatus;
import fun.justdevelops.hbbooking.model.repo.BookingRepo;
import fun.justdevelops.hbbooking.rest.client.HbManagementClient;
import fun.justdevelops.hbbooking.rest.dto.*;
import fun.justdevelops.hbbooking.service.AuthenticationService;
import fun.justdevelops.hbbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private AuthenticationService authService;
    @Mock
    private HbManagementClient managementClient;
    @Mock
    private BookingRepo repo;
    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_Success() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(1L);
        request.setDateStart(LocalDate.now().plusDays(1));
        request.setDateEnd(LocalDate.now().plusDays(3));

        Long userId = 123L;
        Booking savedBooking = new Booking(userId, request.getRoomId(), request.getDateStart(), request.getDateEnd(), BookingStatus.PENDING);
        savedBooking.setId(1L);
        savedBooking.setCreatedAt(LocalDateTime.now());

        ConfirmResponse confirmResponse = new ConfirmResponse();
        confirmResponse.setSuccess(true);

        when(repo.findOverlappingBookings(request.getDateStart(), request.getDateEnd())).thenReturn(Collections.emptyList());
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(repo.save(any(Booking.class))).thenReturn(savedBooking);
        when(managementClient.confirmRoomAvailability(eq(request.getRoomId()), any(ConfirmRequest.class))).thenReturn(confirmResponse);

        CreateBookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(savedBooking.getId(), response.getBookingId());
        assertEquals(savedBooking.getRoomId(), response.getRoomId());
        assertEquals(savedBooking.getDateStart(), response.getDateStart());
        assertEquals(savedBooking.getDateEnd(), response.getDateEnd());
        assertEquals(savedBooking.getCreatedAt(), response.getCreatedAt());

        verify(repo, times(1)).findOverlappingBookings(request.getDateStart(), request.getDateEnd());
        verify(repo, times(2)).save(any(Booking.class));
        verify(managementClient, times(1)).confirmRoomAvailability(eq(request.getRoomId()), any(ConfirmRequest.class));
        verify(managementClient, times(1)).releaseRoom(eq(request.getRoomId()), any(ReleaseRequest.class));

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(repo, times(2)).save(bookingCaptor.capture());
        List<Booking> savedBookings = bookingCaptor.getAllValues();
        assertEquals(BookingStatus.CONFIRMED, savedBookings.get(1).getStatus());
    }

    @Test
    void createBooking_WithOverlappingBookings_ShouldThrowException() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(1L);
        request.setDateStart(LocalDate.now().plusDays(1));
        request.setDateEnd(LocalDate.now().plusDays(3));

        Booking overlappingBooking = new Booking(456L, 1L, request.getDateStart(), request.getDateEnd(), BookingStatus.CONFIRMED);

        when(repo.findOverlappingBookings(request.getDateStart(), request.getDateEnd())).thenReturn(List.of(overlappingBooking));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));

        assertEquals("Number id busy on the specified dates", exception.getMessage());
        verify(repo, never()).save(any(Booking.class));
        verify(managementClient, never()).confirmRoomAvailability(anyLong(), any(ConfirmRequest.class));
    }

    @Test
    void createBooking_WhenManagementClientFails_ShouldCancelBooking() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(1L);
        request.setDateStart(LocalDate.now().plusDays(1));
        request.setDateEnd(LocalDate.now().plusDays(3));

        Long userId = 123L;
        Booking savedBooking = new Booking(userId, request.getRoomId(), request.getDateStart(), request.getDateEnd(), BookingStatus.PENDING);
        savedBooking.setId(1L);

        ConfirmResponse confirmResponse = new ConfirmResponse();
        confirmResponse.setSuccess(false);

        when(repo.findOverlappingBookings(request.getDateStart(), request.getDateEnd())).thenReturn(Collections.emptyList());
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(repo.save(any(Booking.class))).thenReturn(savedBooking);
        when(managementClient.confirmRoomAvailability(eq(request.getRoomId()), any(ConfirmRequest.class))).thenReturn(confirmResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));

        assertEquals("Can't block number for booking", exception.getMessage());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(repo, times(2)).save(bookingCaptor.capture());
        List<Booking> savedBookings = bookingCaptor.getAllValues();
        assertEquals(BookingStatus.CANCELLED, savedBookings.get(1).getStatus());

        verify(managementClient, times(1)).releaseRoom(eq(request.getRoomId()), any(ReleaseRequest.class));
    }

    @Test
    void createBooking_WhenManagementClientThrowsException_ShouldCancelBooking() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(1L);
        request.setDateStart(LocalDate.now().plusDays(1));
        request.setDateEnd(LocalDate.now().plusDays(3));

        Long userId = 123L;
        Booking savedBooking = new Booking(userId, request.getRoomId(), request.getDateStart(), request.getDateEnd(), BookingStatus.PENDING);
        savedBooking.setId(1L);

        when(repo.findOverlappingBookings(request.getDateStart(), request.getDateEnd())).thenReturn(Collections.emptyList());
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(repo.save(any(Booking.class))).thenReturn(savedBooking);
        when(managementClient.confirmRoomAvailability(eq(request.getRoomId()), any(ConfirmRequest.class))).thenThrow(new RuntimeException("Service unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));

        assertEquals("Service unavailable", exception.getMessage());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(repo, times(2)).save(bookingCaptor.capture());
        List<Booking> savedBookings = bookingCaptor.getAllValues();
        assertEquals(BookingStatus.CANCELLED, savedBookings.get(1).getStatus());

        verify(managementClient, times(1)).releaseRoom(eq(request.getRoomId()), any(ReleaseRequest.class));
    }

    @Test
    void getUserBookings_ShouldReturnUserBookings() {
        Long userId = 123L;
        List<Booking> expectedBookings = List.of(new Booking(userId, 1L, LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.CONFIRMED), new Booking(userId, 2L, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), BookingStatus.PENDING));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(repo.findByUserId(userId)).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getUserBookings();

        assertEquals(expectedBookings, result);
        verify(authService, times(1)).getCurrentUserId();
        verify(repo, times(1)).findByUserId(userId);
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenUserIsOwner() {
        Long userId = 123L;
        Long bookingId = 1L;
        Booking expectedBooking = new Booking(userId, 1L, LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.CONFIRMED);
        expectedBooking.setId(bookingId);

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(repo.findById(bookingId)).thenReturn(Optional.of(expectedBooking));

        Booking result = bookingService.getBookingById(bookingId);

        assertEquals(expectedBooking, result);
        verify(authService, times(1)).getCurrentUserId();
        verify(repo, times(1)).findById(bookingId);
    }

    @Test
    void getBookingById_WhenBookingNotFound_ShouldThrowException() {
        Long bookingId = 999L;
        when(authService.getCurrentUserId()).thenReturn(123L);
        when(repo.findById(bookingId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookingService.getBookingById(bookingId));

        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void getBookingById_WhenUserIsNotOwner_ShouldThrowAuthorizationException() {
        Long currentUserId = 123L;
        Long bookingOwnerId = 456L;
        Long bookingId = 1L;
        Booking booking = new Booking(bookingOwnerId, 1L, LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.CONFIRMED);
        booking.setId(bookingId);

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(repo.findById(bookingId)).thenReturn(Optional.of(booking));

        AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class, () -> bookingService.getBookingById(bookingId));

        assertEquals("You have no rights to delete this booking", exception.getMessage());
    }

    @Test
    void deleteBooking_Success() {
        Long currentUserId = 123L;
        Long bookingId = 1L;
        Booking booking = new Booking(currentUserId, 1L, LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.CONFIRMED);
        booking.setId(bookingId);

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(repo.findById(bookingId)).thenReturn(Optional.of(booking));
        when(repo.save(any(Booking.class))).thenReturn(booking);

        bookingService.deleteBooking(bookingId);

        verify(authService, times(1)).getCurrentUserId();
        verify(repo, times(1)).findById(bookingId);
        verify(repo, times(1)).save(booking);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void deleteBooking_WhenBookingNotFound_ShouldThrowException() {
        Long bookingId = 999L;
        when(authService.getCurrentUserId()).thenReturn(123L);
        when(repo.findById(bookingId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookingService.deleteBooking(bookingId));

        assertEquals("Booking not found", exception.getMessage());
        verify(repo, never()).save(any(Booking.class));
    }

    @Test
    void deleteBooking_WhenUserIsNotOwner_ShouldThrowAuthorizationException() {
        Long currentUserId = 123L;
        Long bookingOwnerId = 456L;
        Long bookingId = 1L;
        Booking booking = new Booking(bookingOwnerId, 1L, LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.CONFIRMED);
        booking.setId(bookingId);

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(repo.findById(bookingId)).thenReturn(Optional.of(booking));

        AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class, () -> bookingService.deleteBooking(bookingId));

        assertEquals("You have no rights to delete this booking", exception.getMessage());
        verify(repo, never()).save(any(Booking.class));
    }
}