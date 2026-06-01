package com.sample.java.specialist.issue335;

public class BetterLinkedListPuzzle extends BetterListPuzzle {
    public static void main(String... args) {
        new BetterLinkedListPuzzle()
                .run(java.util.LinkedList::new);
    }
}