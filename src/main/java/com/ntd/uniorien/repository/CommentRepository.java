package com.ntd.uniorien.repository;

import com.ntd.uniorien.entity.Comment;
import com.ntd.uniorien.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findAllByUniversity(University university);
}
