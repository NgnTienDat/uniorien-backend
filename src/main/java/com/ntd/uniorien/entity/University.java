package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Set;

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

    @ColumnDefault("true")
    boolean active;

    String logo;

    @Column(name = "detail_information")
    String detailInformation;

    @Column(name = "created_at")
    Instant createdAt;

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL , fetch = FetchType.LAZY, orphanRemoval = true)
    Set<AdmissionInformation> admissionInformations;

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL , fetch = FetchType.LAZY, orphanRemoval = true)
    Set<Comment> comments;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        active = true;
    }
}
