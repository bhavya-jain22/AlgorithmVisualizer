public class SimulationController {

    // The shared map that Vansh built
    private NetworkGraph graph;

    // We store the results here. This acts as a cache so Bhavya's UI
    // can just grab the data to draw the scoreboard without rerunning the math.
    private PathResult latestDijkstra;
    private PathResult latestAStar;
    private PathResult latestBellman;

    // Constructor: we need the graph to exist before we can control it
    public SimulationController(NetworkGraph graph) {
        this.graph = graph;
    }

    // The main trigger function.
    // The UI calls this every time a cable is broken or healed.
    public void runSimulation(Router start, Router end) {

        // 1. Run Vansh's baseline (Dijkstra)
        DijkstraSolver dSolver = new DijkstraSolver();
        latestDijkstra = dSolver.solve(graph, start, end);

        // 2. Run my optimized algorithm (A*)
        AStarSolver aSolver = new AStarSolver();
        latestAStar = aSolver.solve(graph, start, end);

        // 3. Run Bhavya's brute-force algorithm (Bellman-Ford)
        BellmanFordSolver bSolver = new BellmanFordSolver();
        latestBellman = bSolver.solve(graph, start, end);

        // At this point, the "race" is over. All three results are saved in memory.
    }

    // --- Simple Getter Methods for Bhavya's UI ---

    public PathResult getDijkstraResult() {
        return latestDijkstra;
    }

    public PathResult getAStarResult() {
        return latestAStar;
    }

    public PathResult getBellmanResult() {
        return latestBellman;
    }
}