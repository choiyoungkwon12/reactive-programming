package com.org.toby.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureEx {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<Integer> future = new CompletableFuture<>();
        // cached 스레드 풀은 maximum 제한이 없고, 처음에는 스레드가 미리 만들어있지 않고 요청시 생성
        ExecutorService es = Executors.newCachedThreadPool();

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            es.shutdown();
            return "Hello";
        });

        // future의 get은 submit의 비동기 작업이 완료될때까지 기다림. 그래서 async -> hello -> exit 순서로 log가 찍힘
        // future의 get은 Blocking 메서드임.
        // 이 코드에서는 스레드 풀 만들어서 비동적으로 작업을 할 필요가 없음. 그냥 메인에서 쭉 실행하는 것과 같음.
        log.debug(f.get());
        log.debug("Exit");
    }
}
