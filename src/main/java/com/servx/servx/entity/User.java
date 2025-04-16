package com.servx.servx.entity;

import com.servx.servx.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "profile_photo_url", nullable = false, length = 255)
    @Builder.Default
    private String profilePhotoUrl = "/images/default-profile.jpg";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.SERVICE_SEEKER;  // Default value added

    @Column(nullable = true, length = 100)
    private String education;

    @CreationTimestamp  // Auto-populates on creation
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // Added missing field

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "address_id", referencedColumnName = "id", unique = true, nullable = false)
    @ToString.Exclude  // Optional: Avoid recursion in toString()
    private Address address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude  // Optional: Avoid recursion in toString()
    private List<Language> languagesSpoken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ServiceProfile> services;
}