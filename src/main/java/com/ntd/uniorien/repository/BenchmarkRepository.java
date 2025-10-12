package com.ntd.uniorien.repository;

import com.ntd.uniorien.dto.response.UniversityResponse;
import com.ntd.uniorien.entity.University;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionInfoRepository extends JpaRepository<University, String> {
    @Query("SELECT u.universityCode FROM University u")
    List<String> findAllCodes();

//    @Query("SELECT u.id AS id, u.universityCode AS code, u.universityName AS name FROM University u")
//    List<UniversityInfo> findAllCodeAndName();

    @Query("SELECT" +
            " new com.ntd.uniorien.dto.response.UniversityResponse(u.id, u.universityCode, u.universityName, u.website)" +
            " FROM University u")
    List<UniversityResponse> findAllCodeAndName();

    @Query("SELECT new com.ntd.uniorien.dto.response.UniversityResponse(u.id, u.universityCode, u.universityName, u.website) " +
            "FROM University u")
    List<UniversityResponse> findAllCodeAndName(Pageable pageable);




}
