import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Build the data layer
        NetworkGraph graph = new NetworkGraph();
        graph.generateGrid(); // Creates our 6x6 grid

        // Grab the top-left router (Start) and bottom-right router (End)
        Router start = graph.allRouters.get(0);
        Router end = graph.allRouters.get(graph.allRouters.size() - 1);

        // 2. Boot up the logic layer (Your code)
        SimulationController controller = new SimulationController(graph);
        controller.runSimulation(start, end); // Run it once before drawing the screen

        // 3. Boot up the UI layer
        JFrame frame = new JFrame("PacketTracer: DAA Algorithm Visualizer");
        PacketPanel panel = new PacketPanel(graph, controller, start, end);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.add(panel);
        frame.setLocationRelativeTo(null); // Centers the window on your monitor
        frame.setVisible(true);
    }
}