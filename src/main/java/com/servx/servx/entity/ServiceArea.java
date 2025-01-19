package com.servx.servx.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_areas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) // ISO-compliant length for codes or descriptive names
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // Service area must belong to a category
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;
}
