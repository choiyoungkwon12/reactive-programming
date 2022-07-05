package com.org.toby;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(10);

        CompletableFuture
            .supplyAsync(() -> {
                log.info("runAsync");
                return 1;
            })
            .thenCompose(s -> {
                log.info("thenApply {}", s);
                return CompletableFuture.completedFuture(s + 1);
            })
            .thenApplyAsync(s2 -> {
                log.info("thenApply {}", s2);
                return s2 * 3;
            }, es)
            .exceptionally(e -> -10)
            .thenAcceptAsync(s3 -> log.info("thenAccept {} ", s3), es);

        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);

        es.shutdown();
    }

}
