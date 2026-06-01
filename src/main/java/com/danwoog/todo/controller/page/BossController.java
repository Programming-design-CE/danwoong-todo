package com.danwoog.todo.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BossController {

    @GetMapping("/boss")
    public String bossPage() {
        return "boss/boss";
    }
}