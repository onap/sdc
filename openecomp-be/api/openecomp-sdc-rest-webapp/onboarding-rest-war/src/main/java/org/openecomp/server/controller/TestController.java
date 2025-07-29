package org.openecomp.server.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pinging....";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from onboard-be app!";
    }
}
