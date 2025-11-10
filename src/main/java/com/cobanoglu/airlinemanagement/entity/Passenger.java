package com.cobanoglu.airlinemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String name;

    @Column(name = "last_name", nullable = false, length = 50)
    private String surname;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "loyalty_points", nullable = false)
    private int loyaltyPoints = 0;

    @OneToMany(
            mappedBy = "passenger",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false
    )
    private List<Booking> bookings = new ArrayList<>();
}
