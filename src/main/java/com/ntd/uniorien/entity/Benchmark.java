package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "benchmark")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Benchmark {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    float score;

    String note;

    String subjectCombinations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    Major major;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_information_id", nullable = false)
    AdmissionInformation admissionInformation;


}
