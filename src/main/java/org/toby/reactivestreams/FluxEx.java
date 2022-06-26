package org.toby.reactivestreams;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Flux;

public class FluxEx {

    public static void main(String[] args) throws InterruptedException {
        /*Flux.range(1, 10)
            .publishOn(Schedulers.newSingle("pub"))
            .log()
            .subscribeOn(Schedulers.newSingle("sub"))
            .subscribe(System.out::println);*/
        // 이 코드를 실행시키면 계속 프로세스를 종료시켜야하는데 내부적으로 스레드풀을 하나 만들어서 스레드를 할당하게 하는데 한번만 쓰고 버리는 것이 아니라,
        // publisher는 한번 만들고 subsciber를 여러개를 붙일 수 있음. 그래서 반복 사용이 가능하고, 그래서 얘는 스레드풀이 만들고지고 계속 유지가 된다.
        // 만약 톰캣, 네티, 컨테이너 환경에서 돌리면 그 안에서 얘가 관리가 될텐데 메인에서는 그게 아니라서 프로세스를 종료시켜준거임.

        // interval안에서 만드는 스레드는 데몬스레드라서 sleep을 걸어줘야 확인가능
        Flux.interval(Duration.ofMillis(200))
            .take(10)
            .subscribe(s -> System.out.println("[" + Thread.currentThread().getName() + "] onNext : " + s));

        TimeUnit.SECONDS.sleep(5);
    }
}
