package com.servx.servx.entity;

import com.servx.servx.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "bookings") // Define the table name
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String bookingNumber; // e.g., "BK-12345", generated in service layer

    @Column(nullable = false)
    private Instant scheduledStartTime; // From BookingRequestPayload

    @Column(nullable = false)
    private Integer durationMinutes; // Consider adding this to payload/sheet

    @Column(nullable = false)
    private Double priceMin; // From BookingRequestPayload

    @Column(nullable = false)
    private Double priceMax; // From BookingRequestPayload (can be same as min)

    @Column(columnDefinition = "TEXT") // Allow longer notes
    private String notes; // From BookingRequestPayload

    // Store address details directly, copied from ServiceRequest at booking time
    @Column(nullable = false)
    private String locationAddressLine;

    @Column(nullable = false)
    private String locationCity;

    @Column(nullable = false)
    private String locationZipCode;

    @Column(nullable = false)
    private String locationCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    @Builder.Default
    private boolean providerMarkedComplete = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // --- Relationships ---

    @OneToOne(fetch = FetchType.LAZY) // A booking confirms one specific request
    @JoinColumn(name = "service_request_id", referencedColumnName = "id", unique = true, nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private User seeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false) // Link to the specific ServiceProfile booked
    private ServiceProfile service;

    // --- Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) { // Default status if not set by builder
            this.status = BookingStatus.UPCOMING;
        }
        // Consider generating bookingNumber here if not done in service
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
