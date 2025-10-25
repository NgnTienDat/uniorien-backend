package com.ntd.uniorien.repository;

import com.ntd.uniorien.dto.response.UniversityResponse;
import com.ntd.uniorien.entity.University;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, String> {
    @Query("SELECT u.universityCode FROM University u")
    List<String> findAllCodes();

//    @Query("SELECT u.id AS id, u.universityCode AS code, u.universityName AS name FROM University u")
//    List<UniversityInfo> findAllCodeAndName();

    @Query("SELECT" +
            " new com.ntd.uniorien.dto.response.UniversityResponse(u.id, u.universityCode, u.universityName, u.website)" +
            " FROM University u")
    List<UniversityResponse> findAllCodeAndName();

    @Query("SELECT " +
            "new com.ntd.uniorien.dto.response.UniversityResponse(u.id, u.universityCode, u.universityName, u.website) " +
            "FROM University u")
    List<UniversityResponse> findAllCodeAndName(Pageable pageable);

    Optional<University> findByUniversityCode(String universityCode);

    // Query đã JOIN FETCH benchmarks & major, không còn N+1 query
        @Query("""
                SELECT DISTINCT u
                FROM University u
                LEFT JOIN FETCH u.admissionInformations ai
                LEFT JOIN FETCH ai.benchmarks b
                LEFT JOIN FETCH b.major
                WHERE u.universityCode = :code
                  AND ai.yearOfAdmission = :year
            """)
    Optional<University> findWithBenchmarksByYear(
            @Param("code") String code,
            @Param("year") Integer year
    );


}
