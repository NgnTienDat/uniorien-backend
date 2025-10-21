package com.ntd.uniorien.repository;

import com.ntd.uniorien.entity.AdmissionInformation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionInfoRepository extends JpaRepository<AdmissionInformation, String> {





}
