package com.servx.servx.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "languages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "language"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include  // Include only the ID
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user; // Each language is associated with one user.

    @Column(nullable = false, length = 3)
    private String language; // ISO 639-1 language code (e.g., "en", "az").
}
