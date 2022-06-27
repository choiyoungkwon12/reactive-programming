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

        // f는 사실 비동기 작업의 리턴 값은 아니고 리턴값을 얻어올 방법을 제공할 핸들러 같은 것이지 사실 리턴값은 아님.
        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            es.shutdown();
            return "Hello";
        });

        // future의 get은 submit의 비동기 작업이 완료될때까지 기다림. 그래서 async -> hello -> exit 순서로 log가 찍힘
        // future의 get은 Blocking 메서드임.
        // future의 isDone은 작업의 결과를 기다리지 않고 작업이 끝났으면 true, 안끝났으면 false를 나타내는 함수임.
        // 루프를 돌면서 future의 작업이 끝났는지 isDone으로 확인하고 끝났으면 f.get해서 가져오고 안끝났으면 다른 작업을 하는 방법도 있음.
        System.out.println(f.isDone());
        Thread.sleep(2500);
        log.debug("Exit");
        System.out.println(f.isDone());
        log.debug(f.get());
    }
}
