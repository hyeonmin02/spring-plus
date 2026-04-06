package org.example.expert.domain.todo.dto.response;

public record TodoSearchResponse (
        String title, // 일정 제목
        Long managerCount, // 담당자 수
        Long commentCount // 총 댓글 갯수
) {}

