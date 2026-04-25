import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PacketTracerGUI extends JFrame {
    private NetworkGraph graph;
    private SimulationController controller;
    private NetworkPanel networkPanel;
    
    private Router startRouter;
    private Router endRouter;

    // UI elements
    private JLabel dijkstraCostLbl;
    private JLabel aStarCostLbl;
    private JLabel bellmanCostLbl;
    
    private JLabel dijkstraNodesLbl;
    private JLabel aStarNodesLbl;
    private JLabel bellmanNodesLbl;
    
    private JComboBox<String> algoSelector;
    
    private Timer animationTimer;
    private JButton runBtn;
    private JButton raceBtn;
    private JButton benchmarkBtn;
    
    // New feature controls
    private JLabel bfsNodesLbl, bfsCostLbl;
    private boolean showMST = false;
    private java.util.List<KruskalMST.Edge> mstEdges = new java.util.ArrayList<>();
    private JLabel bigOLabel;

    // Classic Modern Colors
    private final Color BG_DARK = new Color(245, 245, 250);
    private final Color PANEL_DARK = new Color(255, 255, 255);
    private final Color TEXT_LIGHT = new Color(30, 30, 30);

    public PacketTracerGUI(NetworkGraph graph, SimulationController controller) {
        super("PacketTracer Visualizer - Routing Simulator");
        this.graph = graph;
        this.controller = controller;
        
        startRouter = graph.allRouters.get(0);
        endRouter = graph.allRouters.get(graph.allRouters.size() - 1);

        setSize(1000, 700);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BG_DARK);
        
        // First row: controls
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        row1.setBackground(BG_DARK);
        
        JLabel titleLabel = new JLabel("Routing: " + startRouter.id + " → " + endRouter.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_LIGHT);
        row1.add(titleLabel);
        
        algoSelector = new JComboBox<>(new String[]{"Show A*", "Show Dijkstra", "Show Bellman-Ford", "Show BFS"});
        algoSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        algoSelector.addActionListener(e -> { updateDisplayedPath(); updateBigO(); });
        row1.add(algoSelector);
        
        runBtn = new JButton("▶ Run Packet");
        runBtn.setBackground(new Color(52, 152, 219)); runBtn.setForeground(Color.WHITE);
        runBtn.setFocusPainted(false);
        runBtn.addActionListener(e -> runSinglePacket());
        row1.add(runBtn);
        
        raceBtn = new JButton("🏆 Race All");
        raceBtn.setBackground(new Color(155, 89, 182)); raceBtn.setForeground(Color.WHITE);
        raceBtn.setFocusPainted(false);
        raceBtn.addActionListener(e -> startRace());
        row1.add(raceBtn);
        
        benchmarkBtn = new JButton("🔥 Harsh Benchmark");
        benchmarkBtn.setBackground(new Color(231, 76, 60)); benchmarkBtn.setForeground(Color.WHITE);
        benchmarkBtn.setFocusPainted(false);
        benchmarkBtn.addActionListener(e -> runHarshBenchmark());
        row1.add(benchmarkBtn);
        
        headerPanel.add(row1);
        
        // Second row: Topology + new features
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        row2.setBackground(BG_DARK);
        
        JLabel topoLabel = new JLabel("Topology:");
        topoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row2.add(topoLabel);
        
        JButton gridBtn   = makeTopologyBtn("Grid",  () -> switchTopology("Grid"));
        JButton ringBtn   = makeTopologyBtn("Ring",  () -> switchTopology("Ring"));
        JButton starBtn   = makeTopologyBtn("Star",  () -> switchTopology("Star"));
        JButton meshBtn   = makeTopologyBtn("Mesh",  () -> switchTopology("Mesh"));
        row2.add(gridBtn); row2.add(ringBtn); row2.add(starBtn); row2.add(meshBtn);
        
        JButton mstBtn = new JButton("🌲 Show MST");
        mstBtn.setBackground(new Color(39, 174, 96));
        mstBtn.setForeground(Color.WHITE); mstBtn.setFocusPainted(false);
        mstBtn.addActionListener(e -> {
            showMST = !showMST;
            mstBtn.setText(showMST ? "❌ Hide MST" : "🌲 Show MST");
            if (showMST) { mstEdges = new KruskalMST().computeMST(graph); }
            networkPanel.setMSTEdges(showMST ? mstEdges : null);
        });
        row2.add(mstBtn);
        
        JButton stepBfsBtn = new JButton("👣 Step BFS");
        stepBfsBtn.setBackground(new Color(52, 73, 94));
        stepBfsBtn.setForeground(Color.WHITE); stepBfsBtn.setFocusPainted(false);
        stepBfsBtn.addActionListener(e -> openStepByStep("BFS"));
        row2.add(stepBfsBtn);
        
        JButton stepDijkBtn = new JButton("👣 Step Dijkstra");
        stepDijkBtn.setBackground(new Color(52, 73, 94));
        stepDijkBtn.setForeground(Color.WHITE); stepDijkBtn.setFocusPainted(false);
        stepDijkBtn.addActionListener(e -> openStepByStep("Dijkstra"));
        row2.add(stepDijkBtn);
        
        headerPanel.add(row2);
        add(headerPanel, BorderLayout.NORTH);

        // Setup Animation Timer
        animationTimer = new Timer(30, e -> {
            boolean anyMoving = false;
            for (NetworkPanel.Packet p : networkPanel.activePackets) {
                if (p.pathIndex < p.path.size() - 1) {
                    p.moveStep(graph);
                    anyMoving = true;
                }
            }
            if (!anyMoving && !networkPanel.activePackets.isEmpty()) {
                animationTimer.stop();
                if (networkPanel.activePackets.size() > 1) {
                    showRaceExplanation();
                }
            }
            networkPanel.repaint();
        });

        // Network Map Panel
        networkPanel = new NetworkPanel(graph, this::recalculateRoutes);
        add(networkPanel, BorderLayout.CENTER);

        // Sidebar for Stats
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(PANEL_DARK);
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(320, 0));

        JLabel statsTitle = new JLabel("Scoreboard");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statsTitle.setForeground(new Color(52, 152, 219)); // Blue
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(statsTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // A* Stats (Green)
        sidebar.add(createAlgoTitle("A* (Optimized)", new Color(46, 204, 113)));
        aStarCostLbl = createStatLabel("Total Latency: -");
        aStarNodesLbl = createStatLabel("Nodes Explored: -");
        sidebar.add(aStarCostLbl);
        sidebar.add(aStarNodesLbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // Dijkstra Stats (Blue)
        sidebar.add(createAlgoTitle("Dijkstra (Baseline)", new Color(52, 152, 219)));
        dijkstraCostLbl = createStatLabel("Total Latency: -");
        dijkstraNodesLbl = createStatLabel("Nodes Explored: -");
        sidebar.add(dijkstraCostLbl);
        sidebar.add(dijkstraNodesLbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // Bellman-Ford Stats (Purple)
        sidebar.add(createAlgoTitle("Bellman-Ford", new Color(155, 89, 182)));
        bellmanCostLbl = createStatLabel("Total Latency: -");
        bellmanNodesLbl = createStatLabel("Nodes Explored: -");
        sidebar.add(bellmanCostLbl);
        sidebar.add(bellmanNodesLbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // BFS Stats (Orange)
        sidebar.add(createAlgoTitle("BFS (Fewest Hops)", new Color(230, 126, 34)));
        bfsCostLbl  = createStatLabel("Total Latency: -");
        bfsNodesLbl = createStatLabel("Nodes Explored: -");
        sidebar.add(bfsCostLbl);
        sidebar.add(bfsNodesLbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Big-O Complexity Panel
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel bigOTitle = createAlgoTitle("Big-O Complexity", new Color(52, 73, 94));
        sidebar.add(bigOTitle);
        bigOLabel = createStatLabel("Select an algorithm above");
        bigOLabel.setFont(new Font("Consolas", Font.PLAIN, 13));
        sidebar.add(bigOLabel);
        
        sidebar.add(Box.createVerticalGlue());
        
        JTextArea hintText = new JTextArea("Hint: Click any cable to BREAK it. Use topology buttons to switch graph type. Show MST draws the cheapest backbone network.");
        hintText.setWrapStyleWord(true);
        hintText.setLineWrap(true);
        hintText.setEditable(false);
        hintText.setBackground(PANEL_DARK);
        hintText.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintText.setForeground(Color.GRAY);
        sidebar.add(hintText);

        add(sidebar, BorderLayout.EAST);

        // Initial Run
        recalculateRoutes();
        setLocationRelativeTo(null);
    }

    private JLabel createAlgoTitle(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel createStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_LIGHT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void recalculateRoutes() {
        controller.runSimulation(startRouter, endRouter);
        updateScoreboard();
        updateDisplayedPath();
        updateBigO();
    }

    private void updateScoreboard() {
        PathResult aStar = controller.getAStarResult();
        PathResult dijkstra = controller.getDijkstraResult();
        PathResult bellman = controller.getBellmanResult();

        if (aStar.path == null) {
            aStarCostLbl.setText("Total Latency: NO PATH");
            aStarNodesLbl.setText("Nodes Explored: " + aStar.nodesVisited);
        } else {
            aStarCostLbl.setText("Total Latency: " + aStar.totalCost + " ms");
            aStarNodesLbl.setText("Nodes Explored: " + aStar.nodesVisited);
        }

        if (dijkstra.path == null) {
            dijkstraCostLbl.setText("Total Latency: NO PATH");
            dijkstraNodesLbl.setText("Nodes Explored: " + dijkstra.nodesVisited);
        } else {
            dijkstraCostLbl.setText("Total Latency: " + dijkstra.totalCost + " ms");
            dijkstraNodesLbl.setText("Nodes Explored: " + dijkstra.nodesVisited);
        }

        if (bellman.path == null) {
            bellmanCostLbl.setText("Total Latency: NO PATH");
            bellmanNodesLbl.setText("Nodes Explored: " + bellman.nodesVisited);
        } else {
            bellmanCostLbl.setText("Total Latency: " + bellman.totalCost + " ms");
            bellmanNodesLbl.setText("Nodes Explored: " + bellman.nodesVisited);
        }
        
        PathResult bfs = controller.getBFSResult();
        if (bfs == null || bfs.path == null) {
            bfsCostLbl.setText("Total Latency: NO PATH");
            bfsNodesLbl.setText("Nodes Explored: -");
        } else {
            bfsCostLbl.setText("Total Latency: " + bfs.totalCost + " ms");
            bfsNodesLbl.setText("Nodes Explored: " + bfs.nodesVisited);
        }
    }
    
    private void updateBigO() {
        String sel = (String) algoSelector.getSelectedItem();
        if (sel == null) return;
        switch (sel) {
            case "Show A*":
                bigOLabel.setText("<html>Time: O(E log V)<br>Space: O(V)<br>Uses: Heuristic + Greedy</html>");
                break;
            case "Show Dijkstra":
                bigOLabel.setText("<html>Time: O(E log V)<br>Space: O(V)<br>Uses: Priority Queue</html>");
                break;
            case "Show Bellman-Ford":
                bigOLabel.setText("<html>Time: O(V × E)<br>Space: O(V)<br>Uses: Dynamic Programming</html>");
                break;
            case "Show BFS":
                bigOLabel.setText("<html>Time: O(V + E)<br>Space: O(V)<br>Uses: FIFO Queue</html>");
                break;
        }
    }
    
    private JButton makeTopologyBtn(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.addActionListener(e -> action.run());
        return btn;
    }
    
    private void switchTopology(String topo) {
        showMST = false;
        networkPanel.setMSTEdges(null);
        switch (topo) {
            case "Grid": graph.generateGrid(); break;
            case "Ring": graph.generateRing(); break;
            case "Star": graph.generateStar(); break;
            case "Mesh": graph.generateMesh(); break;
        }
        startRouter = graph.allRouters.get(0);
        endRouter = graph.allRouters.get(graph.allRouters.size() - 1);
        networkPanel.setGraph(graph);
        recalculateRoutes();
    }
    
    private void openStepByStep(String algoName) {
        randomizeStartEnd();
        StepByStepGUI viz = new StepByStepGUI(graph, startRouter, endRouter, algoName);
        viz.setVisible(true);
    }

    private void updateDisplayedPath() {
        String selected = (String) algoSelector.getSelectedItem();
        PathResult result = null;
        
        if ("Show A*".equals(selected)) {
            result = controller.getAStarResult();
        } else if ("Show Dijkstra".equals(selected)) {
            result = controller.getDijkstraResult();
        } else if ("Show Bellman-Ford".equals(selected)) {
            result = controller.getBellmanResult();
        }
        
        if (result != null) {
            networkPanel.setActivePath(result.path);
        }
    }
    
    private void randomizeStartEnd() {
        java.util.Random rand = new java.util.Random();
        startRouter = graph.allRouters.get(rand.nextInt(graph.allRouters.size()));
        
        // Pick an end router that is a medium-to-long distance away
        double dist = 0;
        while (dist < 400) {
            endRouter = graph.allRouters.get(rand.nextInt(graph.allRouters.size()));
            dist = Math.sqrt(Math.pow(endRouter.x - startRouter.x, 2) + Math.pow(endRouter.y - startRouter.y, 2));
        }
        recalculateRoutes();
    }

    private void runSinglePacket() {
        randomizeStartEnd();
        networkPanel.activePackets.clear();
        String selected = (String) algoSelector.getSelectedItem();
        PathResult result = null;
        
        if ("Show A*".equals(selected)) result = controller.getAStarResult();
        else if ("Show Dijkstra".equals(selected)) result = controller.getDijkstraResult();
        else if ("Show Bellman-Ford".equals(selected)) result = controller.getBellmanResult();
        
        if (result != null && result.path != null && !result.path.isEmpty()) {
            networkPanel.activePackets.add(new NetworkPanel.Packet(selected.replace("Show ", ""), result.path, new Color(52, 152, 219), 0));
            animationTimer.start();
        }
    }

    private void startRace() {
        randomizeStartEnd();
        networkPanel.activePackets.clear();
        
        PathResult aStar = controller.getAStarResult();
        PathResult dijkstra = controller.getDijkstraResult();
        PathResult bellman = controller.getBellmanResult();
        PathResult bfs = controller.getBFSResult();
        
        // A*=Green, Dijkstra=Blue, Bellman=Purple, BFS=Orange
        if (aStar != null && aStar.path != null) 
            networkPanel.activePackets.add(new NetworkPanel.Packet("A*", aStar.path, new Color(46, 204, 113), -20));
        if (dijkstra != null && dijkstra.path != null) 
            networkPanel.activePackets.add(new NetworkPanel.Packet("Dijkstra", dijkstra.path, new Color(52, 152, 219), -7));
        if (bellman != null && bellman.path != null) 
            networkPanel.activePackets.add(new NetworkPanel.Packet("Bellman", bellman.path, new Color(155, 89, 182), 7));
        if (bfs != null && bfs.path != null) 
            networkPanel.activePackets.add(new NetworkPanel.Packet("BFS", bfs.path, new Color(230, 126, 34), 20));
            
        networkPanel.setActivePath(null); // Clear active path so we can see the race clearly
        animationTimer.start();
    }
    
    private void showRaceExplanation() {
        PathResult aStar = controller.getAStarResult();
        PathResult dijkstra = controller.getDijkstraResult();
        PathResult bellman = controller.getBellmanResult();
        
        String aStarNodes = (aStar != null && aStar.path != null) ? String.valueOf(aStar.nodesVisited) : "Failed";
        String dijkstraNodes = (dijkstra != null && dijkstra.path != null) ? String.valueOf(dijkstra.nodesVisited) : "Failed";
        String bellmanNodes = (bellman != null && bellman.path != null) ? String.valueOf(bellman.nodesVisited) : "Failed";
        
        String message = "🏁 Race Finished!\n\n" +
            "Did you notice that all three packets arrived at the exact same time?\n\n" +
            "Here is why:\n" +
            "1. A*, Dijkstra, and Bellman-Ford all guarantee finding the shortest path.\n" +
            "2. Since they took the exact same shortest path, they traveled at the exact same speed!\n\n" +
            "So what makes them different?\n" +
            "Look at the 'Nodes Explored' on the Scoreboard for this specific race:\n" +
            "🟢 A* explored: " + aStarNodes + " nodes\n" +
            "🔵 Dijkstra explored: " + dijkstraNodes + " nodes\n" +
            "🟣 Bellman-Ford explored: " + bellmanNodes + " nodes\n\n" +
            "A* explored the fewest nodes (using a smart heuristic compass), while Bellman-Ford stubbornly checked almost the entire map.\n\n" +
            "A* is the true winner in computational efficiency!";
            
        JOptionPane.showMessageDialog(this, message, "Race Results & Simple Explanation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void runHarshBenchmark() {
        benchmarkBtn.setEnabled(false);
        runBtn.setEnabled(false);
        raceBtn.setEnabled(false);
        
        int totalIterations = 50;
        int[] currentIter = {0};
        
        int[] totalAStarNodes = {0};
        int[] totalDijkstraNodes = {0};
        int[] totalBellmanNodes = {0};
        
        java.util.Random rand = new java.util.Random();
        
        // Timer runs every 100ms so the user can literally see the chaos happening!
        Timer benchTimer = new Timer(100, null);
        benchTimer.addActionListener(e -> {
            // First fix all cables
            for (Router r : graph.allRouters) {
                for (Cable c : graph.adjList.get(r)) {
                    c.isBroken = false;
                }
            }
            
            // Randomly break some cables (increased to 15% for visual impact)
            for (Router r : graph.allRouters) {
                for (Cable c : graph.adjList.get(r)) {
                    c.isBroken = (rand.nextInt(100) < 15); 
                }
            }
            
            // Pick completely random start and end points
            startRouter = graph.allRouters.get(rand.nextInt(graph.allRouters.size()));
            endRouter = graph.allRouters.get(rand.nextInt(graph.allRouters.size()));
            
            recalculateRoutes(); // Updates the UI and scoreboard live
            networkPanel.repaint();
            
            PathResult aStar = controller.getAStarResult();
            if (aStar != null && aStar.path != null) totalAStarNodes[0] += aStar.nodesVisited;
            
            PathResult dijkstra = controller.getDijkstraResult();
            if (dijkstra != null && dijkstra.path != null) totalDijkstraNodes[0] += dijkstra.nodesVisited;
            
            PathResult bellman = controller.getBellmanResult();
            if (bellman != null && bellman.path != null) totalBellmanNodes[0] += bellman.nodesVisited;
            
            currentIter[0]++;
            
            // Once 50 tests are done, stop timer and show chart
            if (currentIter[0] >= totalIterations) {
                ((Timer)e.getSource()).stop();
                
                // Restore map
                for (Router r : graph.allRouters) {
                    for (Cable c : graph.adjList.get(r)) {
                        c.isBroken = false;
                    }
                }
                recalculateRoutes(); 
                
                benchmarkBtn.setEnabled(true);
                runBtn.setEnabled(true);
                raceBtn.setEnabled(true);
                
                java.util.Map<String, Double> averages = new java.util.HashMap<>();
                averages.put("A*", (double) totalAStarNodes[0] / totalIterations);
                averages.put("Dijkstra", (double) totalDijkstraNodes[0] / totalIterations);
                averages.put("Bellman-Ford", (double) totalBellmanNodes[0] / totalIterations);
                
                BenchmarkChartGUI chart = new BenchmarkChartGUI(averages, totalIterations);
                chart.setVisible(true);
            }
        });
        benchTimer.start();
    }
}
