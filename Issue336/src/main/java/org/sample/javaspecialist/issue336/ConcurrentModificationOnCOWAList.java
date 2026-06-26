package org.sample.javaspecialist.issue336;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentModificationOnCOWAList {

    void main() {
        var list = new CopyOnWriteArrayList<Integer>();
        Collections.addAll(list, 3, 1, 4, 1, 5, 9);
        
        var subList = list.subList(1, 4);
        System.out.println("subList = " + subList); //OK
        
        subList.add(1, 99);
        System.out.println("subList = " + subList); // OK
        
        list.add(1, 42);
        System.out.println("list = " + list); // OK
        
//        System.out.println("subList = " + subList); // stops working if the original list is changed
    }
}
