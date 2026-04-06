package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.dto.projection.UserSearchProjection;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findAllByNickname(String nickname, Pageable pageable);
    Slice<User> findAllSliceByNickname(String nickname, Pageable pageable);
    Page<UserSearchProjection> findProjectedByNickname(String nickname, Pageable pageable);
}
