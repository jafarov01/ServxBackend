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

    private Long id; // Booking ID
    private String bookingNumber;
    private BookingStatus status;

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

    // Service Info (Simplified - get from linked ServiceProfile)
    private Long serviceId; // ID of the ServiceProfile
    private String serviceName; // e.g., "AC Repair", could be subcategory name
    private String serviceCategoryName; // e.g., "Appliance Repair"

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

    // Link back to the original request for messaging context
    private Long serviceRequestId;

    // Timestamps (Optional for display, but often useful)
    private Instant createdAt;
    private Instant updatedAt;

    // --- Helper method to get full provider name ---
    public String getProviderFullName() {
        return (providerFirstName != null ? providerFirstName : "")
                + " " + (providerLastName != null ? providerLastName : "");
    }

    // --- Helper method to get full seeker name ---
    public String getSeekerFullName() {
        return (seekerFirstName != null ? seekerFirstName : "")
                + " " + (seekerLastName != null ? seekerLastName : "");
    }

}