import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PacketPanel extends JPanel implements MouseListener {

    private NetworkGraph graph;
    private SimulationController controller;

    private static final int CLICK_THRESHOLD = 5;

    public PacketPanel(NetworkGraph graph, SimulationController controller) {
        this.graph = graph;
        this.controller = controller;

        setBackground(Color.WHITE);
        addMouseListener(this);
    }

    // =========================
    // Mouse Handling
    // =========================
    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        Cable closestCable = null;
        double minDist = Double.MAX_VALUE;

        // Find closest cable
        for (Cable c : graph.getCables()) {
            Router r1 = c.getSource();
            Router r2 = c.getDestination();

            double dist = pointToSegmentDistance(
                    mx, my,
                    r1.getX(), r1.getY(),
                    r2.getX(), r2.getY()
            );

            if (dist < minDist) {
                minDist = dist;
                closestCable = c;
            }
        }

        // If within threshold → toggle
        if (closestCable != null && minDist <= CLICK_THRESHOLD) {
            closestCable.setBroken(!closestCable.isBroken());

            controller.runSimulation(); // trigger recomputation
            repaint();
        }
    }

    // =========================
    // Geometry Core Logic
    // =========================
    private double pointToSegmentDistance(
            int px, int py,
            int x1, int y1,
            int x2, int y2
    ) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            return Math.hypot(px - x1, py - y1);
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);

        t = Math.max(0, Math.min(1, t));

        double projX = x1 + t * dx;
        double projY = y1 + t * dy;

        return Math.hypot(px - projX, py - projY);
    }

    // =========================
    // Rendering
    // =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // ---- Draw Cables ----
        for (Cable c : graph.getCables()) {
            Router r1 = c.getSource();
            Router r2 = c.getDestination();

            if (c.isBroken()) {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setStroke(new BasicStroke(1));
            } else {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(2));
            }

            g2d.drawLine(r1.getX(), r1.getY(),
                         r2.getX(), r2.getY());
        }

        // ---- Draw Routers ----
        int radius = 10;
        for (Router r : graph.getRouters()) {
            int x = r.getX();
            int y = r.getY();

            g2d.setColor(Color.BLUE);
            g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        }

        // ---- Draw Algorithm Paths ----
        drawPath(g2d, controller.getDijkstraResult(), Color.YELLOW);
        drawPath(g2d, controller.getAStarResult(), Color.GREEN);
        drawPath(g2d, controller.getBellmanFordResult(), Color.BLUE);

        // ---- Draw Scoreboard ----
        drawScoreboard(g2d);
    }

    private void drawPath(Graphics2D g2d, PathResult result, Color color) {
        if (result == null || result.getPath() == null) return;

        List<Router> path = result.getPath();
        if (path.size() < 2) return;

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3));

        for (int i = 0; i < path.size() - 1; i++) {
            Router r1 = path.get(i);
            Router r2 = path.get(i + 1);

            g2d.drawLine(r1.getX(), r1.getY(),
                         r2.getX(), r2.getY());
        }
    }

    private void drawScoreboard(Graphics2D g2d) {
        int x = 10;
        int y = 20;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        PathResult d = controller.getDijkstraResult();
        PathResult a = controller.getAStarResult();
        PathResult b = controller.getBellmanFordResult();

        g2d.drawString("Dijkstra Nodes: " +
                (d != null ? d.getNodesVisited() : "-"), x, y);

        g2d.drawString("A* Nodes: " +
                (a != null ? a.getNodesVisited() : "-"), x, y + 20);

        g2d.drawString("Bellman-Ford Nodes: " +
                (b != null ? b.getNodesVisited() : "-"), x, y + 40);
    }

    // =========================
    // Unused MouseListener Methods
    // =========================
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}