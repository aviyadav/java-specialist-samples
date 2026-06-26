# Issue336

Sample code for JavaSpecialists Issue 336. This module explores how `CopyOnWriteArrayList.subList()` behaves when the backing list changes, and compares a few ways to take a small, stable slice from a `CopyOnWriteArrayList`.

## What is included

- `ConcurrentModificationOnCOWAList` shows that a sub-list from `CopyOnWriteArrayList` can stop being usable after the original list is structurally modified.
- `ImmutableSubList` is a small immutable `AbstractList` implementation that copies a requested range from a `CopyOnWriteArrayList`.
- `ImmutableSubListDemo` compares several approaches:
  - `l.subList(0, 2)`, which can fail under concurrent modification
  - copying the full list before taking a sub-list
  - using `ImmutableSubList`
  - using `stream().limit(2).toList()`

## Requirements

- JDK 25 or newer
- Maven 3.9 or newer

The module is part of the parent `java-specialist-samples` Maven project and is built as a jar.

## Build

From this directory:

```powershell
mvn compile
```

From the repository root:

```powershell
mvn -pl Issue336 compile
```

## Run the demos

Run the benchmark-style comparison:

```powershell
mvn exec:java -Dexec.mainClass=org.sample.javaspecialist.issue336.ImmutableSubListDemo
```

Run the focused `CopyOnWriteArrayList.subList()` behavior demo:

```powershell
mvn exec:java -Dexec.mainClass=org.sample.javaspecialist.issue336.ConcurrentModificationOnCOWAList
```

The current module `pom.xml` sets `exec.mainClass` to `org.sample.javaspecialist.issue336.Issue336`, but that class is not present in this module. Use one of the explicit `-Dexec.mainClass=...` commands above when running the examples.

## Package

```powershell
mvn package
```

## Notes

`ImmutableSubListDemo` intentionally runs repeated timed loops and concurrent mutation checks. Its output varies by machine and JVM run, so treat the numbers as local comparison data rather than fixed benchmark results.
