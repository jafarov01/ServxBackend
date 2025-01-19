package com.servx.servx.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "profile_service_areas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_id", "service_area_id"})
})
@NoArgsConstructor
@AllArgsConstructor
public class ProfileServiceArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // Each service area must be linked to a profile
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // Each record must link to a specific service area
    @JoinColumn(name = "service_area_id", nullable = false)
    private ServiceArea serviceArea;
}
