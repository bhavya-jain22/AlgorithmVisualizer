import java.util.*;

// Feature 1: BFS - finds path with FEWEST HOPS (not lowest cost)
// Complexity: O(V + E) - explores layer by layer
public class BFSSolver {

    public PathResult solve(NetworkGraph graph, Router start, Router end) {
        if (start.equals(end)) return new PathResult(new ArrayList<>(), 0, 0);

        // BFS Queue - stores routers to visit next
        Queue<Router> queue = new LinkedList<>();
        Map<Router, Router> cameFrom = new HashMap<>(); // track path
        Set<Router> visited = new HashSet<>();
        int nodesVisited = 0;

        queue.add(start);
        visited.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            Router current = queue.poll();
            nodesVisited++;

            if (current.equals(end)) {
                // Reconstruct path
                List<Router> path = new ArrayList<>();
                Router c = end;
                int totalCost = 0;
                while (c != null) {
                    path.add(0, c);
                    c = cameFrom.get(c);
                }

                // Calculate total cost along the path
                for (int i = 0; i < path.size() - 1; i++) {
                    Router a = path.get(i);
                    Router b = path.get(i + 1);
                    for (Cable cable : graph.adjList.get(a)) {
                        if (cable.targetRouter.equals(b) && !cable.isBroken) {
                            totalCost += cable.weight;
                            break;
                        }
                    }
                }
                return new PathResult(path, totalCost, nodesVisited);
            }

            for (Cable cable : graph.adjList.get(current)) {
                if (!cable.isBroken && !visited.contains(cable.targetRouter)) {
                    visited.add(cable.targetRouter);
                    cameFrom.put(cable.targetRouter, current);
                    queue.add(cable.targetRouter);
                }
            }
        }
        return new PathResult(null, Integer.MAX_VALUE, nodesVisited); // No path
    }
}
