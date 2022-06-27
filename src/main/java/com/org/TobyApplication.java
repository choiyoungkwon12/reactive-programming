package com.org;

import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Slf4j
@EnableAsync
@SpringBootApplication
public class TobyApplication {

    private final MyService myService;

    public TobyApplication(MyService myService) {
        this.myService = myService;
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext c = SpringApplication.run(TobyApplication.class, args);) {
        }
    }

    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("run()");
            Future<String> result = myService.hello();
            log.info("exit : {}", result.isDone());
            log.info("result : " + result.get());
        };
    }


    @Component
    public static class MyService {

        @Async
        public Future<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("hello");
        }
    }


}
