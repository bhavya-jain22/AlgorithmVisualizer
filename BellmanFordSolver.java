import java.util.*;

public class BellmanFordSolver {

    public PathResult findShortestPath(NetworkGraph graph, Router source, Router destination) {

        Map<Router, Integer> dist = new HashMap<>();
        Map<Router, Router> parent = new HashMap<>();

        // Initialization
        for (Router r : graph.getRouters()) {
            dist.put(r, Integer.MAX_VALUE);
            parent.put(r, null);
        }
        dist.put(source, 0);

        int nodesVisited = 0;
        int V = graph.getRouters().size();

        // Relax edges V-1 times
        for (int i = 1; i <= V - 1; i++) {
            boolean updated = false;

            for (Cable c : graph.getCables()) {

                if (c.isBroken()) continue; // STRICT requirement

                Router u = c.getSource();
                Router v = c.getDestination();
                int weight = c.getWeight();

                if (dist.get(u) != Integer.MAX_VALUE &&
                    dist.get(u) + weight < dist.get(v)) {

                    dist.put(v, dist.get(u) + weight);
                    parent.put(v, u);
                    nodesVisited++;
                    updated = true;
                }

                // If undirected, also relax reverse
                if (dist.get(v) != Integer.MAX_VALUE &&
                    dist.get(v) + weight < dist.get(u)) {

                    dist.put(u, dist.get(v) + weight);
                    parent.put(u, v);
                    nodesVisited++;
                    updated = true;
                }
            }

            // Optimization: stop early if no updates
            if (!updated) break;
        }

        // Reconstruct path
        List<Router> path = new ArrayList<>();
        Router curr = destination;

        while (curr != null) {
            path.add(curr);
            curr = parent.get(curr);
        }

        Collections.reverse(path);

        int totalCost = dist.get(destination);

        return new PathResult(path, totalCost, nodesVisited);
    }
}