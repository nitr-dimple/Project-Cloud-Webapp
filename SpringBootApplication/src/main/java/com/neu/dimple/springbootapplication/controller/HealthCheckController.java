package com.neu.dimple.springbootapplication.controller;

import com.neu.dimple.springbootapplication.config.StatsdClient;
import com.neu.dimple.springbootapplication.controller.accountcontroller.AccountController;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HealthCheckController {

    private static StatsdClient statsDClient;

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Logger logger = LoggerFactory.getLogger(HealthCheckController.class);


    @RequestMapping("/healthz")
    @ResponseStatus(code = HttpStatus.OK)
    public void healthCheck(){
        logger.info("Reached: GET /healthz");
        statsDClient.increment("endpoint.http.getHealthz");
        return;
    }
}

