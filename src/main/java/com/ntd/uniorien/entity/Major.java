package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "major")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Major {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @Column(name = "major_code", nullable = false)
    String majorCode;

    @Column(name = "major_name", nullable = false)
    String majorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_group_id", nullable = true)
    MajorGroup majorGroup;
}
