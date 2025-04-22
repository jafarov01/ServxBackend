package com.servx.servx.util;

import com.servx.servx.dto.BookingDTO;
import com.servx.servx.entity.Booking;
import com.servx.servx.entity.ServiceProfile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public BookingDTO toDto(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .scheduledStartTime(booking.getScheduledStartTime())
                .durationMinutes(booking.getDurationMinutes())
                .priceMin(booking.getPriceMin())
                .priceMax(booking.getPriceMax())
                .notes(booking.getNotes())
                .status(booking.getStatus())
                .locationAddressLine(booking.getLocationAddressLine())
                .locationCity(booking.getLocationCity())
                .locationZipCode(booking.getLocationZipCode())
                .locationCountry(booking.getLocationCountry())
                .serviceRequestId(booking.getServiceRequest().getId())
                // Provider fields
                .providerId(booking.getProvider().getId())
                .providerFirstName(booking.getProvider().getFirstName())
                .providerLastName(booking.getProvider().getLastName())
                .providerProfilePhotoUrl(booking.getProvider().getProfilePhotoUrl())
                // Seeker fields
                .seekerId(booking.getSeeker().getId())
                .seekerFirstName(booking.getSeeker().getFirstName())
                .seekerLastName(booking.getSeeker().getLastName())
                .seekerProfilePhotoUrl(booking.getSeeker().getProfilePhotoUrl())
                // Service fields
                .serviceId(booking.getService().getId())
                .serviceName(getServiceName(booking.getService()))
                .serviceCategoryName(booking.getService().getCategory().getName())
                .build();
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
