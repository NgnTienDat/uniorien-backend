//package com.ntd.uniorien.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//import java.util.Set;
//
//@Entity
//@Table(name = "major_group")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//@Builder
//public class MajorGroup {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", nullable = false, length = 36)
//    String id;
//
//    @Column(nullable = false)
//    String name;
//
//    @OneToMany(mappedBy = "majorGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    Set<Major> majors;
//}
