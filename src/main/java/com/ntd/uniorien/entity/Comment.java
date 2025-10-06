package com.ntd.uniorien.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    String content;

    @Column(name = "like_number")
    Long likeNumber;

    @Column(name = "created_at")
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")       // nullable = true
    Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<Comment> replies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")         // nullable = true
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    University university;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
