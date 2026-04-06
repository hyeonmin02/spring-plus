package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoCustomRepository { // TODO querydsl로 변경해보기
    @Query("SELECT t " +
            "FROM Todo t " +
            "WHERE (:weather IS NULL OR :weather = t.weather) " +
            "AND (:startDate IS NULL OR t.modifiedAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.modifiedAt < :endDate) " + // end = 2026-04-03 t.modifiedAt < 2026-04-04 00:00:00
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findTodosByCondition(@Param("weather") String weather,
                                    @Param("startDate") LocalDateTime start,
                                    @Param("endDate") LocalDateTime end,
                                    Pageable pageable);
}
