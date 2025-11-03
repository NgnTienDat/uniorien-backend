package com.ntd.uniorien.repository;

import com.ntd.uniorien.entity.Major;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, String> {
    @Query("SELECT m.majorCode FROM Major m")
    List<String> findAllCodes();

    Optional<Major> findByMajorCodeAndMajorName(String majorCode, String majorName);





}
