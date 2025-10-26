package fun.justdevelops.hbbooking.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    @Column(name = "status", nullable = false, columnDefinition = "ENUM('PENDING')")
    private BookingStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Booking() {
        this.status = BookingStatus.PENDING;
    }

    public Booking(Long userId, Long roomId, LocalDate dateStart, LocalDate dateEnd, BookingStatus status) {
        this.userId = userId;
        this.roomId = roomId;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.status = status;
    }
}
