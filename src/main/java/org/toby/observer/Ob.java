package org.toby.observer;

import java.util.Arrays;
import java.util.Iterator;

public class Ob {

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
        for(Iterator<Integer> iterator = iter2.iterator();iterator.hasNext();){
            System.out.println(iterator.next());
        }
    }

}
