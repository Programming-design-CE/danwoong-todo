package com.danwoog.todo.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClosetViewController {

    @GetMapping("/closet")
    public String closetPage() {
        return "main/closet";
    }
}