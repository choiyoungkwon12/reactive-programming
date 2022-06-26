package org.toby.reactivestreams;

import reactor.core.publisher.Flux;

public class FluxEx {

    public static void main(String[] args) {
        Flux.range(1,10)
            .log()
            .subscribe(System.out::println);
    }
}
