package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.MyUserDetails;
import org.example.expert.domain.user.dto.projection.UserSearchProjection;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(
            @AuthenticationPrincipal MyUserDetails myUserDetails,
            @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(myUserDetails.getUserId(), userChangePasswordRequest);
    }

    // 일반 페이징 조회
    @GetMapping("/users/search")
    public ResponseEntity<Page<UserSearchResponse>> searchUsersByNickname(
            @RequestParam String nickname,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsersByNickname(nickname, pageable));
    }

    // 슬라이스 조회
    @GetMapping("/users/search/slice")
    public ResponseEntity<Slice<UserSearchResponse>> searchUsersByNicknameWithSlice(
            @RequestParam String nickname,
            Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsersByNicknameWithSlice(nickname, pageable));
    }

    // 인터페이스 프로젝션 조회
    @GetMapping("/users/search/projection")
    public ResponseEntity<Page<UserSearchResponse>> userSearchProjections(
            @RequestParam String nickname,
            Pageable pageable) {
        return ResponseEntity.ok(userService.userSearchProjections(nickname, pageable));
    }
}
