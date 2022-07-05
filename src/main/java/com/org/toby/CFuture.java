package com.org.toby;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture
            .supplyAsync(() -> {
                log.info("runAsync");
                return 1;
            })
            .thenApply(s -> {
                log.info("thenApply {}", s);
                return s + 1;
            })
            .thenApply(s2 -> {
                log.info("thenApply {}", s2);
                return s2 * 3;
            })
            .thenAccept(s3 -> log.info("thenAccept {} ", s3));

        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }

}
