package com.xhpcd.shop.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a")
public class Testcontroller {
    @GetMapping("/f")
    public String a(){
        return "a";
    }
}
