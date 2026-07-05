package com.jala.backend.site.entity;

import com.jala.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "site_code", nullable = false, unique = true, length = 20)
    private String siteCode;

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false)
    private String location;

    @Column(name = "total_acres", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAcres;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}