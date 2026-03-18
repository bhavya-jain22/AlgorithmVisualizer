import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Assume graph is already constructed elsewhere
        NetworkGraph graph = new NetworkGraph();

        // TEMP: you will populate this later
        // graph.addRouter(...);
        // graph.addCable(...);

        JFrame frame = new JFrame("PacketTracer - Network Visualizer");

        PacketPanel panel = new PacketPanel(graph);

        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}