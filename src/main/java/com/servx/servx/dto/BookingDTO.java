package com.servx.servx.dto;

import com.servx.servx.enums.BookingStatus; // Import the enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;
    private String bookingNumber;
    private BookingStatus status;
    private boolean providerMarkedComplete;

    // Schedule Info
    private Instant scheduledStartTime;
    private Integer durationMinutes;

    // Price Info
    private Double priceMin;
    private Double priceMax;

    // Location Info
    private String locationAddressLine;
    private String locationCity;
    private String locationZipCode;
    private String locationCountry;

    // Notes from proposal
    private String notes;

    // Service Info
    private Long serviceId;
    private String serviceName;
    private String serviceCategoryName;

    // Provider Info
    private Long providerId;
    private String providerFirstName;
    private String providerLastName;
    private String providerProfilePhotoUrl;

    // Seeker Info
    private Long seekerId;
    private String seekerFirstName;
    private String seekerLastName;
    private String seekerProfilePhotoUrl;

    // Link back to the original request for messaging
    private Long serviceRequestId;

    private Instant createdAt;
    private Instant updatedAt;

    public String getProviderFullName() {
        return (providerFirstName != null ? providerFirstName : "")
                + " " + (providerLastName != null ? providerLastName : "");
    }

    public String getSeekerFullName() {
        return (seekerFirstName != null ? seekerFirstName : "")
                + " " + (seekerLastName != null ? seekerLastName : "");
    }

}