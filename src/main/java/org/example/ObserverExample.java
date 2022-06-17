package org.example;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObserverExample {

    static class IntObservable extends Observable implements Runnable{

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                 setChanged();
                 notifyObservers(i);     // push 방식 <-> int i = it.next() // pull 방식
            }
        }
    }

    public static void main(String[] args) {
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        IntObservable intObservable = new IntObservable();
        intObservable.addObserver(observer);

        // 다른 스레드에서 수행
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(intObservable);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        executorService.shutdown();

        intObservable.run();
    }

}
