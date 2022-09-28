package com.neu.dimple.springbootapplication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @RequestMapping("/healthz")
    @ResponseStatus(code = HttpStatus.OK)
    public void healthCheck(){
        return;
    }
}

