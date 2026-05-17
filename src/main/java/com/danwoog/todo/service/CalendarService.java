package com.danwoog.todo.service;

import com.danwoog.todo.domain.Todo;
import com.danwoog.todo.dto.CalendarDailyResponse;
import com.danwoog.todo.dto.CalendarDayDto;
import com.danwoog.todo.dto.CalendarMonthlyResponse;
import com.danwoog.todo.dto.CalendarTodoDto;
import com.danwoog.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final TodoRepository todoRepository;

    public CalendarDailyResponse getDailyTodos(LocalDate date) {
        List<Todo> todos = todoRepository.findByDeadline(date);
        
        List<CalendarTodoDto> todoDtos = todos.stream()
                .map(todo -> CalendarTodoDto.builder()
                        .todoId(todo.getId())
                        .title(todo.getTitle())
                        .date(todo.getDeadline())
                        .completed(false) // 임시: 전체 그룹의 할 일 조회이므로, 완료 여부는 false 로 고정
                        .category(todo.getCategory())
                        .priority(todo.getPriority() != null ? todo.getPriority().name() : null)
                        .build())
                .collect(Collectors.toList());

        return new CalendarDailyResponse(date, todoDtos.size(), todoDtos);
    }

    public CalendarMonthlyResponse getMonthlyTodos(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Todo> todos = todoRepository.findByDeadlineBetween(startDate, endDate);

        // Group by date and count
        Map<LocalDate, Long> countByDate = todos.stream()
                .collect(Collectors.groupingBy(Todo::getDeadline, Collectors.counting()));

        List<CalendarDayDto> days = countByDate.entrySet().stream()
                .map(entry -> new CalendarDayDto(entry.getKey(), entry.getValue().intValue()))
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        return new CalendarMonthlyResponse(year, month, days);
    }
}
