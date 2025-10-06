package com.ntd.uniorien.entity;


import com.ntd.uniorien.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    String avatar;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "created_at")
    Instant createdAt;

    Role role = Role.USER;

    boolean active = true;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
