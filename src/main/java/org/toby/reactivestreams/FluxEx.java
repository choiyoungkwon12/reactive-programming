package org.toby.reactivestreams;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class FluxEx {

    public static void main(String[] args) {
        Flux.range(1,10)
            .log()
            .subscribeOn(Schedulers.newSingle("sub"))
            .subscribe(System.out::println);
    }
}
