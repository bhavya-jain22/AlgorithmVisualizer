public class SimulationController {

    private NetworkGraph graph;

    private PathResult latestDijkstra;
    private PathResult latestAStar;
    private PathResult latestBellman;
    private PathResult latestBFS;

    public SimulationController(NetworkGraph graph) {
        this.graph = graph;
    }

    public void runSimulation(Router start, Router end) {
        DijkstraSolver dSolver = new DijkstraSolver();
        latestDijkstra = dSolver.solve(graph, start, end);

        AStarSolver aSolver = new AStarSolver();
        latestAStar = aSolver.solve(graph, start, end);

        BellmanFordSolver bSolver = new BellmanFordSolver();
        latestBellman = bSolver.solve(graph, start, end);

        BFSSolver bfsSolver = new BFSSolver();
        latestBFS = bfsSolver.solve(graph, start, end);
    }

    public PathResult getDijkstraResult() { return latestDijkstra; }
    public PathResult getAStarResult()    { return latestAStar; }
    public PathResult getBellmanResult()  { return latestBellman; }
    public PathResult getBFSResult()      { return latestBFS; }
}