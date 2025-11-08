package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Set;
@Entity
@Table(name = "university_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UniversityInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    String name;

    String location;

    String websiteAddress;

    String about;

    String logo;

    @Column(name = "created_at")
    Instant createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", referencedColumnName = "id", nullable = false, unique = true)
    University university;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}


