import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class BenchmarkChartGUI extends JFrame {

    private Map<String, Double> averageNodesExplored;
    private int iterations;

    public BenchmarkChartGUI(Map<String, Double> averages, int iterations) {
        super("Benchmark Results (" + iterations + " Simulations)");
        this.averageNodesExplored = averages;
        this.iterations = iterations;

        setSize(800, 750);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ChartPanel chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(800, 450));
        add(chartPanel, BorderLayout.CENTER);
        
        // Add simple explanation at the bottom
        JTextArea explanation = new JTextArea();
        explanation.setEditable(false);
        explanation.setWrapStyleWord(true);
        explanation.setLineWrap(true);
        explanation.setBackground(new Color(245, 245, 250));
        explanation.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        explanation.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        explanation.setText("📊 What does this graph mean?\n\n" +
            "This graph shows how much 'thinking' each algorithm had to do to find the correct path in a chaotic, broken network.\n\n" +
            "🟢 A* (Green): Lowest bar because it is 'smart'. Uses a heuristic to search towards the target.\n\n" +
            "🔵 Dijkstra (Blue): Medium bar. Expands in all directions like a ripple in a pond.\n\n" +
            "🟣 Bellman-Ford (Purple): Highest bar. Brutally checks the entire network over and over again.\n\n" +
            "🟠 BFS (Orange): Checks nodes hop by hop without caring about cable latency/weight.");
            
        add(explanation, BorderLayout.SOUTH);
    }

    private class ChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            String[] algos = {"A*", "Dijkstra", "Bellman-Ford", "BFS"};
            Color[] colors = {new Color(46, 204, 113), new Color(52, 152, 219), new Color(155, 89, 182), new Color(230, 126, 34)};

            int maxVal = 0;
            for (Double v : averageNodesExplored.values()) {
                if (v > maxVal) maxVal = v.intValue();
            }
            if (maxVal == 0) maxVal = 1;

            int startY = 380;
            int startX = 100;
            int barWidth = 90;
            int spacing = 60;

            // Draw Title
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2d.drawString("Average Nodes Explored (Lower is Better)", startX, 40);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2d.drawString("Harsh Environment Test with random broken cables & pairs", startX, 65);

            // Draw Axes
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(startX, 80, startX, startY);
            g2d.drawLine(startX, startY, getWidth() - 50, startY);

            for (int i = 0; i < algos.length; i++) {
                String algo = algos[i];
                double avg = averageNodesExplored.getOrDefault(algo, 0.0);

                int barHeight = (int) ((avg / maxVal) * 280);
                int x = startX + spacing + (i * (barWidth + spacing));
                int y = startY - barHeight;

                g2d.setColor(colors[i]);
                g2d.fillRect(x, y, barWidth, barHeight);

                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, barWidth, barHeight);

                // Label X Axis
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString(algo, x + 10, startY + 20);

                // Label Value
                g2d.drawString(String.format("%.1f", avg), x + 20, y - 10);
            }
        }
    }
}
