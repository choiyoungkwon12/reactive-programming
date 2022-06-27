package com.org.example;

import java.util.Iterator;

public class IteratorExample {

    public static void main(String[] args) {
        Iterable<Integer> iterable = () -> new Iterator<>() {
            int i = 0;
            final static int MAX = 10;

            @Override
            public boolean hasNext() {
                return i < MAX;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };

        for (Integer integer : iterable) {
            System.out.println(integer);
        }

        for(Iterator<Integer> iterator = iterable.iterator(); iterator.hasNext();){
            System.out.println(iterator.next());
        }
    }

}
