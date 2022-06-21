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

        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
        Publisher<Integer> map2Pub = mapPub(mapPub, s -> -s);
        map2Pub.subscribe(logSub());
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub,
        Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        sub.onNext(f.apply(integer));
                    }

                    @Override
                    public void onError(Throwable t) {
                        sub.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        sub.onComplete();
                    }
                });
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
