package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.request.CommentCreationRequest;
import com.ntd.uniorien.dto.response.CommentResponse;
import com.ntd.uniorien.dto.response.PageResponse;
import com.ntd.uniorien.dto.response.UniversityDetailResponse;
import com.ntd.uniorien.dto.response.UniversityReviewResponse;
import com.ntd.uniorien.entity.Comment;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.entity.UniversityInformation;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.repository.CommentRepository;
import com.ntd.uniorien.repository.UniversityInformationRepository;
import com.ntd.uniorien.repository.UniversityRepository;
import com.ntd.uniorien.repository.UserRepository;
import com.ntd.uniorien.utils.helper.PageResponseUtil;
import com.ntd.uniorien.utils.mapper.CommentMapper;
import com.ntd.uniorien.utils.mapper.UniversityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {
    UniversityRepository universityRepository;
    CommentRepository commentRepository;
    UserRepository userRepository;
    UniversityInformationRepository universityInformationRepository;
    CommentMapper commentMapper;
    UniversityMapper universityMapper;


    public List<UniversityReviewResponse> allUniversityReviews() {
        List<University> universities = universityRepository.findAll();
        return universities.stream().map(universityMapper::toUniversityResponse).toList();
    }

    public void createComment(CommentCreationRequest commentCreationRequest) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user1 = userRepository.findUserByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOTFOUND)
        );

        University university = universityRepository.findById(commentCreationRequest.getUniversityId())
                .orElseThrow(() -> new AppException(ErrorCode.UNIVERSITY_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(commentCreationRequest.getContent())
                .user(user1)
                .university(university)
                .build();

        commentRepository.save(comment);
    }


    public PageResponse<CommentResponse> getAllComments(String universityCode, int page, int size) {
        University university = universityRepository.findByUniversityCode(universityCode)
                .orElseThrow(() -> new AppException(ErrorCode.UNIVERSITY_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findAllByUniversity(university, pageable);

        return PageResponseUtil.build(commentPage, commentMapper::toCommentResponse);
    }



    @PreAuthorize("hasRole('ADMIN')")
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(comment);

    }


    public UniversityDetailResponse getDetailInfo(String universityCode) {
        University university = universityRepository.findByUniversityCode(universityCode)
                .orElseThrow(() -> new AppException(ErrorCode.UNIVERSITY_NOT_FOUND));

        UniversityInformation universityInformation = universityInformationRepository
                .findByUniversity(university);
        return universityMapper.toUniversityDetailResponse(universityInformation);

    }



}
