package com.org;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Slf4j
@EnableAsync
@SpringBootApplication
public class TobyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TobyApplication.class, args);
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

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(1);
        te.setMaxPoolSize(10);
        te.initialize();
        return te;
    }

    @Slf4j
    @RestController
    public static class MyController {

        static final String URL1 = "http://localhost:8081/service?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";
        @Autowired
        MyService myService;
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            Completion
                .from(rt.getForEntity(URL1, String.class, "hello" + idx))
                .andAccept(s -> dr.setResult(s.getBody()));

//
//            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}", String.class,
//                "hello" + idx);
//            log.info("rest3");
//            f1.addCallback(s -> {
//                log.info("rest1");
//                ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}",
//                    String.class, s.getBody());
//                log.info("rest2");
//                f2.addCallback(s2 -> {
//                    ListenableFuture<String> f3 = myService.work(s2.getBody());
//                    f3.addCallback(s3 -> {
//                        dr.setResult(s3);
//                    }, ex -> {
//                        dr.setErrorResult(ex.getMessage());
//                    });
//                }, e -> {
//                    dr.setErrorResult(e.getMessage());
//                });
//
//            }, e -> {
//                dr.setErrorResult(e.getMessage());
//            });

            return dr;
        }
    }

    public static class Completion {

        Completion next;
        Consumer<ResponseEntity<String>> con;

        public Completion(Consumer<ResponseEntity<String>> con) {
            this.con = con;
        }

        public Completion() {
        }

        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
            Completion completion = new Completion();
            lf.addCallback(s -> {
                completion.complete(s);
            }, e -> {
                completion.error(e);
            });
            return completion;
        }

        public void andAccept(Consumer<ResponseEntity<String>> con) {
            Completion c = new Completion(con);
            this.next = c;
        }

        private void error(Throwable e) {

        }

        private void complete(ResponseEntity<String> s) {
            if (next != null) {
                next.run(s);
            }
        }

        private void run(ResponseEntity<String> value) {
            if (con != null) {
                con.accept(value);
            }
        }
    }

    @Service
    public static class MyService {

        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + "/asyncwork");
        }
    }
}


