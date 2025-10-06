package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "university")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @Column(name = "university_code", nullable = false, length = 10)
    String universityCode;

    @Column(name = "university_name", nullable = false)
    String universityName;

    @Column(nullable = false)
    String location;

    String website;

    boolean active = true;

    String logo;

    @Column(name = "detail_information")
    String detailInformation;

    @Column(name = "created_at")
    Instant createdAt;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
