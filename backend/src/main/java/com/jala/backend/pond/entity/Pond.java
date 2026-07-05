package com.jala.backend.pond.entity;

import com.jala.backend.common.entity.BaseEntity;
import com.jala.backend.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ponds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pond extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "pond_code", nullable = false, unique = true, length = 20)
    private String pondCode;

    @Column(name = "pond_name", nullable = false, length = 100)
    private String pondName;

    @Column(name = "pond_acres", nullable = false, precision = 10, scale = 2)
    private BigDecimal pondAcres;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}