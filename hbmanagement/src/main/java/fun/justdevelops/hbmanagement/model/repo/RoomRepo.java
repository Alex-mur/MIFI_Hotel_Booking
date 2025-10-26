package fun.justdevelops.hbmanagement.model.repo;

import fun.justdevelops.hbmanagement.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepo extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);
}
