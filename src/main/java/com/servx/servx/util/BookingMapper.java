package com.servx.servx.util;

import com.servx.servx.dto.BookingDTO;
import com.servx.servx.entity.Booking;
import com.servx.servx.entity.ServiceProfile;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    @Value("${app.base-url}")
    private String appBaseUrl;

    public BookingDTO toDto(Booking booking) {

        String baseUrl = appBaseUrl;

        return BookingDTO.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .scheduledStartTime(booking.getScheduledStartTime())
                .durationMinutes(booking.getDurationMinutes())
                .priceMin(booking.getPriceMin())
                .priceMax(booking.getPriceMax())
                .notes(booking.getNotes())
                .status(booking.getStatus())
                .providerMarkedComplete(booking.isProviderMarkedComplete())
                .locationAddressLine(booking.getLocationAddressLine())
                .locationCity(booking.getLocationCity())
                .locationZipCode(booking.getLocationZipCode())
                .locationCountry(booking.getLocationCountry())
                .serviceRequestId(booking.getServiceRequest().getId())
                // provider fields
                .providerId(booking.getProvider().getId())
                .providerFirstName(booking.getProvider().getFirstName())
                .providerLastName(booking.getProvider().getLastName())
                .providerProfilePhotoUrl(constructFullUrl(baseUrl, booking.getProvider().getProfilePhotoUrl()))
                .seekerId(booking.getSeeker().getId())
                .seekerFirstName(booking.getSeeker().getFirstName())
                .seekerLastName(booking.getSeeker().getLastName())
                .seekerProfilePhotoUrl(constructFullUrl(baseUrl, booking.getSeeker().getProfilePhotoUrl()))
                // service fields
                .serviceId(booking.getService().getId())
                .serviceName(getServiceName(booking.getService()))
                .serviceCategoryName(booking.getService().getCategory().getName())
                .build();
    }

    private String constructFullUrl(String baseUrl, String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
            return path;
        }
        String cleanBaseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.replaceAll("/$", "") : "";
        String cleanPath = path.startsWith("/") ? path : "/" + path;

        return cleanBaseUrl.isEmpty() ? null : cleanBaseUrl + cleanPath;
    }

    private String getServiceName(ServiceProfile service) {
        return service.getServiceArea() != null ?
                service.getServiceArea().getName() :
                service.getCategory().getName();
    }

    public List<BookingDTO> toDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
