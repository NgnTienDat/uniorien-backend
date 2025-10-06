package com.ntd.uniorien.entity;


import com.ntd.uniorien.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

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


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    Role role = Role.USER;


    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    boolean active = true;


    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    Set<Comment> comments;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
