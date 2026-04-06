package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;

@Entity
@Table(name = "logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Log extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message; // 실패 이유
    private boolean success; // 성공, 실패 여부
    // todoId 어느 일정에 대한 매니저 등록 요청인가?
    private Long todoId;
    // managerUserId 누구를 매니저로 등록하려 했는지
    private Long managerUserId;
    // requesterUserId 누가 매니저가 되려고 했는지뿐만 아니라 누가 등록을 시도했는지
    private Long requesterUserId;
    @Builder
    private Log(
            String message,
            boolean success,
            Long todoId,
            Long managerUserId,
            Long requesterUserId
            )
    {
        this.message = message;
        this.success = success;
        this.todoId = todoId;
        this.managerUserId = managerUserId;
        this.requesterUserId = requesterUserId;
    }
}
