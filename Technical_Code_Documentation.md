# Technical Code Documentation

This document provides a technical breakdown of the classes and methods within the Packet Tracer Routing Simulator.

## 1. Graph Data Structures

### `Router.java`
Represents a node (intersection) in the network.
* `public class Router`
  * `public int id;` - Unique identifier for the router.
  * `public int x, y;` - Screen coordinates used for drawing and A* heuristics.

### `Cable.java`
Represents a directed edge between two routers.
* `public class Cable`
  * `public Router targetRouter;` - The destination of this cable.
  * `public int weight;` - The latency cost of traversing this cable.
  * `public boolean isBroken;` - State flag. If true, algorithms will ignore this cable.

### `NetworkGraph.java`
The core adjacency list managing the network structure.
* `public HashMap<Router, ArrayList<Cable>> adjList;`
* `public void addCable(Router a, Router b, int weight)`: Adds a bi-directional edge by pushing a `Cable` object into the lists of both `a` and `b`.
* **Topology Generators**: 
  * `public void generateGrid()`: Creates an 11x12 matrix of connected routers.
  * `public void generateRing() / generateStar() / generateMesh()`: Wipes the current graph and dynamically generates alternative network structures.

---

## 2. Controllers & Wrappers

### `SimulationController.java`
Acts as the central orchestrator (Controller in MVC).
* `public void runSimulation(Router start, Router end)`: Initializes and executes A*, Dijkstra, Bellman-Ford, and BFS simultaneously.
* `public PathResult getAStarResult()` (and similar getters): Retrieves the cached results for the GUI scoreboard without requiring recalculation.

### `PathResult.java`
A data transfer object (DTO) returned by all algorithms.
* `public List<Router> path;` - The sequential list of routers forming the correct route.
* `public int totalCost;` - The summed weight (latency) of the path.
* `public int nodesVisited;` - The number of routers the algorithm had to explore to find the path.

---

## 3. Algorithm Solvers

### `DijkstraSolver.java`
Greedy Shortest Path algorithm.
* `public PathResult solve(NetworkGraph graph, Router start, Router end)`:
  Uses a `PriorityQueue<DNode>` sorted by distance from start. Iteratively pops the node with the lowest distance and relaxes all its neighboring cables.

### `AStarSolver.java`
Heuristic-optimized Shortest Path algorithm.
* `public PathResult solve(NetworkGraph graph, Router start, Router end)`:
  Similar to Dijkstra but uses an $f(n) = g(n) + h(n)$ formula.
* `private double heuristic(Router a, Router b)`: Calculates the Euclidean distance `Math.sqrt(Math.pow(a.x - b.x, 2) + ...)` to bias the priority queue toward the physical direction of the destination.

### `BellmanFordSolver.java`
Dynamic Programming Shortest Path algorithm.
* `public PathResult solve(NetworkGraph graph, Router start, Router end)`:
  Iterates `V - 1` times. On each iteration, it loops through literally *every* edge in the network, relaxing distances. Highly inefficient but extremely robust.

### `BFSSolver.java`
Breadth-First Search for fewest-hops path.
* `public PathResult solve(NetworkGraph graph, Router start, Router end)`:
  Uses a standard `java.util.LinkedList` as a FIFO queue to spread outwards uniformly, completely ignoring cable weights.

### `KruskalMST.java`
Minimum Spanning Tree algorithm.
* `private int find(int x)` and `private boolean union(int a, int b)`: Implements the Disjoint Set Union (Union-Find) with path compression and union-by-rank.
* `public List<Edge> computeMST(NetworkGraph graph)`: Extracts all unique edges, sorts them by weight ascending, and iterates through them, adding edges to the final tree only if they do not cause a cycle in the Union-Find structure.

---

## 4. UI & Visualization (PacketTracerProject Enhanced)

### `PacketTracerGUI.java` (Extends `JFrame`)
The main application window.
* `private void runSinglePacket()`: Takes the active PathResult, clears the map, and starts the `animationTimer` to move a single `NetworkPanel.Packet` object.
* `private void startRace()`: Spawns 4 concurrent `Packet` objects with physical drawing offsets (-20, -7, 7, 20) so they don't overlap on the UI, and starts the race.
* `private void runHarshBenchmark()`: Disables the UI, starts a `javax.swing.Timer` running every 100ms for 50 iterations, randomly breaking 15% of cables on every tick to stress-test the algorithms, eventually launching `BenchmarkChartGUI`.

### `NetworkPanel.java` (Extends `JPanel`)
The canvas that physically draws the lines, nodes, and moving packets.
* `protected void paintComponent(Graphics g)`: Uses `Graphics2D` to draw the entire network. Broken cables are rendered as red `BasicStroke`.
* `public static class Packet`: An inner class representing a moving dot.
  * `public void moveStep(NetworkGraph graph)`: Calculates interpolation (`current.x + (next.x - current.x) * progress`) based on the specific cable's weight, making higher latency cables physically slower to cross.

### `StepByStepGUI.java` (Extends `JFrame`)
A granular debugging visualizer.
* `private void step(JPanel mapPanel)`: Triggered by the "Next Step" button. Executes a single loop iteration of the chosen algorithm (BFS or Dijkstra), moves nodes from `inFrontier` to `inVisited`, updates the `JList` queue model, appends to the log, and repaints.
