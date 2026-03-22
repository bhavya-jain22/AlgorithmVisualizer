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

    // The high-level state tracker!
    public enum ViewMode {
        ASTAR, DIJKSTRA, BELLMAN, COMPARE
    }

    private ViewMode currentMode = ViewMode.COMPARE;

    public PacketPanel(NetworkGraph graph, SimulationController controller, Router start, Router end) {
        this.graph = graph;
        this.controller = controller;
        this.startNode = start;
        this.endNode = end;

        setBackground(new Color(30, 30, 30)); // Sleek dark mode background

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                for (Router u : graph.allRouters) {
                    for (Cable cable : graph.adjList.get(u)) {
                        Router v = cable.targetRouter;
                        if (isPointNearLine(mouseX, mouseY, u.x, u.y, v.x, v.y, 5.0)) {
                            cable.isBroken = !cable.isBroken;

                            for (Cable reverseCable : graph.adjList.get(v)) {
                                if (reverseCable.targetRouter.id == u.id) {
                                    reverseCable.isBroken = cable.isBroken;
                                }
                            }
                            // Recalculate math behind the scenes, then repaint
                            controller.runSimulation(startNode, endNode);
                            repaint();
                            return;
                        }
                    }
                }
            }
        });
    }

    public void setViewMode(ViewMode mode) {
        this.currentMode = mode;
        repaint(); // Instantly update screen when a button is clicked
    }

    private boolean isPointNearLine(int px, int py, int x1, int y1, int x2, int y2, double tolerance) {
        double lineMag = Point2D.distanceSq(x1, y1, x2, y2);
        if (lineMag == 0.0)
            return Point2D.distance(px, py, x1, y1) < tolerance;
        double u = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lineMag;
        u = Math.max(0.0, Math.min(1.0, u));
        double intersectX = x1 + u * (x2 - x1);
        double intersectY = y1 + u * (y2 - y1);
        return Point2D.distance(px, py, intersectX, intersectY) < tolerance;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Network Infrastructure
        g2d.setStroke(new BasicStroke(2));
        for (Router u : graph.allRouters) {
            for (Cable cable : graph.adjList.get(u)) {
                if (cable.isBroken) {
                    g2d.setColor(new Color(255, 80, 80)); // Bright Red
                } else {
                    g2d.setColor(new Color(100, 100, 100)); // Dim Gray
                }
                g2d.drawLine(u.x, u.y, cable.targetRouter.x, cable.targetRouter.y);
            }
        }

        // 2. Draw Algorithm Paths based on the active button
        if (currentMode == ViewMode.BELLMAN || currentMode == ViewMode.COMPARE) {
            drawPath(g2d, controller.getBellmanResult(), Color.CYAN, -4);
        }
        if (currentMode == ViewMode.DIJKSTRA || currentMode == ViewMode.COMPARE) {
            drawPath(g2d, controller.getDijkstraResult(), Color.YELLOW, 0);
        }
        if (currentMode == ViewMode.ASTAR || currentMode == ViewMode.COMPARE) {
            drawPath(g2d, controller.getAStarResult(), Color.GREEN, 4);
        }

        // 3. Draw Routers
        int rSize = 24;
        for (Router u : graph.allRouters) {
            if (u.id == startNode.id)
                g2d.setColor(Color.GREEN);
            else if (u.id == endNode.id)
                g2d.setColor(Color.MAGENTA);
            else
                g2d.setColor(Color.WHITE);

            g2d.fillOval(u.x - rSize / 2, u.y - rSize / 2, rSize, rSize);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(String.valueOf(u.id), u.x - 7, u.y + 4);
        }

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
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(10, 10, 260, 130);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("📈 EFFICIENCY ANALYSIS", 15, 30);

        PathResult aStar = controller.getAStarResult();
        PathResult dijk = controller.getDijkstraResult();
        PathResult bell = controller.getBellmanResult();

        int y = 55;
        if (currentMode == ViewMode.ASTAR || currentMode == ViewMode.COMPARE) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("A* Visited: " + (aStar != null ? aStar.nodesVisited : "0") + " nodes", 15, y);
            y += 20;
        }
        if (currentMode == ViewMode.DIJKSTRA || currentMode == ViewMode.COMPARE) {
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Dijkstra Visited: " + (dijk != null ? dijk.nodesVisited : "0") + " nodes", 15, y);
            y += 20;
        }
        if (currentMode == ViewMode.BELLMAN || currentMode == ViewMode.COMPARE) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("Bellman-Ford Visited: " + (bell != null ? bell.nodesVisited : "0") + " nodes", 15, y);
            y += 20;
        }

        g2d.setColor(Color.WHITE);
        g2d.drawString("Optimal Path Latency: " + (aStar != null ? aStar.totalCost : "0"), 15, 120);
    }
}