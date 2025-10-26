package fun.justdevelops.hbmanagement.model.repo;

import fun.justdevelops.hbmanagement.model.entity.ReservationLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationLockRepo extends JpaRepository<ReservationLock, Long> {

    Optional<ReservationLock> findByRequestId(String requestId);

    void deleteByRequestId(String requestId);

    @Query("SELECT r FROM reservation_locks r " +
            "WHERE r.roomId = :roomId " +
            "AND r.dateStart <= :dateEnd " +
            "AND r.dateEnd >= :dateStart")
    List<ReservationLock> findOverlappingLocks(
            @Param("roomId") Long roomId,
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd);
}
