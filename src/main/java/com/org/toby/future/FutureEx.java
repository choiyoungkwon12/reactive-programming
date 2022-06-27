package com.org.toby.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureEx {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        // Future를 callback 방식으로
        FutureTask<String> f = new FutureTask<>(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            return "Hello";
        });

        // 비동기 작업을 실행
        es.execute(f);

        System.out.println(f.isDone());
        Thread.sleep(2500);
        log.debug("Exit");
        System.out.println(f.isDone());
        log.debug(f.get());
    }
}
