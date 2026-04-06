package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    /**
     * 성공 로그 저장
     * - 매니저 등록 성공 시 호출
     * - REQUIRES_NEW로 별도 트랜잭션에서 커밋
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 기존 트랜잭션과 분리하여 항상 새로운 트랜잭션에서 실행
    public void saveSuccessLog(Long todoId, Long managerUserId, Long requesterUserId) {
        Log log = Log.builder()
                .message("매니저 등록 성공")
                .success(true)
                .todoId(todoId)
                .managerUserId(managerUserId)
                .requesterUserId(requesterUserId)
                .build();

        logRepository.save(log);
    }

    /**
     * 실패 로그 저장
     * - 매니저 등록 실패 시 호출
     * - 실패 사유(message)를 함께 저장
     * - REQUIRES_NEW로 별도 트랜잭션에서 커밋
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailLog(Long todoId, Long managerUserId, Long requesterUserId, String message) {
        Log log = Log.builder()
                .message(message)
                .success(false)
                .todoId(todoId)
                .managerUserId(managerUserId)
                .requesterUserId(requesterUserId)
                .build();

        logRepository.save(log);
    }
}
