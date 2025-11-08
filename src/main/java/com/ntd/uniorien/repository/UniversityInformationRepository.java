package com.ntd.uniorien.repository;

import com.ntd.uniorien.dto.response.UniversityResponse;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.entity.UniversityInformation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityInformationRepository extends JpaRepository<UniversityInformation, String> {


    UniversityInformation findByUniversity(University university);
}
