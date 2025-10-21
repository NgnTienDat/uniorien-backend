package com.ntd.uniorien.repository;

import com.ntd.uniorien.entity.MajorForGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MajorForGroupRepository extends JpaRepository<MajorForGroup, String> {

}
