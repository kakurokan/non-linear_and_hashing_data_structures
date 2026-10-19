# Data Structures and Time Complexity Analysis (AED - Trabalho 3)

[![CI](https://github.com/kakurokan/non-linear_and_hashing_data_structures/actions/workflows/ci.yml/badge.svg)](https://github.com/kakurokan/non-linear_and_hashing_data_structures/actions/workflows/ci.yml)

A Java implementation of custom non-linear and hashing data structures developed for the Algorithms and Data Structures (*Algoritmos e Estruturas de Dados* - AED) course. This project focuses on custom tree structures, hash table bucket strategies, and empirical temporal analysis benchmarking.

---

## 📌 Features

- **Custom Tree Implementations (`aed.trees`):**
  - Node contract interface (`IUAlgTreeNode`) defining structural tree node behaviors.
  - Concrete tree data structure (`UAlgTree`) supporting fundamental tree operations and traversals.

- **Hash Table Structures (`aed.tables`):**
  - Bucket abstraction interface (`IUAlshBucket`) for collision handling.
  - Hash table implementation (`UAlshTable`) with customized hashing and bucket management.

- **Benchmarking & Time Complexity Analysis (`aed.utils`):**
  - Performance measurement tools (`TemporalAnalysisUtils`) for empirical runtime evaluations.
  - Automated execution harness via `Main.java` to test structure operations across varying dataset sizes.

---

## 🏗 Project Structure


```text
Trabalho3AED/
├── .github/workflows/ci.yml                     # CI: runs mvn -B test on every push and PR
├── pom.xml                                      # Maven build (Java 17, JUnit 5)
└── src/
    ├── main/java/aed/
    │   ├── Main.java                            # Main execution & benchmarking entry point
    │   ├── tables/
    │   │   ├── IUAlshBucket.java                # Bucket interface for hash collision handling
    │   │   └── UAlshTable.java                  # Hash table data structure implementation
    │   ├── trees/
    │   │   ├── IUAlgTreeNode.java               # Tree node interface
    │   │   └── UAlgTree.java                    # Custom tree structure implementation
    │   └── utils/
    │       └── TemporalAnalysisUtils.java       # Utility methods for execution time benchmarking
    └── test/java/aed/
        ├── tables/UAlshTableTest.java           # JUnit 5 tests for the hash table
        └── trees/UAlgTreeTest.java              # JUnit 5 tests for the tree
```

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher.
- **Apache Maven:** Version 3.9 or higher.

### Compilation & Execution

#### 1. Compile the Project
From the root directory, compile all source files into `target/classes`:

```bash
mvn compile
```

#### 2. Run the Benchmark

Execute the `Main` class to run data structure operations and print temporal analysis metrics:

```bash
java -cp target/classes aed.Main
```

---

## 🧪 Build and test

The project uses a standard Maven layout (`src/main/java`, `src/test/java`). JUnit 5 tests cover `UAlgTree` and `UAlshTable`, including seeded randomized comparisons against `java.util.TreeMap` and `java.util.HashMap` and checks of the tree's size/weight invariants.

```bash
mvn test
```

Continuous integration runs `mvn -B test` on every push and pull request (`.github/workflows/ci.yml`); the badge at the top of this file shows the latest result.

---

## 📊 Benchmarking & Analysis

The utility class `TemporalAnalysisUtils` measures operation runtimes (e.g., search, insertion, and deletion) to validate theoretical asymptotic bounds ($O(1)$, $O(\log n)$, $O(n)$) against experimental results.

```
