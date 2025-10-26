package fun.justdevelops.hbmanagement.service;


import fun.justdevelops.hbmanagement.model.entity.Hotel;
import fun.justdevelops.hbmanagement.model.repo.HotelRepo;
import fun.justdevelops.hbmanagement.rest.dto.CreateHotelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class HotelService {

    private final HotelRepo repo;

    @Autowired
    public HotelService(HotelRepo repo) {
        this.repo = repo;
    }

    public Hotel create(CreateHotelRequest request) {
        Hotel hotel = new Hotel(request.getName(), request.getAddress());
        return repo.save(hotel);
    }

    public List<Hotel> getAllHotels() {
        return repo.findAll();
    }

    public Hotel getHotelById(Long id) {
        return repo.findById(id).orElseThrow(() ->  new RuntimeException("Hotel not found"));
    }
}
