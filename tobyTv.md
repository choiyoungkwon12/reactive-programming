리액티브 프로그래밍

- 외부의 이벤트나 데이터가 발생하면 거기에 대응하는 방식으로 프로그래밍을 하는 것을 통틀어서 말할 수 있다.

### Iterable Interface

![image](https://user-images.githubusercontent.com/47075043/176705481-1528b385-c4d2-4119-8582-ffaed86fd9e1.png)


해당 인터페이스를 구현한 객체는 for-each문에서 타겟이 될 수 있다.

# Duality(쌍대성)

이해하기 쉽지않은 표현일 수 있는데 리액티브 프로그래밍으로 이야기 할 때 항상 하는 단어로 Duality가 언급됨.

- Iterable ↔ Observeralbe
- 궁극적인 기능은 같은데 반대 방향으로 표현하는 것이 쌍대성임.
    - 옵져버블과 이터러블이 쌍대성 관계
- Iterable
    - pull 방식
        - 받는 쪽에서 요청후에 끌어 받아서 사용하는 방식
- Observerable
    - push 방식
        - 어떤 데이터가 이벤트를 가지고있는 소스쪽에서 밀어넣어주는 방식
        - 사실 GoF의 옵저버 패턴에 잘 나와있음.


# Observer 패턴

옵저버 패턴은 push방식으로 장점이 많음.

→ 따로 미리 만들어놓은 데이터를 끌어오는 용도보다 훨씬 다이나믹하게 데이터가 만들어 내면(키보드 입력, db에서 가져오고…) 관심있어하는 모든 애들(옵저버)한테 전부 브로드캐스팅 할 수 있다.

Reactive Extentions을 만든 ms 개발자들은 옵저버 패턴은 부족하다. → 더 좋게 만들 수 있다.

2가지 지적.

1. 데이터를 전부 주어도 끝이라는  **Complete**라는 개념이 없음.
2. **Error** 처리에 대한 개념이 없음.
    1. 익셉션에 대한 처리를 할 수 없음.
        1. 예외를 받았지만 재시도 or 다른방식으로 시도등을 고려되어있지 않음.
    2. 익셉션의 발생이 버그때문에 발생할 수 있지만(이 경우 빠르게 대처해야 하지만) 복구 가능한 예외가 발생할 수 있음.

### 그래서 위 문제점 두가지를 추가해서 확장된 observable을 만들었음

→ 확장된 observer 패턴이 Reactive Programming의 큰 축중 하나임. (총 3가지 정도 있음)

# Reactive Streams (표준)

([https://www.reactive-streams.org/](https://www.reactive-streams.org/))

([https://reactivex.io/](https://reactivex.io/))

- JVM 언어를 이용하는 기술을 만드는 회사들이 reactive 관련된 기술들이 붐이 일어나는데 중구난방으로 막 개발하는 것이 아닌 적당한 레벨의 표준을 정한 것.
- Java9 API에서는 들어간 내용도 있음.

자바안에서 여러 프로젝트에서도 Reactive Streams 라는 표준을 지켜서 개발을 하고 있음.

엔진은 pivotal에서 만든 Reactor로 만들어짐.

리액터의 퍼블리셔가 만약 Rxjava에서 만들어진것이면 따로 어댑터를 만들 필요가 없을 수 있음.

Reactive-Streams의 동작 원리를 알면 다른 툴들을 볼 때 잘 보일 수 있음.

Reactive-Streams의 각 인터페이스가 준수해야 하는 스펙이 있음.([링크](https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.4/README.md#specification))

1. Publisher (11가지)
2. Subscriber (13가지)
3. Subscription (17가지)
4. Processor (2가지)

## Publisher

- 잠재적으로 한계가 없는 연속된 요소들을 제공하는것. Subscriber한테 받을 수 있도록 제공.
- 프로토콜 형태
    - onSubscribe : 무조건 필수로 호출해야 되는 메서드
    - onNext : 0…N번 호출할 수 있는 메서드
    - onError | onComplete: 옵셔널하게 호출될 수 있는데 둘 중 하나만 호출될 수 있음.


java9 Flow

퍼블리셔는 데이터를 주는쪽.

subscriber가 자기한테 데이터를 주라고 할 때 사용하는 메서드 subscribe만 있음.

![image](https://user-images.githubusercontent.com/47075043/176705714-5d619f75-5f73-4b69-afe8-220eb3e4c535.png)

subscription은 pubshlier와 subscriber의 중개역할을 해줌.

subscriber는 subscription을 통해 요청을 할 수 있음.

- push 방식인데 요청?? → 일반적인 요청인 아닌 백프레셔 / 퍼블리셔와 subscriber 사이에 속도차가 발생할 수 있는데 그것을 조절해야하는데 subsciption을 통해서 해결
    - ex) 데이터 만개,천만개,무한개 다 보내줘라는 방식이 될 수도 있지만, subscriber 현재 바쁘니 10개만 보내주고 나머지 대기하고 있어. 형태로 이루어 질 수 있음.
        - request(long n)에 파라미터로 갯수를 넣어주면 됨./ 요청을 하는 것이지 응답을 무조건 달라는 것은 아님  request를 호출하는 순간에 아직 퍼블리셔에 데이터가 없을 수 있음. 단지 흐름을 조절하기 위한 것임.


subscription은 퍼블리셔와 subscriber 사이에 백프레셔역할

백프레셔가 필요한 이유

- 퍼블리셔가 subsciber에 비해 매우 빠른경우 많은 데이터가 누수되거나 많은 버퍼가 필요할 수 있음.
- 반대로 퍼블리셔가 느린데 subscriber가 빠른경우도 있음.
- 둘의 속도가 맞으면 좋지만, 비동기적으로 바라보았을때, 스레드간이 아닌 서버-서버 서비스-서비스에서도 리액티브하게 처리가 가능한데 만약 처리속도가 안맞을 경우 가능하다면 생성자체를 지연시키는 것이 좋을 수 있다. 왜냐면 버퍼를 사용안하기 떄문에 메모리 사용률이 매우 줄어든다. (ex. netflix에서 발표한 자료가 있음)

# Reactive Streams - Operators

이런식으로 데이터가 오퍼레이터들을 거치면서 가공된 데이터가 Subscriber한테 가게 됨.

*`Publisher -> Data1 -> Operator -> Data2 -> Operator2 -> Data3 -> Subscriber`*

코드 구현한 것중 mapPub 같이 데이터를 가공하는 코드들을 모두 Operator라고 부른다.

토비의 리액티브 스트림스 - 2 (57:00부터 보면됨.)

reduce까지 했음 → mapPub을 가지고 operator구현한 것을 제네릭을 사용한것으로 할 예정

# Reactive Streams - Scheduler

사실 어떻게 동작하는지 잘 몰라도 사용법을 알면 사용할 수 있을 수 있지만, 기계적으로 사용법만 익혀서 사용하다보면 복잡해질수밖에 없는 비동기적인 작업을 단순하고 추상화하는게 리액티브 프로그래밍의 장점인데 복잡한 문제를 만났을 때 간결하게 표현하지 못하는 함정에 빠질 수 있다.

그래서 표준을 잘 알아야 함. → 프로토콜, + 거기서 구성되는 대표적인 두가지 pub/sub 어떻게 관계를 맺고 데이터를 주고받고 어떻게 시작하고 종료하는가에 대해서 알아야함.

만약 퍼블리셔가 subscribe하고 subsciber가 request하고 onNext, oncomplete하는 과정들을 블로킹, 동기 방식으로 진행된다면 예를들어, (사용자의 키보드 입력, 언제줄지 모르는 데이터를 받아오는 것을 기다리는 등의 작업이라면) 서버에서는 요청마다 스레드를 나누긴하지만 금방 스레드풀의 스레드가 꽉차고 큐도 꽉차서 서비스를 할 수 없다 라는 표시가 나타날것임.(ex. 메인에서 모두 처리)

그래서 작업을 처리하는 부분을 비동기 적으로 처리하려면 대표적으로 Reactive 프로그래밍에서 이야기하는 스케쥴러를 통해서 한다.

스케쥴러는 크게 두가지 방식으로 지정할 수 있다.

subscribeOn, publishOn

**subscribeOn** : publisher가 특별히 느린경우(ex. blocking I/O)와 그에 반해 consumer(subscriber)는 빠른경우 사용하면 좋다.

- 데이터를 생성하는데  오래걸리거나, 얼마나 걸릴지 예측할 수 없는  경우 subscribeOn을 사용해서 이후 데이터를 생성하는 작업부터 다  외부에서 돌리도록 함

**publishOn :** publisher의 데이터 생성 속도는 빠르지만 해당 데이터를 받아서 처리하는 consumer의 속도가 느린경우 별도의 스레드에서 작업을 처리하도록 사용할 수 있다.

- 만약  퍼블리셔가 여러개의 데이터를 빠르게 주고 처리가 1~5까지 있을 때 5부터 처리되면 어떻게 하느냐?
    - reactor, reactive-streams등 표준에서는 퍼블리셔가 데이터를 생성할 때, 멀티스레드로 분리가 돼서 onNext를 호출되지 않게 되어잇음. 반드시 단일스레드에서만 넘어가게 되어 있음 → 그래서 newSingleThreadExcutor를 사용한 것임.



---

지난 시간까지는 Reactive-Streams 라는 자바계열의 reactive 표준 api를 구현해보면서 기본적인 io와 동작을 감을 잡기 위함이었음.

리액티브를 api 몇가지로 생각하기에는 사실상 너무 큰 범위임.

- 그중 몇가지 핵심만 구현해본것임.

리액티브 프로그래밍은 사실 한쪽에서 데이터를 보내고(이벤트를 발생시키고) 받아보는 구조에서만 바라보는 것이 아니라 비동기적인 동작환경에서 어떤 의미를 가지는지를 알아야 더 좋음.

그 후에 비동기적인 관점에서 이전에 했던 api를 큰그림을 다시 그려보면 좋음

도대체 스프링이 뭐하러 기존의 스프링 MVC을 재끼고 리액티브 웹이라는 것을 전면에 내세워서 엔진까지 바꿔가면서(사용법은 유사하지만) 다른방향으로 접근을 시도했는가 어떤 문제를 해결하려고 시도했는가??

계속해서 10년도 전부터 스프링은 비동기를 위한 기능을 추가했음. 독자적인 비동기 기술이 아닌 자바 언어에서의 지원 비동기 기술, 오픈소스의 비동기 기술을 잘 조화해서 비동기 기술을 스프링에 녹여왔었음.

자바8의 비동기 기술등을 넘어서 리액티브를 도입하기까지의 중요한 차이점과 변화들.

비동기 기술을 위한 자바에서부터의 노력들…

# Future

- java 1.5에서 나옴.
- 가장 기본이 되는 인터페이스임. 잘 알아야 함.
- **비동기적인 작업을 수행하고 난 결과를 나타내는 것.**

다른 스레드에서 호출되는 결과를 가져오기 위한 가장 간단한 방법

```jsx
@Slf4j
public class FutureEx {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<Integer> future = new CompletableFuture<>();
        // cached 스레드 풀은 maximum 제한이 없고, 처음에는 스레드가 미리 만들어있지 않고 요청시 생성
        ExecutorService es = Executors.newCachedThreadPool();

        // f는 사실 비동기 작업의 리턴 값은 아니고 리턴값을 얻어올 방법을 제공할 핸들러 같은 것이지 사실 리턴값은 아님.
        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            es.shutdown();
            return "Hello";
        });

        // future의 get은 submit의 비동기 작업이 완료될때까지 기다림. 그래서 async -> hello -> exit 순서로 log가 찍힘
        // future의 get은 Blocking 메서드임.
        // future의 isDone은 작업의 결과를 기다리지 않고 작업이 끝났으면 true, 안끝났으면 false를 나타내는 함수임.
        // 루프를 돌면서 future의 작업이 끝났는지 isDone으로 확인하고 끝났으면 f.get해서 가져오고 안끝났으면 다른 작업을 하는 방법도 있음.
        System.out.println(f.isDone());
        Thread.sleep(2500);
        log.debug("Exit");
        System.out.println(f.isDone());
        log.debug(f.get());
    }
}
```

```java
CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if (1 == 1) {
                throw new RuntimeException("Async ERROR");
            }
            log.debug("Async");
            return "Hello";
        },
            s -> System.out.println("result : " + s),
            t -> System.out.println("error : " + t.getMessage()));

es.execute(f);
es.shutdown();
```

비즈니스 로직이 담겨있고 성공 했을 때 수행되는 코드, 실패 했을 때 수행되는 코드, 비동기 작업을 실행, 실행 후 스레드 풀 종료와 같은 성격이 다른 코드가 한 곳에 모여있음.

⇒ 두 가지 다 잘 알면 자유자재로 사용하겠지만 사실 좋은 코드는 아니고 분리 할 수 있으면 더 좋음.

```java
@GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        emitter.send("<p>Stream" + i + "</p>");
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                }
            });

            return emitter;
        }
```

emitter를 이용하면 http에서 한번에 모아서 결과를 주는게 아니라 sse 표준에 따라서 데이터를 streaming 방식처럼 response 해주면 그때그때 데이터가 클라이언트한테 넘어간다.
-> 브라우저의 경우 바로 출력이 됨.
