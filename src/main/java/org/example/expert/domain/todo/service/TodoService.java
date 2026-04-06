package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.config.MyUserDetails;
import org.example.expert.domain.common.exception.NotFoundException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(MyUserDetails myUserDetails, TodoSaveRequest todoSaveRequest) {
        Long userId = myUserDetails.getUserId();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("존재하지않는 유저입니다"));

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDate start, LocalDate end) {
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime startDateTime = null;
        if (start != null) {
            // LocalDateTime과 비교하기 위해 해당 날짜의 시작 시간으로 변환
            startDateTime = start.atStartOfDay();
        }
        // endDate는 해당 날짜 하루 전체를 포함해야함
        LocalDateTime endExclusive = null;
        if (end != null) {
            // 다음날 00:00:00으로 변환 후 미만이란 조건으로 비교하기 위한 경계값(endExclusive) 사용
            endExclusive = end.plusDays(1).atStartOfDay();
        }

        Page<Todo> todos = todoRepository.findTodosByCondition(weather, startDateTime, endExclusive, pageable);


        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(Long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new NotFoundException("Todo Not Found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    public Page<TodoSearchResponse> searchTodos(
            TodoSearchCondition condition,
            Pageable pageable
    ) {
        return todoRepository.findAllBySearch(condition, pageable);
    }
}
