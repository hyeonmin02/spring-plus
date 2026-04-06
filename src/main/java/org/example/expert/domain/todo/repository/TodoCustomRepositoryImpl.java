package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(todo)
                .join(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne());
    }

    @Override
    public Page<TodoSearchResponse> findAllBySearch(
            TodoSearchCondition condition, Pageable pageable)
    {
        // content 조회: 실제 페이지에 보여줄 데이터
        List<TodoSearchResponse> content = (queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        manager.id.countDistinct(), // 담당자 수(중복 제거)
                        comment.id.countDistinct() // 댓글 수(중복 제거)
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(condition.keyword()), // 제목 부분 검색
                        managerNicknameContains(condition.managerNickname()), // 담당자 닉네임 부분 검색
                        createdAtGoe(condition.startDate()), // 시작일 이상
                        createdAtLt(condition.endDate()) // 종료일 포함(다음날 00시00분 미만)
                )
                .groupBy(todo.id, todo.title, todo.createdAt) // Todo 기준 그룹화
                .orderBy(todo.createdAt.desc()) // 생성일 기준 최신순 정렬
                .offset(pageable.getOffset()) // 페이징 시작 위치
                .limit(pageable.getPageSize()) // 페이징 크기
                .fetch());

        // total count 조회: 전체 데이터 개수 (페이지 계산용)
        Long total = queryFactory
                .select(todo.id.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(
                        titleContains(condition.keyword()),
                        managerNicknameContains(condition.managerNickname()),
                        createdAtGoe(condition.startDate()),
                        createdAtLt(condition.endDate())
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // 제목 키워드 부분 검색
    private BooleanExpression titleContains(String keyword) {
        return hasText(keyword) ? todo.title.contains(keyword) : null;
    }

    // 담당자 닉네임 부분 검색
    private BooleanExpression managerNicknameContains(String nickname) {
        return hasText(nickname) ? user.nickname.contains(nickname) : null;
    }

    // 시작일 조건(해당 날짜 00:00:00 이상)
    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? todo.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    // 종료일 다음날 00:00 미만으로 조회하여 endDate 하루 전체를 포함
    // 예: 2026-04-06 -> 2026-04-07 00:00:00 미만
    private BooleanExpression createdAtLt(LocalDate endDate) {
        return endDate != null ? todo.createdAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
    }
}
