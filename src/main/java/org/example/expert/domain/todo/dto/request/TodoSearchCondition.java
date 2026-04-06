package org.example.expert.domain.todo.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


public record TodoSearchCondition(
        String keyword, // 검색 키워드
        String managerNickname, // 담당자 닉네임

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate, // 시작 날짜

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate // 종료 날짜
) {
}
