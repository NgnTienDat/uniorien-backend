package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "major_for_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MajorForGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @Column(name = "major_name", nullable = false)
    String majorName;

    @Column(name = "major_group_name", nullable = false)
    String majorGroupName;
}
