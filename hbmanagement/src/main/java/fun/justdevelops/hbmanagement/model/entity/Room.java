package fun.justdevelops.hbmanagement.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "number", nullable = false)
    private int number;

    @Column(name = "available", nullable = false, columnDefinition = "boolean default true")
    private boolean available;

    @Column(name = "times_booked", nullable = true, columnDefinition = "int default 0")
    private int timesBooked;

    public Room() {}

    public Room(Hotel hotel, int number) {
        this.hotel = hotel;
        this.number = number;
    }
}
