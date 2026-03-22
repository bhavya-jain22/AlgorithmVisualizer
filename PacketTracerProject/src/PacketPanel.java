import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

public class PacketPanel extends JPanel {
    private NetworkGraph graph;
    private SimulationController controller;
    private Router startNode;
    private Router endNode;

    public PacketPanel(NetworkGraph graph, SimulationController controller, Router start, Router end) {
        this.graph = graph;
        this.controller = controller;
        this.startNode = start;
        this.endNode = end;

        setBackground(Color.DARK_GRAY);

        // This listens for our mouse clicks to break cables
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                // Check every cable to see if we clicked on it
                for (Router u : graph.allRouters) {
                    for (Cable cable : graph.adjList.get(u)) {
                        Router v = cable.targetRouter;

                        // If the mouse is within 5 pixels of the line, toggle it!
                        if (isPointNearLine(mouseX, mouseY, u.x, u.y, v.x, v.y, 5.0)) {
                            cable.isBroken = !cable.isBroken; // Toggle chaos

                            // Also toggle the reverse direction since it's an undirected graph
                            for (Cable reverseCable : graph.adjList.get(v)) {
                                if (reverseCable.targetRouter.id == u.id) {
                                    reverseCable.isBroken = cable.isBroken;
                                }
                            }

                            // Re-run the math with the new broken/healed cable
                            controller.runSimulation(startNode, endNode);
                            repaint(); // Redraw the screen
                            return; // Stop checking once we find the clicked cable
                        }
                    }
                }
            }
        });
    }

    // --- The Geometry Math to detect clicks on a line segment ---
    private boolean isPointNearLine(int px, int py, int x1, int y1, int x2, int y2, double tolerance) {
        double lineMag = Point2D.distanceSq(x1, y1, x2, y2);
        if (lineMag == 0.0)
            return Point2D.distance(px, py, x1, y1) < tolerance;

        // Calculate the closest point on the line segment to our mouse click
        double u = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lineMag;
        u = Math.max(0.0, Math.min(1.0, u)); // Clamp to the segment bounds

        double intersectX = x1 + u * (x2 - x1);
        double intersectY = y1 + u * (y2 - y1);

        return Point2D.distance(px, py, intersectX, intersectY) < tolerance;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Make lines look smooth instead of pixelated
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw all cables first (so they are under the routers)
        g2d.setStroke(new BasicStroke(2));
        for (Router u : graph.allRouters) {
            for (Cable cable : graph.adjList.get(u)) {
                if (cable.isBroken) {
                    g2d.setColor(Color.RED); // Broken cables are red
                } else {
                    g2d.setColor(Color.GRAY); // Working cables are gray
                }
                g2d.drawLine(u.x, u.y, cable.targetRouter.x, cable.targetRouter.y);
            }
        }

        // 2. Draw the winning paths (Offset slightly so they don't hide each other)
        drawPath(g2d, controller.getBellmanResult(), Color.CYAN, -4); // Bellman is Blue/Cyan
        drawPath(g2d, controller.getDijkstraResult(), Color.YELLOW, 0); // Dijkstra is Yellow
        drawPath(g2d, controller.getAStarResult(), Color.GREEN, 4); // A* is Green

        // 3. Draw the routers (Circles)
        int rSize = 20;
        for (Router u : graph.allRouters) {
            if (u.id == startNode.id)
                g2d.setColor(Color.GREEN); // Start is Green
            else if (u.id == endNode.id)
                g2d.setColor(Color.MAGENTA); // End is Magenta
            else
                g2d.setColor(Color.WHITE); // Normal routers are White

            g2d.fillOval(u.x - rSize / 2, u.y - rSize / 2, rSize, rSize);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(u.id), u.x - 5, u.y + 5);
        }

        // 4. Draw the Scoreboard!
        drawScoreboard(g2d);
    }

    private void drawPath(Graphics2D g2d, PathResult result, Color color, int offset) {
        if (result == null || result.path == null || result.path.isEmpty())
            return;

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(4));
        List<Router> path = result.path;

        for (int i = 0; i < path.size() - 1; i++) {
            Router a = path.get(i);
            Router b = path.get(i + 1);
            g2d.drawLine(a.x + offset, a.y + offset, b.x + offset, b.y + offset);
        }
    }

    private void drawScoreboard(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200)); // Semi-transparent black background
        g2d.fillRect(10, 10, 250, 110);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("--- ALGORITHM PERFORMANCE ---", 15, 30);

        PathResult aStar = controller.getAStarResult();
        PathResult dijk = controller.getDijkstraResult();
        PathResult bell = controller.getBellmanResult();

        g2d.setColor(Color.GREEN);
        g2d.drawString("A* Nodes Visited: " + (aStar != null ? aStar.nodesVisited : "0"), 15, 50);

        g2d.setColor(Color.YELLOW);
        g2d.drawString("Dijkstra Nodes Visited: " + (dijk != null ? dijk.nodesVisited : "0"), 15, 70);

        g2d.setColor(Color.CYAN);
        g2d.drawString("Bellman-Ford Visited: " + (bell != null ? bell.nodesVisited : "0"), 15, 90);

        g2d.setColor(Color.WHITE);
        g2d.drawString("Path Latency Cost: " + (aStar != null ? aStar.totalCost : "0"), 15, 110);
    }
}