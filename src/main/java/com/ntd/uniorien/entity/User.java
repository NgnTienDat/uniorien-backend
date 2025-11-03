package com.ntd.uniorien.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @Column(nullable = false, unique = true)
    String email;

    String password;
    String avatar;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "created_at")
    Instant createdAt;

    @ManyToOne(fetch = FetchType.EAGER) // Một user chỉ có 1 role
    @JoinColumn(name = "role_id")       // Tên cột foreign key
    Role role;

    @ColumnDefault("true")
    boolean active;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    Set<Comment> comments;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        active = true;
    }
}
