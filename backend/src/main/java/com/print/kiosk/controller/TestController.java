package com.print.kiosk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testBackend() {
        return "Kiosk backend is running successfully";
    }
}