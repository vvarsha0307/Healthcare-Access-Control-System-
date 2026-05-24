package com.example.demo.controller;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/secure")
    public String secure() {
        return "JWT Authorization Working!";
    }
}
