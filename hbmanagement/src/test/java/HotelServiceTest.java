import fun.justdevelops.hbmanagement.model.entity.Hotel;
import fun.justdevelops.hbmanagement.model.repo.HotelRepo;
import fun.justdevelops.hbmanagement.rest.dto.CreateHotelRequest;
import fun.justdevelops.hbmanagement.service.HotelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepo hotelRepo;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void create_ShouldSaveAndReturnHotel_WhenValidRequest() {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("Grand Hotel");
        request.setAddress("123 Main Street, City");
        Hotel expectedHotel = new Hotel("Grand Hotel", "123 Main Street, City");
        expectedHotel.setId(1L);
        when(hotelRepo.save(any(Hotel.class))).thenReturn(expectedHotel);
        Hotel result = hotelService.create(request);
        assertNotNull(result);
        assertEquals(expectedHotel.getId(), result.getId());
        assertEquals(expectedHotel.getName(), result.getName());
        assertEquals(expectedHotel.getAddress(), result.getAddress());
        ArgumentCaptor<Hotel> hotelCaptor = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepo, times(1)).save(hotelCaptor.capture());
        Hotel capturedHotel = hotelCaptor.getValue();
        assertEquals(request.getName(), capturedHotel.getName());
        assertEquals(request.getAddress(), capturedHotel.getAddress());
    }

    @Test
    void create_ShouldHandleEmptyNameAndAddress() {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("");
        request.setAddress("");

        Hotel savedHotel = new Hotel("", "");
        savedHotel.setId(1L);

        when(hotelRepo.save(any(Hotel.class))).thenReturn(savedHotel);

        Hotel result = hotelService.create(request);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getAddress());
        verify(hotelRepo, times(1)).save(any(Hotel.class));
    }

    @Test
    void create_ShouldHandleNullRequest() {
        assertThrows(NullPointerException.class, () -> {
            hotelService.create(null);
        });
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() {
        List<Hotel> expectedHotels = Arrays.asList(new Hotel("Hotel A", "Address A"), new Hotel("Hotel B", "Address B"), new Hotel("Hotel C", "Address C"));
        expectedHotels.get(0).setId(1L);
        expectedHotels.get(1).setId(2L);
        expectedHotels.get(2).setId(3L);

        when(hotelRepo.findAll()).thenReturn(expectedHotels);

        List<Hotel> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertEquals(expectedHotels.size(), result.size());
        assertEquals(expectedHotels, result);
        verify(hotelRepo, times(1)).findAll();
    }

    @Test
    void getAllHotels_ShouldReturnEmptyList_WhenNoHotelsExist() {
        List<Hotel> emptyList = List.of();
        when(hotelRepo.findAll()).thenReturn(emptyList);

        List<Hotel> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(hotelRepo, times(1)).findAll();
    }

    @Test
    void getHotelById_ShouldReturnHotel_WhenHotelExists() {
        Long hotelId = 1L;
        Hotel expectedHotel = new Hotel("Test Hotel", "Test Address");
        expectedHotel.setId(hotelId);

        when(hotelRepo.findById(hotelId)).thenReturn(Optional.of(expectedHotel));

        Hotel result = hotelService.getHotelById(hotelId);

        assertNotNull(result);
        assertEquals(expectedHotel, result);
        assertEquals(hotelId, result.getId());
        verify(hotelRepo, times(1)).findById(hotelId);
    }

    @Test
    void getHotelById_ShouldThrowException_WhenHotelNotFound() {
        Long nonExistentHotelId = 999L;
        when(hotelRepo.findById(nonExistentHotelId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> hotelService.getHotelById(nonExistentHotelId));

        assertEquals("Hotel not found", exception.getMessage());
        verify(hotelRepo, times(1)).findById(nonExistentHotelId);
    }

    @Test
    void getHotelById_ShouldThrowException_WhenIdIsNull() {
        Long nullId = null;
        when(hotelRepo.findById(null)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> hotelService.getHotelById(nullId));
        assertEquals("Hotel not found", exception.getMessage());
        verify(hotelRepo, times(1)).findById(null);
    }

    @Test
    void create_ShouldPreserveHotelProperties() {
        CreateHotelRequest request = new CreateHotelRequest();
        String hotelName = "Test Hotel with Very Long Name That Might Be Truncated";
        String hotelAddress = "Very long address with many details about the location of the hotel in the city";

        request.setName(hotelName);
        request.setAddress(hotelAddress);

        Hotel savedHotel = new Hotel(hotelName, hotelAddress);
        savedHotel.setId(100L);

        when(hotelRepo.save(any(Hotel.class))).thenReturn(savedHotel);

        Hotel result = hotelService.create(request);

        assertNotNull(result);
        assertEquals(hotelName, result.getName());
        assertEquals(hotelAddress, result.getAddress());

        ArgumentCaptor<Hotel> captor = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepo).save(captor.capture());
        assertEquals(hotelName, captor.getValue().getName());
        assertEquals(hotelAddress, captor.getValue().getAddress());
    }

    @Test
    void getHotelById_WithMultipleCalls_ShouldCallRepositoryEachTime() {
        Long hotelId = 1L;
        Hotel hotel = new Hotel("Hotel", "Address");
        hotel.setId(hotelId);

        when(hotelRepo.findById(hotelId)).thenReturn(Optional.of(hotel));

        hotelService.getHotelById(hotelId);
        hotelService.getHotelById(hotelId);
        hotelService.getHotelById(hotelId);

        verify(hotelRepo, times(3)).findById(hotelId);
    }
}