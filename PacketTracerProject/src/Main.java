import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.geom.Point2D;

public class Main {
    private static JFrame frame;
    private static PacketPanel currentPanel;
    private static JPanel mainContainer;

    public static void main(String[] args) {
        // Setup the Main Dashboard Window
        frame = new JFrame("PacketTracer: Enterprise Algorithm Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 750);
        frame.setLocationRelativeTo(null);

        mainContainer = new JPanel(new BorderLayout());
        frame.add(mainContainer);

        // Load the default Grid Map to start
        loadMap("GRID");

        // Build the bottom Control Panel
        mainContainer.add(buildControlPanel(), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // --- The Control Panel (Buttons) ---
    private static JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(45, 45, 45));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Algorithm Buttons
        JButton btnAStar = createButton("🟢 Run A*", Color.GREEN);
        JButton btnDijkstra = createButton("🟡 Run Dijkstra", Color.YELLOW);
        JButton btnBellman = createButton("🔵 Run Bellman", Color.CYAN);
        JButton btnCompare = createButton("🏆 Compare All", Color.WHITE);

        btnAStar.addActionListener(e -> currentPanel.setViewMode(PacketPanel.ViewMode.ASTAR));
        btnDijkstra.addActionListener(e -> currentPanel.setViewMode(PacketPanel.ViewMode.DIJKSTRA));
        btnBellman.addActionListener(e -> currentPanel.setViewMode(PacketPanel.ViewMode.BELLMAN));
        btnCompare.addActionListener(e -> currentPanel.setViewMode(PacketPanel.ViewMode.COMPARE));

        // Map Switching Buttons
        JButton btnMapGrid = createButton("🗺️ Grid Map", Color.LIGHT_GRAY);
        JButton btnMapStar = createButton("🗺️ Star Map", Color.LIGHT_GRAY);
        JButton btnMapRandom = createButton("🗺️ Random Map", Color.LIGHT_GRAY);

        btnMapGrid.addActionListener(e -> loadMap("GRID"));
        btnMapStar.addActionListener(e -> loadMap("STAR"));
        btnMapRandom.addActionListener(e -> loadMap("RANDOM"));

        // Add them to the panel
        controlPanel.add(new JLabel("<html><font color='white'><b>ALGORITHMS:</b></font></html>"));
        controlPanel.add(btnAStar);
        controlPanel.add(btnDijkstra);
        controlPanel.add(btnBellman);
        controlPanel.add(btnCompare);

        controlPanel.add(new JLabel("<html><font color='white'><b> | MAPS:</b></font></html>"));
        controlPanel.add(btnMapGrid);
        controlPanel.add(btnMapStar);
        controlPanel.add(btnMapRandom);

        return controlPanel;
    }

    // --- System Manager to Hot-Swap Maps ---
    private static void loadMap(String type) {
        NetworkGraph newGraph;
        if (type.equals("GRID")) {
            newGraph = new NetworkGraph();
            newGraph.generateGrid();
        } else if (type.equals("STAR")) {
            newGraph = generateStarMap();
        } else {
            newGraph = generateRandomMap();
        }

        Router start = newGraph.allRouters.get(0);
        Router end = newGraph.allRouters.get(newGraph.allRouters.size() - 1);

        SimulationController controller = new SimulationController(newGraph);
        controller.runSimulation(start, end);

        if (currentPanel != null) {
            mainContainer.remove(currentPanel); // Remove the old canvas
        }

        currentPanel = new PacketPanel(newGraph, controller, start, end);
        mainContainer.add(currentPanel, BorderLayout.CENTER);

        mainContainer.revalidate();
        mainContainer.repaint();
    }

    // --- Custom Map Generators (No need to touch NetworkGraph.java!) ---

    private static NetworkGraph generateStarMap() {
        NetworkGraph g = new NetworkGraph();
        Router center = new Router(0, 450, 300);
        g.allRouters.add(center);
        g.adjList.put(center, new ArrayList<>());

        Random rand = new Random();
        for (int i = 1; i <= 12; i++) {
            double angle = i * (Math.PI / 6); // 12 points in a circle
            int rx = 450 + (int) (200 * Math.cos(angle));
            int ry = 300 + (int) (200 * Math.sin(angle));
            Router edge = new Router(i, rx, ry);

            g.allRouters.add(edge);
            g.adjList.put(edge, new ArrayList<>());

            int weight = rand.nextInt(10) + 1;
            g.adjList.get(center).add(new Cable(edge, weight)); // Center to edge
            g.adjList.get(edge).add(new Cable(center, weight)); // Edge to center
        }
        return g;
    }

    private static NetworkGraph generateRandomMap() {
        NetworkGraph g = new NetworkGraph();
        Random rand = new Random();
        int nodeCount = 20;

        // Place random routers
        for (int i = 0; i < nodeCount; i++) {
            int rx = 100 + rand.nextInt(700);
            int ry = 100 + rand.nextInt(450);
            Router r = new Router(i, rx, ry);
            g.allRouters.add(r);
            g.adjList.put(r, new ArrayList<>());
        }

        // Connect them if they are close to each other
        for (int i = 0; i < nodeCount; i++) {
            for (int j = i + 1; j < nodeCount; j++) {
                Router u = g.allRouters.get(i);
                Router v = g.allRouters.get(j);

                double dist = Point2D.distance(u.x, u.y, v.x, v.y);
                if (dist < 200) { // Only connect if they are close
                    int weight = (int) (dist / 10);
                    g.adjList.get(u).add(new Cable(v, weight));
                    g.adjList.get(v).add(new Cable(u, weight));
                }
            }
        }
        return g;
    }

    // Helper for beautiful buttons
    private static JButton createButton(String text, Color textColor) {
        JButton btn = new JButton(text);
        btn.setForeground(textColor);
        btn.setBackground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        return btn;
    }
}