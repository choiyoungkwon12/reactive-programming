package com.org;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
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
                .andApply(s -> rt.getForEntity(URL2, String.class, s.getBody()))
                .andAccept(s -> dr.setResult(s.getBody()));

            return dr;
        }
    }

    public static class AcceptCompletion extends Completion {
        Consumer<ResponseEntity<String>> con;

        public AcceptCompletion(Consumer<ResponseEntity<String>> con) {
            this.con = con;
        }

        @Override
        protected void run(ResponseEntity<String> value) {
            con.accept(value);
        }
    }

    public static class ApplyCompletion extends Completion {
        Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn;
        public ApplyCompletion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
            this.fn = fn;
        }

        @Override
        protected void run(ResponseEntity<String> value) {
            ListenableFuture<ResponseEntity<String>> lf = fn.apply(value);
            lf.addCallback(s -> complete(s), e -> error(e));
        }

    }

    public static class Completion {

        Completion next;

        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
            Completion completion = new Completion();
            lf.addCallback(s -> {
                completion.complete(s);
            }, e -> {
                completion.error(e);
            });
            return completion;
        }

        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
            Completion c = new ApplyCompletion(fn);
            this.next = c;
            return c;
        }

        public void andAccept(Consumer<ResponseEntity<String>> con) {
            Completion c = new AcceptCompletion( con);
            this.next = c;
        }

        protected void error(Throwable e) {

        }

        protected void complete(ResponseEntity<String> s) {
            if (next != null) {
                next.run(s);
            }
        }

        protected void run(ResponseEntity<String> value) {

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


