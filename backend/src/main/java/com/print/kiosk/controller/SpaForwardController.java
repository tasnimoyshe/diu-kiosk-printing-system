package com.print.kiosk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/queue",

            "/track",
            "/track/**",

            "/payment",
            "/payment/**",

            "/pay-later",
            "/pay-later/**",

            "/login",
            "/login/**",

            "/register-agent",

            "/unauthorized",

            "/admin",
            "/admin/**",

            "/agent",
            "/agent/**"
    })
    public String forwardReactRoutes() {
        return "forward:/index.html";
    }
}