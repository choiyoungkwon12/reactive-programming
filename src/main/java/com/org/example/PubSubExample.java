package com.org.example;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;

public class PubSubExample {

    public static void main(String[] args) {
        Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Publisher<Integer> publisher = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> iterator = itr.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        executorService.execute(() -> {
                            int i = 0;
                            try {
                                while (i++ < n){
                                    if (iterator.hasNext()){
                                        subscriber.onNext(iterator.next());
                                    }else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            }catch (RuntimeException e){
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> subscriber = new Subscriber<>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubcribe");
                this.subscription = subscription;
                this.subscription.request(1);
                // this.subscription.request(Long.MAX_VALUE); // 전부 다 받고 싶을 때
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " onNext" + item);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
                executorService.shutdown();
            }
        };

        publisher.subscribe(subscriber);

        try {
            executorService.awaitTermination(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }
}
