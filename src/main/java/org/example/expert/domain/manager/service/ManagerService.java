package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.MyUserDetails;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.NotFoundException;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final LogService logService;

    @Transactional
    public ManagerSaveResponse saveManager(MyUserDetails myUserDetails, long todoId, ManagerSaveRequest managerSaveRequest) {
        // 실패하더라도 로그에 남기기 위해 요청 단계에서 바로 확보 가능한 값은 먼저 꺼내둔다.
        Long requesterUserId = myUserDetails.getUserId(); // 매니저 등록을 요청한 사용자의 ID (현재 로그인 유저)
        Long managerUserId = managerSaveRequest.getManagerUserId(); // 매니저로 등록하려는 대상 사용자의 ID
        // 일정을 만든 유저
        try {
            Long userId = myUserDetails.getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("존재하지않는 유저입니다"));

            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new InvalidRequestException("Todo not found"));

            if (todo.getUser() == null || !ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId())) {
                throw new InvalidRequestException("담당자를 등록하려고 하는 유저가 유효하지 않거나, 일정을 만든 유저가 아닙니다.");
            }

            User managerUser = userRepository.findById(managerSaveRequest.getManagerUserId())
                    .orElseThrow(() -> new InvalidRequestException("등록하려고 하는 담당자 유저가 존재하지 않습니다."));

            if (ObjectUtils.nullSafeEquals(user.getId(), managerUser.getId())) {
                throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
            }

            Manager newManagerUser = new Manager(managerUser, todo);
            Manager savedManagerUser = managerRepository.save(newManagerUser);
            // 성공 로그 저장
            // 별도 트랜잭션(REQUIRES_NEW)으로 저장되므로
            // 이후 다른 문제가 생겨도 로그는 커밋될 수 있다.
            logService.saveSuccessLog(todoId, managerUserId, requesterUserId);

            return new ManagerSaveResponse(
                    savedManagerUser.getId(),
                    new UserResponse(managerUser.getId(), managerUser.getEmail())
            );
        } catch (RuntimeException e) {
            // 실패 로그 저장
            // 예외 메시지를 그대로 남겨서 실패 원인을 추적할 수 있게 한다.
            logService.saveFailLog(todoId, managerUserId, requesterUserId, e.getMessage());
            throw e;
        }
    }

    public List<ManagerResponse> getManagers(long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        List<Manager> managerList = managerRepository.findByTodoIdWithUser(todo.getId());

        List<ManagerResponse> dtoList = new ArrayList<>();
        for (Manager manager : managerList) {
            User user = manager.getUser();
            dtoList.add(new ManagerResponse(
                    manager.getId(),
                    new UserResponse(user.getId(), user.getEmail())
            ));
        }
        return dtoList;
    }

    @Transactional
    public void deleteManager(MyUserDetails myUserDetails, long todoId, long managerId) {
        Long userId = myUserDetails.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지않는 유저입니다"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new NotFoundException("Todo not found"));

        if (todo.getUser() == null || !ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId())) {
            throw new InvalidRequestException("해당 일정을 만든 유저가 유효하지 않습니다.");
        }

        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        if (!ObjectUtils.nullSafeEquals(todo.getId(), manager.getTodo().getId())) {
            throw new InvalidRequestException("해당 일정에 등록된 담당자가 아닙니다.");
        }

        managerRepository.delete(manager);
    }
}
