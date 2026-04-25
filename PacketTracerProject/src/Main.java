import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Booting up PacketTracer Backend Test ---");

        // 1. Initialize Vansh's Graph
        NetworkGraph graph = new NetworkGraph();
        graph.generateGrid();
        System.out.println(" Network Grid | graph Generated Successfully.");

        // Grab the start (top-left) and end (bottom-right) routers
        Router start = graph.allRouters.get(0);
        Router end = graph.allRouters.get(graph.allRouters.size() - 1);
        System.out.println("Routing from Router ID: " + start.id + " to Router ID: " + end.id + "\n");

        // 2. Pass the graph to your Simulation Controller
        SimulationController controller = new SimulationController(graph);

        // Run the algorithms!
        controller.runSimulation(start, end);

        // 3. Print the Proof to the Terminal
        System.out.println("====== ALGORITHMS RESULTS ======");

        PathResult aStar = controller.getAStarResult();
        System.out.println(" A*  Search :");
        System.out.println(" Nodes Visited : " + (aStar != null ? aStar.nodesVisited : "Fail"));
        System.out.println(" Total Latency : " + (aStar != null ? aStar.totalCost : "Fail"));

        PathResult dijkstra = controller.getDijkstraResult();
        System.out.println("\n Dijkstra's Algorithm :");
        System.out.println("  Nodes Visited : " + (dijkstra != null ? dijkstra.nodesVisited : "Fail"));
        System.out.println("  Total Latency : " + (dijkstra != null ? dijkstra.totalCost : "Fail"));

        PathResult bellman = controller.getBellmanResult();
        System.out.println("\nBellman-Ford Algorithm :");
        System.out.println("   -> Nodes Visited : " + (bellman != null ? bellman.nodesVisited : "Fail"));
        System.out.println("  Total Latency : " + (bellman != null ? bellman.totalCost : "Fail"));

        System.out.println("\n==========================================");
        
        System.out.println("Launching GUI Visualizer...");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            PacketTracerGUI gui = new PacketTracerGUI(graph, controller);
            gui.setVisible(true);
        });
    }
}