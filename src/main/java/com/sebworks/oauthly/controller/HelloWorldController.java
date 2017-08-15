package com.sebworks.oauthly.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Selim Eren Bekçe on 2016-10-20.
 */
@RestController
@RequestMapping("/api")
public class HelloWorldController {
    @RequestMapping("/hello")
    public String hello(){
        return "Hello, world!";
    }
}
