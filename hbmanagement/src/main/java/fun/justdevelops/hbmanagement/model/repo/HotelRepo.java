package fun.justdevelops.hbmanagement.model.repo;

import fun.justdevelops.hbmanagement.model.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepo extends JpaRepository<Hotel, Long> {
}
