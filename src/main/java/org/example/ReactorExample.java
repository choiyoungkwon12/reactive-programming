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

        /**
         * 이 방식은 비효율적임.
         *  distinctFruits와 countFruitsMono모두 Flux.fromIterable(basket)로부터 시작해서 각각 basket을 독립적으로 순회합니다.
         *  절차 지향으로 생각하면 하나의 for each loop 안에서 2가지를 한 번에 해결할 수 있는데 여기서는 총 2번 basket을 순회하고,
         *  특별히 스레드를 지정하지 않았기 때문에 동기, 블록킹 방식으로 동작합니다.
         *  논 블록킹 라이브러리의 장점을 전혀 살릴 수 없고, 효율성도 떨어집니다. 단순히 Reactor에서 제공하는 연산자들의 조합의 코드일 뿐입니다.
         */
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
