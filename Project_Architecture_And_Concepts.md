# Packet Tracer Routing Simulator: Architecture and Concepts

This document outlines the core architecture and theoretical concepts behind both versions of the Packet Tracer Routing Simulator:
1. **PacketTracerProject (Enhanced Version)**: The fully interactive, GUI-driven visualization suite.
2. **PacketTracerProject copy (Baseline Version)**: The foundational logic containing the core networking components and routing engines.

---

## 1. Core Concepts & Graph Theory

At its heart, this project is a simulation of computer networking routing tables using **Graph Theory**.

* **Vertices (Nodes)**: Represented by the `Router` class. These are the intersections where data packets make decisions on where to go next.
* **Edges (Connections)**: Represented by the `Cable` class. These are the physical or virtual links between routers. They contain a `weight` (latency/delay) and an `isBroken` state.
* **Adjacency List**: Used in the `NetworkGraph` class, mapping each `Router` to a list of `Cable` connections. This is highly memory-efficient for sparse networks compared to an adjacency matrix.

## 2. Algorithms Implemented

The project implements several distinct categories of algorithms, making it a comprehensive DAA (Design and Analysis of Algorithms) submission:

### Category A: Shortest Path (Latency-Based)
* **Dijkstra's Algorithm**: The baseline greedy algorithm. It explores outwards in a circle, guaranteeing the absolute shortest path by checking nodes with the lowest known distance using a Priority Queue. `O(E log V)`
* **A* (A-Star) Search**: An optimized version of Dijkstra. It adds a "Heuristic" (a distance-to-target guess using physical X/Y coordinates). This acts like a compass, pulling the search directly toward the target, resulting in far fewer "Nodes Explored" while still guaranteeing the shortest path. `O(E log V)`
* **Bellman-Ford Algorithm**: A Dynamic Programming approach. Rather than exploring smartly, it brute-forces the network by "relaxing" all edges multiple times. It is much slower `O(V × E)` but is used in real life (e.g., RIP protocol) because it can handle negative edge weights.

### Category B: Shortest Path (Hop-Based)
* **BFS (Breadth-First Search)**: Focuses purely on finding the path with the fewest number of router "jumps" (hops), ignoring the cable latency weights entirely. Uses a standard FIFO Queue. `O(V + E)`

### Category C: Minimum Spanning Tree (Network Backbone)
* **Kruskal's Algorithm**: Uses the **Union-Find (Disjoint Set)** data structure. Instead of finding a path from A to B, this algorithm designs the cheapest possible physical infrastructure that connects *all* routers together without any redundant loops. `O(E log E)`

---

## 3. Software Architecture (MVC Pattern)

The enhanced version (`PacketTracerProject`) is built strictly on the **Model-View-Controller (MVC)** design pattern, which separates the math from the visuals.

### Model (The Logic & Data)
* `NetworkGraph`, `Router`, `Cable`, `PathResult`
* Holds the mathematical state of the grid, the coordinates, and the break statuses.

### Controller (The Orchestrator)
* `SimulationController`, `AStarSolver`, `DijkstraSolver`, `BellmanFordSolver`, `BFSSolver`, `KruskalMST`
* The Controller receives input (e.g., "Run a simulation from Router 1 to Router 40") and commands the Solvers to compute the data.

### View (The UI & Presentation)
* `PacketTracerGUI`, `NetworkPanel`, `BenchmarkChartGUI`, `StepByStepGUI`
* Built on Java Swing. It uses event listeners (like `MouseListener` for breaking cables) to communicate back to the Controller, and it uses `paintComponent` loops and Swing `Timers` to visually render the Model data to the screen.

## 4. Differences Between Folders

* **PacketTracerProject copy (Baseline)**: This folder represents the "backend" logic. It contains the structural data (`Router`, `Cable`) and the basic Solvers, serving as the mathematical backbone.
* **PacketTracerProject (Enhanced)**: This folder wraps the baseline logic in a highly complex presentation layer. It introduces Java Swing threaded timers for the "Race Mode", bulk processing loops for the "Harsh Benchmark", and completely new Solvers (BFS and Kruskal's) to add depth to the visual presentation.
