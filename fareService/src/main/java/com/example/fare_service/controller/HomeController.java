package com.example.fare_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping
    public String index(){
        return "index";
    }

    @GetMapping("/success")
    public String success(){
        return "success";
    }
}