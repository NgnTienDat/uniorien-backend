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

    float rating;

    int students;

    String founded;

    String institutionType; // loại hình: Công lập / tư thục / Quốc tế...

    String programsOffered; // Đào tạo: Đa ngành / Kỹ thuật / Kinh tế...e

    String logo;

    @Column(columnDefinition = "TEXT")
    private String about;

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


