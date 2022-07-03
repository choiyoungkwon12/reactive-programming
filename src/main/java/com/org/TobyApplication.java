package com.org;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
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

    @Slf4j
    @RestController
    public static class MyController {

        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}", String.class,
                "hello" + idx);
            log.info("rest3");
            f1.addCallback(s -> {
                log.info("rest1");
                ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}",
                    String.class, s.getBody());
                log.info("rest2");
                f2.addCallback(s2 -> {
                    log.info("응답 옴");
                    dr.setResult(s2.getBody());
                }, e -> {
                    dr.setErrorResult(e.getMessage());
                });

            }, e -> {
                dr.setErrorResult(e.getMessage());
            });

            return dr;
        }
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


