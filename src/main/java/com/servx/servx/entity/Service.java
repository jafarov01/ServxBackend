package com.servx.servx.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "services")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    @ManyToOne
    @JoinColumn(name = "service_area_id", nullable = false)
    private ServiceArea serviceArea;

    @NotBlank(message = "Work experience is mandatory")
    @Column(nullable = false, length = 50)
    private String workExperience;
}