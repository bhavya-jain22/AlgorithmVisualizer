import javax.swing.*;
import java.awt.*;

public class PacketPanel extends JPanel {

    private NetworkGraph graph;

    public PacketPanel(NetworkGraph graph) {
        this.graph = graph;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smooth visuals
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Cables (Edges)
        g2d.setColor(Color.GRAY);
        for (Cable c : graph.getCables()) {

            if (c.isBroken()) continue;

            Router r1 = c.getSource();
            Router r2 = c.getDestination();

            g2d.drawLine(r1.getX(), r1.getY(),
                         r2.getX(), r2.getY());
        }

        // Draw Routers (Nodes)
        int radius = 10;

        for (Router r : graph.getRouters()) {
            int x = r.getX();
            int y = r.getY();

            g2d.setColor(Color.BLUE);
            g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}