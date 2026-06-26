package org.sample.javaspecialist.issue336;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ImmutableSubListDemo {

    void main() {
        for (var i = 0; i < 10; i++) {
            testAll();
        }
    }

    private void testAll() {
        test("broken", l -> new ArrayList<>(l.subList(0, 2)));
        test("fixed", l -> new ArrayList<>(l).subList(0, 2));
        test("fast", l -> new ImmutableSubList<>(l, 0, 2));
        test("AI", l -> l.stream().limit(2).toList());
        System.out.println("-".repeat(80));
    }

    /**
     * Returns a subList of the first two elements from a
     * {@link CopyOnWriteArrayList}.
     */
    private interface Subber extends Function<
            CopyOnWriteArrayList<Integer>, List<Integer>> {
    }

    private static final List<Integer> TINY
            = List.of(60, 50);
    private static final List<Integer> HUNDRED
            = IntStream.range(0, 100).boxed().toList();
    private static final List<Integer> THOUSAND
            = IntStream.range(555, 1555).boxed().toList();

    private void test(String experiment, Subber subber) {
        System.out.println(experiment);
        System.out.println(experiment.replaceAll(".", "="));
        checkCorrectness(subber);
        measure("tiny", TINY, subber);
        measure("100", HUNDRED, subber);
        measure("1000", THOUSAND, subber);
        System.out.println();
    }

    private void measure(String set,
            List<Integer> data,
            Subber subber) {
        var cow = new CopyOnWriteArrayList<Integer>(data);
        System.out.print(set + ": ");
        measure(() -> subber.apply(cow));
    }

    private volatile List<Integer> leak;

    private void measure(Supplier<List<Integer>> task) {
        var running = new AtomicBoolean(true);
        ForkJoinPool.commonPool().schedule(
                () -> running.set(false),
                3, TimeUnit.SECONDS);
        var repeats = 0L;
        while (running.get()) {
            leak = task.get();
            repeats++;
        }
        System.out.printf("subLists = %,d%n", repeats);
    }

    private void checkCorrectness(Subber subber) {
        System.out.print("Testing: ");
        var cow = new CopyOnWriteArrayList<Integer>();
        Collections.addAll(cow, 1, 2, 3, 4, 5);
        var testing = new AtomicBoolean(true);
        ForkJoinPool.commonPool().schedule(
                () -> testing.set(false),
                1, TimeUnit.SECONDS);
        var thread = Thread.ofPlatform().start(() -> {
            while (testing.get()) {
                cow.addLast(42);
                cow.removeLast();
                cow.addFirst(99);
                cow.removeFirst();
            }
        });
        try {
            while (testing.get()) {
                subber.apply(cow);
            }
            try {
                // test with empty input list
                subber.apply(new CopyOnWriteArrayList<>());
                System.out.println("Expected "
                        + "IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Probably correct");
            }
        } catch (ConcurrentModificationException e) {
            System.out.println(e.getClass().getSimpleName());
        }
    }
}
