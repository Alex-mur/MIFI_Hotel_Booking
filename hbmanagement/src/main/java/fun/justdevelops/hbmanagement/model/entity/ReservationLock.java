package fun.justdevelops.hbmanagement.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "reservation_locks")
public class ReservationLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ReservationLock() {}

    public ReservationLock(String requestId, Room room, LocalDate dateStart, LocalDate dateEnd) {
        this.requestId = requestId;
        this.room = room;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }
}
