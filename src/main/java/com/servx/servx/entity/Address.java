package com.servx.servx.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String city; // ISO code for the city, if applicable, or plain city name.

    @Column(nullable = false, length = 3)
    private String country; // ISO 3166-1 alpha-3 country code (e.g., "USA", "AZE").

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 255)
    private String addressLine;

    @OneToOne(mappedBy = "address", optional = false)
    private User user;
}