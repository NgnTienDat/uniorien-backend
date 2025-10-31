package com.ntd.uniorien.repository;

import com.ntd.uniorien.entity.Benchmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenchmarkRepository extends JpaRepository<Benchmark, String> {

    @Query("""
        SELECT b FROM Benchmark b
        JOIN b.major m
        JOIN b.admissionInformation ai
        JOIN b.university u
        WHERE LOWER(m.majorName) LIKE LOWER(CONCAT('%', :majorName, '%'))
          AND LOWER(ai.admissionMethod) LIKE LOWER(CONCAT('%', :admissionMethod, '%'))
          AND (:location IS NULL OR LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%')))
    """)
    List<Benchmark> searchBenchmarks(
            @Param("majorName") String majorName,
            @Param("admissionMethod") String admissionMethod,
            @Param("location") String location
    );
}


