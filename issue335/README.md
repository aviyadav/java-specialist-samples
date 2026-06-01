# Small Maven-based Java samples that explore list performance and array resizing behavior.

https://www.javaspecialists.eu/archive/Issue335-ZGC-Mysteries.html

The repository currently contains two sample areas:

- `issue334`: a baseline list benchmark harness with `ArrayList` and `LinkedList` runners
- `issue335`: an extended benchmark layer that adds warmups, repeated runs, and an array resize demo

## Project Layout

```text
java-specialist-samples/
├── pom.xml
├── issue334/
│   ├── pom.xml
│   └── src/main/java/com/sample/java/specialist/issue334/
│       ├── ListPuzzle.java
│       ├── ArrayListPuzzle.java
│       └── LinkedListPuzzle.java
└── issue335/
    ├── pom.xml
    └── src/main/java/com/sample/java/specialist/issue335/
        ├── BetterListPuzzle.java
        ├── BetterArrayListPuzzle.java
        ├── BetterArrayListPreSizedPuzzle.java
        ├── BetterLinkedListPuzzle.java
        └── ArrayResizingEvents.java
```

## What The Code Does

### `issue334`

`ListPuzzle` is the core benchmark harness. It creates a `List<Integer>` from a supplied factory, adds `42` one hundred million times, and reports wall-clock time plus current-thread CPU and user time using `ThreadMXBean`.

`ArrayListPuzzle` and `LinkedListPuzzle` are thin wrappers that run the harness with `ArrayList` and `LinkedList` respectively.

### `issue335`

`BetterListPuzzle` extends the baseline harness with two phases:

- warmups, which default to 10 runs
- actual runs, which default to 30 runs and report a total time summary

`BetterArrayListPuzzle` runs the benchmark with a normal `ArrayList`.

`BetterArrayListPreSizedPuzzle` runs the benchmark with an `ArrayList` pre-sized to 100,000,000 elements to reduce resizing overhead.

`BetterLinkedListPuzzle` runs the benchmark with `LinkedList`.

`ArrayResizingEvents` is a small helper that prints the growth steps for an array-backed list-sized capacity estimate until it reaches 100,000,000.

## Requirements

- JDK 24
- Maven

The code is configured for Java 24 in the Maven POM files.

## Build

Build the modules from the repository root:

```bash
mvn clean package
```

If you want to build a single module, run Maven from that module directory.

## Run

The `issue335` module contains the clearest runnable entry points because it defines standard `public static void main(String... args)` methods.

Example runs from the `issue335` directory:

```bash
mvn exec:java -Dexec.mainClass=com.sample.java.specialist.issue335.BetterArrayListPuzzle
mvn exec:java -Dexec.mainClass=com.sample.java.specialist.issue335.BetterArrayListPreSizedPuzzle
mvn exec:java -Dexec.mainClass=com.sample.java.specialist.issue335.BetterLinkedListPuzzle
```

To inspect the resize growth trace:

```bash
mvn exec:java -Dexec.mainClass=com.sample.java.specialist.issue335.ArrayResizingEvents
```

## Article Notes

The original Issue 335 article compares `LinkedList`, unsized `ArrayList`, and pre-sized `ArrayList` across multiple garbage collectors and JDK versions. The headline result is that the fastest choice depends on which time you care about:

- `LinkedList` can look competitive on CPU time, but it can suffer very large elapsed-time spikes under ZGC.
- An unsized `ArrayList` is generally better than `LinkedList` in elapsed time, but still pays for repeated resizes.
- A pre-sized `ArrayList` is the most stable choice in these experiments.

### Java 25 With ZGC

| Structure | Time (`tim`) | CPU (`cpu`) | User (`usr`) | Notes |
| --- | ---: | ---: | ---: | --- |
| `LinkedList` | 64172 | 5209 | 4961 | Very slow elapsed time, with long stalls |
| `ArrayList` | 14047 | 14035 | 13356 | Better elapsed time than `LinkedList` |
| `ArrayList` pre-sized | 8688 | 8663 | 8450 | Best overall result in this set |

The article highlights that `LinkedList` can appear faster by CPU/user time while still being much worse in wall-clock time because ZGC may not keep up and can introduce long pauses.

### Java 25 By Collector

| Collector | `LinkedList` `tim` | `ArrayList` `tim` | Pre-sized `ArrayList` `tim` |
| --- | ---: | ---: | ---: |
| G1 | 131791 | 49509 | 18437 |
| Parallel | 32176 | 8901 | 7705 |
| Serial | 22051 | 10963 | 6842 |

The note in the article is that `LinkedList` is not automatically the best choice just because each add is cheap. The later cost of traversal, relocation, and garbage collection matters.

### Java 21 With ZGC

| Mode | `LinkedList` `tim` | `ArrayList` `tim` | Pre-sized `ArrayList` `tim` |
| --- | ---: | ---: | ---: |
| Default ZGC | 12453 | 12170 | 7333 |
| ZGC Generational | 37908 | 13328 | 7676 |

The article calls out a large jump in elapsed time for `LinkedList` under ZGC Generational, while the `ArrayList` results change only modestly.

### GC Behavior Observed In The Benchmarks

The article points to two different behaviors in the logs:

- `LinkedList` can trigger `Allocation Stall` events and long stop-the-world pauses when memory pressure builds up.
- `ArrayList` avoids those stalls, but minor collections can still show up in the timing profile.

### Profiling Summary

Async Profiler output in the article shows `java.util.ArrayList.add` as the top cost, followed by GC-related work such as copying, marking, filling, and relocation. The pre-sized `ArrayList` shifts the profile toward less resize-related work and more direct list mutation.

### Takeaway

For these experiments, the practical lesson is:

1. If you do not know the required size, an `ArrayList` is still often a better default than `LinkedList`.
2. If you do know the size, pre-sizing the `ArrayList` removes a major source of overhead.
3. Under ZGC, elapsed time can diverge sharply from CPU time, so both should be considered when benchmarking.

### Sample Output Shape

```text
Warmup: 10x
Actual runs: 30x
class java.util.LinkedList:
tim=64172,cpu=5209,usr=4961
class java.util.ArrayList:
tim=14047,cpu=14035,usr=13356
class java.util.ArrayList: (pre-sized)
tim=8688,cpu=8663,usr=8450
```

## Notes

- The `issue334` examples use instance `main()` methods, so they are mainly intended as supporting sample code for the benchmark harness.
- The benchmark loops are intentionally large, so the runs can take noticeable time and will produce a lot of console output.
