package com.org;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
public class RemoteService {

    public static void main(String[] args) {
        System.setProperty("server.tomcat.threads.max", "1000");
        SpringApplication.run(RemoteService.class, args);
    }

    @RestController
    public static class MyController {
        // VM Option : -Dserver.port=8081
        @GetMapping("/service")
        public String service(String req) throws InterruptedException {
            Thread.sleep(2000);
            return req + "/service";
        }
    }
}
