import java.util.*;

public class BellmanFordSolver {

    public PathResult solve(NetworkGraph graph, Router start, Router end) {
        HashMap<Router, Integer> distMap = new HashMap<>();
        HashMap<Router, Router> parentMap = new HashMap<>();
        int nodesVisited = 0;

        // Initialize distances to infinity
        for (Router r : graph.allRouters) {
            distMap.put(r, Integer.MAX_VALUE / 2);
        }
        distMap.put(start, 0); // Distance of start node is always 0

        int V = graph.allRouters.size();

        // Relax all edges V - 1 times
        // This is the core of Bellman-Ford. It iterates through every single edge
        // repeatedly.
        for (int i = 0; i < V - 1; i++) {
            boolean updated = false; // Optimization flag

            for (Router u : graph.allRouters) {
                if (distMap.get(u) == Integer.MAX_VALUE / 2)
                    continue;

                nodesVisited++; // for analysis

                // Check all cables connected to router 'u'
                for (Cable edge : graph.adjList.get(u)) {
                    if (edge.isBroken)
                        continue; //skip if broken

                    Router v = edge.targetRouter;
                    int weight = edge.weight;

                    // If we found a shorter path to 'v' through 'u', update it
                    if (distMap.get(u) + weight < distMap.get(v)) {
                        distMap.put(v, distMap.get(u) + weight);
                        parentMap.put(v, u);
                        updated = true;
                    }
                }
            }
            // If we went through all edges and didn't update anything, we can stop early!
            if (!updated)
                break;
        }

        // Build the final path (Same logic as your A* reconstructor)
        if (!parentMap.containsKey(end) && start.id != end.id) {
            return new PathResult(new ArrayList<>(), -1, nodesVisited); // No path found
        }

        List<Router> finalPath = new ArrayList<>();
        Router curr = end;
        while (curr != null) {
            finalPath.add(curr);
            curr = parentMap.get(curr);
        }
        Collections.reverse(finalPath);

        return new PathResult(finalPath, distMap.get(end), nodesVisited);
    }
}