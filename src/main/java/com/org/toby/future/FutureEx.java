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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        /**
         * 비즈니스 로직이 담겨있고 성공 했을 때 수행되는 코드, 실패 했을 때 수행되는 코드, 비동기 작업을 실행,
         * 실행 후 스레드 풀 종료와 같은 성격이 다른 코드가 한 곳에 모여있음.
         * ⇒ 두 가지 다 잘 알면 자유자재로 사용하겠지만 사실 좋은 코드는 아니고 분리 할 수 있으면 더 좋음.
         */
        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if (1 == 1) {
                throw new RuntimeException("Async ERROR");
            }
            log.debug("Async");
            return "Hello";
        },
            s -> System.out.println("result : " + s),
            t -> System.out.println("error : " + t.getMessage()));

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

    interface SuccessCallback {

        void onSuccess(String result);
    }

    interface ExceptionCallback {

        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {

        SuccessCallback sc;
        ExceptionCallback ec;

        public CallbackFutureTask(Callable<String> callable, SuccessCallback successCallback, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(successCallback);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                // get에서 실행중 exception 발생
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // 에러 전달
                ec.onError(e.getCause());
            }
        }
    }
}
