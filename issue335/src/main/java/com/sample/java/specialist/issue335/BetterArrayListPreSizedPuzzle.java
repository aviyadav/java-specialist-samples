package com.sample.java.specialist.issue335;

public class BetterArrayListPreSizedPuzzle extends
        BetterListPuzzle {
    public static void main(String... args) {
        new BetterArrayListPreSizedPuzzle()
                .run(() -> new java.util.ArrayList<>(
                        100_000_000));
    }
}