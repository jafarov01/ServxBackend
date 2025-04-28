package com.servx.servx.enums;

public enum BookingStatus {
    UPCOMING,   // Confirmed but not yet started/completed
    COMPLETED,  // Service successfully completed
    CANCELLED_BY_SEEKER,   // Cancelled by seeker before completion
    CANCELLED_BY_PROVIDER  // Cancelled by provider
}