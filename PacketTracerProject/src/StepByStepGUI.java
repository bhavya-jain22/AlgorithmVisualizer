import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

// Feature 4: Step-by-Step algorithm visualizer
public class StepByStepGUI extends JFrame {

    private NetworkGraph graph;
    private Router startRouter, endRouter;
    private String algoName;

    // BFS State
    private Queue<Router> bfsQueue;
    private Map<Router, Router> bfsCameFrom;
    private Set<Router> bfsVisited;

    // Dijkstra State
    private PriorityQueue<DNode> dijkQueue;
    private Map<Router, Integer> dijkDist;
    private Map<Router, Router> dijkCameFrom;
    private Set<Router> dijkVisited;

    // Common
    private Router currentNode;
    private List<Router> finalPath;
    private boolean finished = false;
    private int stepCount = 0;
    private Timer autoTimer;

    // Visual State
    private Set<Router> inFrontier = new HashSet<>();
    private Set<Router> inVisited = new HashSet<>();

    private DefaultListModel<String> queueModel;
    private JLabel stepCountLabel;
    private JTextArea logArea;

    private static class DNode {
        Router router; int dist;
        DNode(Router r, int d) { router = r; dist = d; }
    }

    private final int SCALE_X = 55;
    private final int SCALE_Y = 55;
    private final int OFFSET_X = 40;
    private final int OFFSET_Y = 40;

    public StepByStepGUI(NetworkGraph graph, Router start, Router end, String algoName) {
        super("Step-by-Step: " + algoName + " | " + start.id + " → " + end.id);
        this.graph = graph;
        this.startRouter = start;
        this.endRouter = end;
        this.algoName = algoName;

        setSize(1100, 720);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- Map panel ---
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph((Graphics2D) g);
            }
        };
        mapPanel.setBackground(new Color(248, 248, 252));
        add(mapPanel, BorderLayout.CENTER);

        // --- Controls top bar ---
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controlBar.setBackground(new Color(245, 245, 250));

        JLabel title = new JLabel("Algorithm: " + algoName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        controlBar.add(title);

        stepCountLabel = new JLabel("Step: 0");
        stepCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        controlBar.add(stepCountLabel);

        JButton stepBtn = makeBtn("▶ Next Step", new Color(52, 152, 219));
        stepBtn.addActionListener(e -> { step(mapPanel); });
        controlBar.add(stepBtn);

        JButton autoBtn = makeBtn("⚡ Auto Play", new Color(46, 204, 113));
        autoTimer = new Timer(400, e -> {
            if (finished) { autoTimer.stop(); return; }
            step(mapPanel);
        });
        autoBtn.addActionListener(e -> autoTimer.start());
        controlBar.add(autoBtn);

        JButton stopBtn = makeBtn("⏹ Stop", new Color(231, 76, 60));
        stopBtn.addActionListener(e -> autoTimer.stop());
        controlBar.add(stopBtn);

        // Color legend
        controlBar.add(new JLabel("  "));
        controlBar.add(colorDot(new Color(52, 152, 219), "In Queue/Frontier"));
        controlBar.add(colorDot(new Color(155, 89, 182), "Visited"));
        controlBar.add(colorDot(Color.ORANGE, "Current"));
        controlBar.add(colorDot(new Color(46, 204, 113), "Final Path"));
        controlBar.add(colorDot(Color.RED, "Start"));
        controlBar.add(colorDot(new Color(231, 76, 60), "Target"));

        add(controlBar, BorderLayout.NORTH);

        // --- Right sidebar ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 225)));

        queueModel = new DefaultListModel<>();
        JList<String> queueList = new JList<>(queueModel);
        queueList.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane qScroll = new JScrollPane(queueList);
        qScroll.setBorder(BorderFactory.createTitledBorder(algoName.contains("BFS") ? "Queue (FIFO)" : "Priority Queue"));
        qScroll.setPreferredSize(new Dimension(280, 300));
        sidebar.add(qScroll, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("What's Happening"));
        sidebar.add(logScroll, BorderLayout.CENTER);

        add(sidebar, BorderLayout.EAST);

        // Initialize algorithm
        initAlgorithm();
        setLocationRelativeTo(null);
        mapPanel.repaint();
    }

    private JLabel colorDot(Color c, String label) {
        JLabel lbl = new JLabel("● " + label);
        lbl.setForeground(c);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return lbl;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }

    private void initAlgorithm() {
        finalPath = new ArrayList<>();
        inFrontier.clear();
        inVisited.clear();

        if (algoName.startsWith("BFS")) {
            bfsQueue = new LinkedList<>();
            bfsCameFrom = new HashMap<>();
            bfsVisited = new HashSet<>();
            bfsQueue.add(startRouter);
            bfsVisited.add(startRouter);
            bfsCameFrom.put(startRouter, null);
            inFrontier.add(startRouter);
            log("BFS initialized. Start node " + startRouter.id + " added to queue.\nBFS explores the graph layer by layer - each 'hop' is treated equally.");
        } else {
            dijkQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
            dijkDist = new HashMap<>();
            dijkCameFrom = new HashMap<>();
            dijkVisited = new HashSet<>();
            for (Router r : graph.allRouters) dijkDist.put(r, Integer.MAX_VALUE);
            dijkDist.put(startRouter, 0);
            dijkQueue.add(new DNode(startRouter, 0));
            inFrontier.add(startRouter);
            log("Dijkstra initialized. Start node " + startRouter.id + " added to priority queue with cost 0.\nDijkstra always expands the cheapest known node next.");
        }
        queueModel.clear();
        queueModel.addElement(String.valueOf(startRouter.id));
    }

    private void step(JPanel mapPanel) {
        if (finished) return;
        stepCount++;
        stepCountLabel.setText("Step: " + stepCount);

        if (algoName.startsWith("BFS")) stepBFS();
        else stepDijkstra();

        mapPanel.repaint();
    }

    private void stepBFS() {
        if (bfsQueue.isEmpty()) { finished = true; log("Queue empty. No path found."); return; }

        currentNode = bfsQueue.poll();
        inFrontier.remove(currentNode);
        inVisited.add(currentNode);
        queueModel.clear();
        for (Router r : bfsQueue) queueModel.addElement(String.valueOf(r.id));

        log("Step " + stepCount + ": Dequeued Router " + currentNode.id + ". Checking its neighbors...");

        if (currentNode.equals(endRouter)) {
            finished = true;
            tracePath(bfsCameFrom);
            log("✅ Target Router " + endRouter.id + " reached after " + stepCount + " steps! Tracing shortest-hop path back...");
            return;
        }

        for (Cable c : graph.adjList.get(currentNode)) {
            if (!c.isBroken && !bfsVisited.contains(c.targetRouter)) {
                bfsVisited.add(c.targetRouter);
                bfsCameFrom.put(c.targetRouter, currentNode);
                bfsQueue.add(c.targetRouter);
                inFrontier.add(c.targetRouter);
                queueModel.addElement(String.valueOf(c.targetRouter.id));
                log("  → Added Router " + c.targetRouter.id + " to queue");
            }
        }
    }

    private void stepDijkstra() {
        if (dijkQueue.isEmpty()) { finished = true; log("Priority queue empty."); return; }

        DNode dNode = dijkQueue.poll();
        currentNode = dNode.router;
        if (dijkVisited.contains(currentNode)) { stepDijkstra(); return; }
        dijkVisited.add(currentNode);
        inFrontier.remove(currentNode);
        inVisited.add(currentNode);
        queueModel.clear();
        for (DNode dn : dijkQueue) queueModel.addElement("R" + dn.router.id + " (d=" + dn.dist + ")");

        log("Step " + stepCount + ": Processing Router " + currentNode.id + " with cost=" + dNode.dist);

        if (currentNode.equals(endRouter)) {
            finished = true;
            tracePath(dijkCameFrom);
            log("✅ Target " + endRouter.id + " reached! Total cost: " + dNode.dist + ". Path traced.");
            return;
        }

        for (Cable c : graph.adjList.get(currentNode)) {
            if (!c.isBroken && !dijkVisited.contains(c.targetRouter)) {
                int newDist = dijkDist.get(currentNode) + c.weight;
                if (newDist < dijkDist.get(c.targetRouter)) {
                    dijkDist.put(c.targetRouter, newDist);
                    dijkCameFrom.put(c.targetRouter, currentNode);
                    dijkQueue.add(new DNode(c.targetRouter, newDist));
                    inFrontier.add(c.targetRouter);
                    log("  → Updated R" + c.targetRouter.id + " dist=" + newDist);
                }
            }
        }
    }

    private void tracePath(Map<Router, Router> cameFrom) {
        Router c = endRouter;
        while (c != null) { finalPath.add(0, c); c = cameFrom.get(c); }
    }

    private void log(String msg) {
        logArea.append(msg + "\n\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void drawGraph(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw cables
        g2d.setStroke(new BasicStroke(2.0f));
        for (Router r : graph.allRouters) {
            for (Cable c : graph.adjList.get(r)) {
                if (r.id > c.targetRouter.id) continue;
                g2d.setColor(c.isBroken ? new Color(220, 53, 69) : new Color(200, 200, 210));
                g2d.drawLine(r.x, r.y, c.targetRouter.x, c.targetRouter.y);
            }
        }

        // Highlight final path
        if (!finalPath.isEmpty()) {
            g2d.setColor(new Color(46, 204, 113));
            g2d.setStroke(new BasicStroke(5.0f));
            for (int i = 0; i < finalPath.size() - 1; i++) {
                Router a = finalPath.get(i), b = finalPath.get(i + 1);
                g2d.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        // Draw routers
        for (Router r : graph.allRouters) {
            Color nodeColor;
            int size = 22;

            if (r.equals(startRouter)) { nodeColor = Color.GREEN.darker(); size = 28; }
            else if (r.equals(endRouter)) { nodeColor = new Color(231, 76, 60); size = 28; }
            else if (r.equals(currentNode)) { nodeColor = Color.ORANGE; size = 26; }
            else if (!finalPath.isEmpty() && finalPath.contains(r)) { nodeColor = new Color(46, 204, 113); }
            else if (inFrontier.contains(r)) { nodeColor = new Color(52, 152, 219); }
            else if (inVisited.contains(r)) { nodeColor = new Color(155, 89, 182); }
            else { nodeColor = new Color(180, 180, 195); }

            g2d.setColor(nodeColor);
            g2d.fillOval(r.x - size/2, r.y - size/2, size, size);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String lbl = String.valueOf(r.id);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(lbl, r.x - fm.stringWidth(lbl)/2, r.y + fm.getAscent()/2 - 2);
        }
    }
}
