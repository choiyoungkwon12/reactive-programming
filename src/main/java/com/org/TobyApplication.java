package com.org;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
            // ??????????????? ???????????? ?????????????????? ???????????? ????????? ?????? ???????????? ???????????? DeferredResult??? ?????? ?????????
            // ??? ?????? ???????????? ??????
            DeferredResult<String> dr = new DeferredResult<>();

/*            Completion
                .from(rt.getForEntity(URL1, String.class, "hello" + idx))
                .andApply(s -> rt.getForEntity(URL2, String.class, s.getBody()))
                .andApply(s -> myService.work(s.getBody()))
                .andError(e -> dr.setErrorResult(e))
                .andAccept(s -> dr.setResult(s));*/

            toCF(rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx))
                .thenCompose(s -> toCF(rt.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody())))
                .thenApplyAsync(s2 -> myService.work(s2.getBody()))
                .thenAccept(s -> dr.setResult(s))
                .exceptionally(e -> {dr.setErrorResult(e.getMessage()); return (Void)null;});


            /*log.info("rest3");
            f1.addCallback(s -> {
                log.info("rest1");
                ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}",
                    String.class, s.getBody());
                log.info("rest2");
                f2.addCallback(s2 -> {
                    ListenableFuture<String> f3 = myService.work(s2.getBody());
                    f3.addCallback(s3 -> {
                        dr.setResult(s3);
                    }, ex -> {
                        dr.setErrorResult(ex.getMessage());
                    });
                }, e -> {
                    dr.setErrorResult(e.getMessage());
                });

            }, e -> {
                dr.setErrorResult(e.getMessage());
            });*/

            return dr;
        }


        // listenableFuture??? completableFuture??? ?????????
        <T> CompletableFuture<T> toCF(ListenableFuture<T> lf) {
            CompletableFuture<T> cf = new CompletableFuture<T>();
            lf.addCallback(cf::complete, cf::completeExceptionally);
            return cf;
        }
    }

    public static class AcceptCompletion<S> extends Completion<S, Void> {

        Consumer<S> con;

        public AcceptCompletion(Consumer<S> con) {
            this.con = con;
        }

        @Override
        protected void run(S value) {
            con.accept(value);
        }
    }

    public static class ErrorCompletion<T> extends Completion<T, T> {

        Consumer<Throwable> econ;

        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        protected void run(T value) {
            if (next != null) {
                next.run(value);
            }
        }

        @Override
        protected void error(Throwable e) {
            econ.accept(e);
        }
    }

    public static class ApplyCompletion<S, T> extends Completion<S, T> {

        Function<S, ListenableFuture<T>> fn;

        public ApplyCompletion(Function<S, ListenableFuture<T>> fn) {
            this.fn = fn;
        }

        @Override
        protected void run(S value) {
            ListenableFuture<T> lf = fn.apply(value);
            lf.addCallback(s -> complete(s), e -> error(e));
        }

    }

    public static class Completion<S, T> {

        Completion next;

        public static <S, T> Completion<S, T> from(ListenableFuture<T> lf) {
            Completion<S, T> completion = new Completion<>();
            lf.addCallback(s -> {
                completion.complete(s);
            }, e -> {
                completion.error(e);
            });
            return completion;
        }

        public <V> Completion<T, V> andApply(Function<T, ListenableFuture<V>> fn) {
            Completion<T, V> c = new ApplyCompletion<>(fn);
            this.next = c;
            return c;
        }

        public void andAccept(Consumer<T> con) {
            Completion<T, Void> c = new AcceptCompletion<>(con);
            this.next = c;
        }

        public Completion<T, T> andError(Consumer<Throwable> econ) {
            Completion<T, T> c = new ErrorCompletion<>(econ);
            this.next = c;
            return c;
        }

        protected void error(Throwable e) {
            if (next != null) {
                next.error(e);
            }
        }

        protected void complete(T s) {
            if (next != null) {
                next.run(s);
            }
        }

        protected void run(S value) {

        }
    }

    @Service
    public static class MyService {

        public String work(String req) {
            return req + "/asyncwork";
        }
    }
}


