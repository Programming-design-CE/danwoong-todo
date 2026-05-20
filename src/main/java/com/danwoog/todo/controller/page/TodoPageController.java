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
        return "todo/todo";
    }
}
