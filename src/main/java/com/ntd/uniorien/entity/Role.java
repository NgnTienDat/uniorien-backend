package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Role {
    @Id
    @Column(nullable = false, unique = true)
    String roleName;

    String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    Set<User> users;
}
