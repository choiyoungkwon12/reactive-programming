package org.example;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorExample {

    public static void main(String[] args) {
        final List<String> basket1 = Arrays.asList("kiwi", "orange", "lemon", "orange", "lemon", "kiwi");
        final List<String> basket2 = Arrays.asList("banana", "lemon", "lemon", "kiwi");
        final List<String> basket3 = Arrays.asList("strawberry", "orange", "lemon", "grape", "strawberry");
        final List<List<String>> baskets = Arrays.asList(basket1, basket2, basket3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        Flux<FruitInfo> fruitInfoFlux = basketFlux.concatMap(basket -> {
            final Mono<List<String>> distinctFruits = Flux.fromIterable(basket).distinct().collectList();
            final Mono<Map<String, Long>> countFruitMono = Flux.fromIterable(basket)
                .groupBy(fruit -> fruit)
                .concatMap(groupedFlux -> groupedFlux.count().map(count -> {
                    Map<String, Long> fruitCount = new LinkedHashMap<>();
                    fruitCount.put(groupedFlux.key(), count);
                    return fruitCount;
                }))
                .reduce((accumulatedMap, currentMap) -> new LinkedHashMap<String, Long>() {
                    {
                        putAll(accumulatedMap);
                        putAll(currentMap);
                    }
                });
            return Flux.zip(distinctFruits, countFruitMono, FruitInfo::new);
        });

        fruitInfoFlux.subscribe(System.out::println);
    }

}
