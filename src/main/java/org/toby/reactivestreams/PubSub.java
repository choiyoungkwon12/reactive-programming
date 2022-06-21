package org.toby.reactivestreams;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Reactive Streams - Operators
 * <p>
 * Publisher -> Data -> Subscriber
 */
public class PubSub {

    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, i -> i + 1).limit(10).collect(Collectors.toList()));

        /*
         * pub -> [Data1] -> mapPub -> [Data2] -> logSub
         *                  <- subscribe(logsub)
         *                   -> onSubscribe(s)
         *                   -> onNext
         *                   -> onNext
         *                  -> onComplete
         */

        Publisher<Integer> mapPub = mapPub(pub, (Function<Integer, Integer>)s -> s * 10);
        mapPub.subscribe(logSub());
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub,
        Function<Integer, Integer> integerIntegerFunction) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                pub.subscribe(s);
            }
        };
    }

    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext : " + integer);

            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(sub::onNext);
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
