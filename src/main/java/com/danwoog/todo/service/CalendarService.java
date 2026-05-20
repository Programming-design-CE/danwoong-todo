package com.danwoog.todo.service;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.dto.calendar.CalendarDailyResponse;
import com.danwoog.todo.dto.calendar.CalendarDayDto;
import com.danwoog.todo.dto.calendar.CalendarMonthlyResponse;
import com.danwoog.todo.dto.calendar.CalendarTodoDto;
import com.danwoog.todo.repository.todo.TodoRepository;
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
        List<Todo> todos = todoRepository.findByDeadlineBetween(date.atStartOfDay(), date.atTime(23, 59, 59));

        List<CalendarTodoDto> todoDtos = todos.stream()
                .map(todo -> CalendarTodoDto.builder()
                        .todoId(todo.getTodoId())
                        .title(todo.getTodoName())
                        .date(todo.getDeadline() != null ? todo.getDeadline().toLocalDate() : null)
                        .completed(false)
                        .category(todo.getCategory() != null ? todo.getCategory().getLabel() : null)
                        .priority(todo.getPriority() != null ? todo.getPriority().name() : null)
                        .build())
                .collect(Collectors.toList());

        return new CalendarDailyResponse(date, todoDtos.size(), todoDtos);
    }

    public CalendarMonthlyResponse getMonthlyTodos(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Todo> todos = todoRepository.findByDeadlineBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        Map<LocalDate, Long> countByDate = todos.stream()
                .filter(todo -> todo.getDeadline() != null)
                .collect(Collectors.groupingBy(todo -> todo.getDeadline().toLocalDate(), Collectors.counting()));

        List<CalendarDayDto> days = countByDate.entrySet().stream()
                .map(entry -> new CalendarDayDto(entry.getKey(), entry.getValue().intValue()))
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        return new CalendarMonthlyResponse(year, month, days);
    }
}
