package dev.danvega.boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    /*
     * Trailing Slash Matching Configuration change:
     * @GetMapping - will no longer work without {WebConfiguration.class}
     */
    @GetMapping
    public String home() {
        return "Hello, World!";
    }


}
