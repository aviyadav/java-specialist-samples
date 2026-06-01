package com.sample.java.specialist.issue335;

public class BetterArrayListPuzzle extends BetterListPuzzle {
    public static void main(String... args) {
        new BetterArrayListPuzzle()
                .run(java.util.ArrayList::new);
    }
}