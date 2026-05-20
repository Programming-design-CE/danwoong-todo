package com.danwoog.todo.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TodoPageController {

    @GetMapping("/todos")
    public String todosPage() {
        return "todo/group-todo";
    }

    @GetMapping("/todo")
    public String todoPage() {
        return "redirect:/todos/working";
    }

    @GetMapping("/todos/working")
    public String workingTodosPage() {
        return "todo/todos/working";
    }

    @GetMapping("/todos/completed")
    public String completedTodosPage() {
        return "todo/todos/completed";
    }

    @GetMapping("/todos/calendar")
    public String calendarTodosPage() {
        return "todo/todos/calendar";
    }

    @GetMapping("/todos/files")
    public String fileTodosPage() {
        return "todo/todos/files";
    }

    @GetMapping("/mytodos")
    public String myTodoPage() {
        return "redirect:/mytodos/working";
    }

    @GetMapping("/mytodos/working")
    public String myWorkingTodosPage() {
        return "todo/mytodos/working";
    }

    @GetMapping("/mytodos/completed")
    public String myCompletedTodosPage() {
        return "todo/mytodos/completed";
    }
}
