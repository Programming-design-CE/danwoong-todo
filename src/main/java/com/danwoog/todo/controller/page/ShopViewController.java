package com.danwoog.todo.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopViewController {

    @GetMapping("/shop")
    public String shopPage() {
        return "main/shop";
    }
}