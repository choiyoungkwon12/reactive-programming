package com.org.toby.future;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureEx {

    interface SuccessCallback{
        void onSuccess(String result);
    }
    public static class CallbackFutureTask extends FutureTask<String>{
        SuccessCallback sc;
        public CallbackFutureTask(Callable<String> callable, SuccessCallback successCallback) {
            super(callable);
            this.sc = Objects.requireNonNull(successCallback);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            return "Hello";
        }, System.out::println);

        // Future를 callback 방식으로
        /*FutureTask<String> f = new FutureTask<>(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            return "Hello";
        }) {

            // 비동기 작업이 모두 수행이 끝나면 호출이 되는 훅같은 메서드드
            @Override
            protected void done() {
                try {
                    System.out.println(get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };*/

        es.execute(f);
        es.shutdown();
    }
}
