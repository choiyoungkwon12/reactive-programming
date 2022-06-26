package org.toby.observer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class Ob {

    /**
     * Observable
     * java 1.0 부터 있던 인터페이스로 9부터는 deprecate 되었지만
     * reactive 프로그래밍을 학습하기 위해 Duality(iterable <-> Observable) 알아보기 위해 사용
     */
    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                // 새로운 변화가 생긴것을 setChanged로 호출
                setChanged();

                // notify 하면서 해당 데이터를 던질 수 있음
                notifyObservers(i);                     // push
                // int i = it.next();와 대응이 될 수 있다.  // pull
            }
        }
    }

    public static void main(String[] args) {
        // 이터러블 아래의 경우 1부터 10까지 1씩 더하면 됨.
        // 이터러블을 구현한 객체의 경우 for-each를 사용할 수 있음.
        // iterator는 이터러블의 여러 원소를 순회하는 실제 도구라고 생각하면 됨.
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Iterable<Integer> iter2 = () -> new Iterator<>() {
            int i = 0;
            final static int Max = 10;

            @Override
            public boolean hasNext() {
                return i < Max;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };

        // 여기서 오른쪽 변수를 iter를 넣으나 iter2를 넣으나 똑같은 역할을 한다.
//        for (Integer integer : iter2) {
//            System.out.println(integer);
//        }

        // java5 이전의 방식
        /*for(Iterator<Integer> iterator = iter2.iterator();iterator.hasNext();){
            System.out.println(iterator.next());
        }*/

        // Source -> Event/Data -> Observer(관찰자)한테 던짐
        // Source인 옵저버블을 만들고, Observer를 만들어서 옵저버블에 등록하고 이벤트가 발생됐을 때 등록되어있는 옵저버들한테 알림을 보낸다.
        // Reactive Streams에서는 Observer를 subscriber, Observable을 publisher라고도 한다.
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " : " +  arg);
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        System.out.println(Thread.currentThread().getName() + " EXIT");

        // 메인스레드가 아니라 이벤트가 언제 실행될 지 모르니 블록킹시키지 않고 별도의 스레드에서 비동기적으로 동작하도록 하기 위함.
        // 그렇게 동작 후 여러 스레드에서 동작하고 있는 옵저버들이 결과를 받을 수 있음.
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);
        es.shutdown();
    }

}
