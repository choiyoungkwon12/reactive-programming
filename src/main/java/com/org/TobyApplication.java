package com.org;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Slf4j
@EnableAsync
@SpringBootApplication
public class TobyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TobyApplication.class, args);
    }

    @RestController
    public static class MyController {

        AsyncRestTemplate rt = new AsyncRestTemplate();

        @GetMapping("/rest")
        public ListenableFuture<ResponseEntity<String>> rest(int idx) {
            return rt.getForEntity("http://localhost:8081/service?req={req}",
                String.class, "hello" + idx);
        }

        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        emitter.send("<p>Stream" + i + "</p>");
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                }
            });

            return emitter;
        }

        @GetMapping("/callable")
        public Callable<String> async() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }
    }
}
