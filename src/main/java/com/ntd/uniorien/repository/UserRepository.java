package com.ntd.uniorien.repository;

import com.ntd.uniorien.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsUserByEmail(String email);
    Optional<User> findUserByEmail(String email);

    @Override
    Page<User> findAll(Pageable pageable);
}
