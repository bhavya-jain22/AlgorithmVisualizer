import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class NetworkPanel extends JPanel {
    private NetworkGraph graph;
    private List<Router> activePath;
    private Runnable onGraphChanged;
    
    public List<Packet> activePackets = new java.util.ArrayList<>();

    public void setGraph(NetworkGraph g) { this.graph = g; repaint(); }

    // Classic Modern Color Palette
    private final Color BG_DARK = new Color(245, 245, 250);
    private final Color CABLE_NORMAL = new Color(200, 200, 210);
    private final Color CABLE_BROKEN = new Color(220, 53, 69); // Bootstrap Red
    private final Color NODE_COLOR = new Color(100, 100, 120);
    private final Color PATH_COLOR = new Color(46, 204, 113); // Green

    public NetworkPanel(NetworkGraph graph, Runnable onGraphChanged) {
        this.graph = graph;
        this.onGraphChanged = onGraphChanged;
        setBackground(BG_DARK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    public void setActivePath(List<Router> path) {
        this.activePath = path;
        repaint();
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        if (graph == null) return;
        
        Cable closestCable = null;
        Router cableStart = null;
        double minDistance = Double.MAX_VALUE;

        for (Router r : graph.allRouters) {
            for (Cable c : graph.adjList.get(r)) {
                int x1 = r.x;
                int y1 = r.y;
                int x2 = c.targetRouter.x;
                int y2 = c.targetRouter.y;

                double dist = pointToSegmentDistance(mouseX, mouseY, x1, y1, x2, y2);
                if (dist < minDistance && dist < 15) { // Click within 15 pixels
                    minDistance = dist;
                    closestCable = c;
                    cableStart = r;
                }
            }
        }

        if (closestCable != null) {
            boolean isBroken = closestCable.isBroken;
            closestCable.isBroken = !isBroken;
            
            // Toggle reverse cable
            for (Cable reverse : graph.adjList.get(closestCable.targetRouter)) {
                if (reverse.targetRouter == cableStart) {
                    reverse.isBroken = !isBroken;
                    break;
                }
            }
            
            onGraphChanged.run(); // trigger recalculation
            repaint();
        }
    }

    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (l2 == 0) return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        return Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Cables
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (graph != null) {
            for (Router r : graph.allRouters) {
                for (Cable c : graph.adjList.get(r)) {
                    if (r.id > c.targetRouter.id) continue; // Avoid drawing twice
                    
                    if (c.isBroken) {
                        g2d.setColor(CABLE_BROKEN);
                        drawDashedLine(g2d, r.x, r.y, c.targetRouter.x, c.targetRouter.y);
                    } else {
                        g2d.setColor(CABLE_NORMAL);
                        g2d.drawLine(r.x, r.y, c.targetRouter.x, c.targetRouter.y);
                    }
                    
                    // Draw Weight Label
                    if (!c.isBroken) {
                        int midX = (r.x + c.targetRouter.x) / 2;
                        int midY = (r.y + c.targetRouter.y) / 2;
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                        g2d.drawString(String.valueOf(c.weight), midX, midY - 5);
                    }
                }
            }

            // Draw Active Path (if any)
            if (activePath != null && activePath.size() > 1) {
                g2d.setColor(PATH_COLOR);
                g2d.setStroke(new BasicStroke(5.0f));
                for (int i = 0; i < activePath.size() - 1; i++) {
                    Router a = activePath.get(i);
                    Router b = activePath.get(i + 1);
                    g2d.drawLine(a.x, a.y, b.x, b.y);
                }
            }

            // Draw Routers
            for (Router r : graph.allRouters) {
                g2d.setColor(NODE_COLOR);
                if (activePath != null && activePath.contains(r)) {
                    g2d.setColor(PATH_COLOR);
                }
                g2d.fillOval(r.x - 15, r.y - 15, 30, 30);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String idStr = String.valueOf(r.id);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = r.x - fm.stringWidth(idStr) / 2;
                int textY = r.y + fm.getAscent() / 2 - 2;
                g2d.drawString(idStr, textX, textY);
            }

            // Draw Packets
            for (Packet p : activePackets) {
                int drawX = (int)p.drawX + p.offset;
                int drawY = (int)p.drawY + p.offset;
                
                g2d.setColor(p.color);
                g2d.fillOval(drawX - 8, drawY - 8, 16, 16);
                g2d.setColor(Color.WHITE);
                g2d.drawOval(drawX - 8, drawY - 8, 16, 16);
                
                // Draw Packet Name
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.setColor(p.color.darker());
                g2d.drawString(p.name, drawX - fm.stringWidth(p.name)/2, drawY - 12);
            }
        }
    }
    
    public static class Packet {
        public String name;
        public List<Router> path;
        public int pathIndex = 0;
        public double progress = 0.0; 
        public double drawX, drawY;
        public Color color;
        public int offset;

        public Packet(String name, List<Router> path, Color color, int offset) {
            this.name = name;
            this.path = path;
            this.color = color;
            this.offset = offset;
            if (path != null && !path.isEmpty()) {
                drawX = path.get(0).x;
                drawY = path.get(0).y;
            }
        }

        public void moveStep(NetworkGraph graph) {
            if (path == null || pathIndex >= path.size() - 1) return;

            Router current = path.get(pathIndex);
            Router next = path.get(pathIndex + 1);

            int weight = 5;
            for (Cable c : graph.adjList.get(current)) {
                if (c.targetRouter == next) {
                    weight = c.weight;
                    break;
                }
            }

            // inversely proportional to weight (higher latency = slower visually)
            progress += 0.2 / weight; 
            
            if (progress >= 1.0) {
                progress = 0;
                pathIndex++;
                if (pathIndex < path.size()) {
                    drawX = path.get(pathIndex).x;
                    drawY = path.get(pathIndex).y;
                }
            } else {
                drawX = current.x + (next.x - current.x) * progress;
                drawY = current.y + (next.y - current.y) * progress;
            }
        }
    }
    
    private void drawDashedLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        Stroke oldStroke = g2d.getStroke();
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(oldStroke);
    }
}
