package fun.justdevelops.hbbooking.model.repo;

import fun.justdevelops.hbbooking.model.entity.Booking;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM bookings b WHERE " +
            "(b.status = 'PENDING' OR b.status = 'CONFIRMED') " +
            "AND b.dateStart <= :dateEnd " +
            "AND b.dateEnd >= :dateStart")
    List<Booking> findOverlappingBookings(
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd);

    List<Booking> findByUserId(Long userId);
}
