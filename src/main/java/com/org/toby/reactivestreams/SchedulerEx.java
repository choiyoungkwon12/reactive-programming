package com.org.toby.reactivestreams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SchedulerEx {

    public static void main(String[] args) {
        Publisher<Integer> publisher = s -> {
            s.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    System.out.println(Thread.currentThread().getName() + " : request");
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onNext(4);
                    s.onNext(5);
                    s.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        };

        Publisher<Integer> subOnPub = sub -> {
            ExecutorService ex = Executors.newSingleThreadExecutor();
            ex.execute(() -> {publisher.subscribe(sub);});
            ex.shutdown();
        };

        Publisher<Integer> pubOnPub = sub -> {
            subOnPub.subscribe(new Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor();
                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    es.execute(() ->sub.onNext(integer));
                }

                @Override
                public void onError(Throwable t) {
                    es.execute(() -> sub.onError(t));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(sub::onComplete);
                    es.shutdown();
                }
            });
        };

        pubOnPub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println(Thread.currentThread().getName() + " : onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread().getName() + " : onNext " + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError" + t);
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " : onComplete");
            }
        });

        System.out.println(Thread.currentThread().getName() + " : exit");
    }


}
