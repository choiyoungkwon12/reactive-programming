package com.org;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@EnableAsync
@SpringBootApplication
public class TobyApplication {


    @Autowired
    private MyService myService;

    public static void main(String[] args) {
        try (ConfigurableApplicationContext c = SpringApplication.run(TobyApplication.class, args);) {
        }
    }

    @Bean
    ThreadPoolTaskExecutor threadPool() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();

        /**
         * 10개가 기본으로 코어로 만들어놓고 더 요청이 많아지면 100개까지 만든다 가 아님
         * 기본 10개 스레드한테 작업 할당 이후 큐가 먼저 차고 그 큐에도 다 차게되면 스레드 풀에 있는 스레들을 maxPool 만큼 늘림
         * */
        te.setCorePoolSize(10);
        te.setMaxPoolSize(100);
        te.setQueueCapacity(200);
        te.setThreadNamePrefix("myThread");
        te.initialize();
        return te;
    }

    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("run()");
            ListenableFuture<String> result = myService.hello();
            result.addCallback(System.out::println, ex -> System.out.println(ex.getMessage()));
            log.info("exit");
        };
    }

    @Component
    public static class MyService {

        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            log.info("인터럽트떄문에 안찍힘?");
            return new AsyncResult<>("hello");
        }
    }
}
