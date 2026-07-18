package com.jala.backend.siteaccess.entity;

import com.jala.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Assignment of a user to a site. Restricted roles (SUPERVISOR, WORKER,
 * DRIVER) may only access data belonging to sites they are assigned to.
 */
@Entity
@Table(name = "user_sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SuppressWarnings("java:S2160") // equality is id-based in BaseEntity (intentional for JPA)
public class UserSite extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;
}
