package org.example.expert.domain.user.dto.response;

public record UserSearchResponse(
        Long id,
        String email,
        String nickname
) {
}