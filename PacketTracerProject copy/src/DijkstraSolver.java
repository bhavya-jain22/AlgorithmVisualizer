import java.util.*;

public class DijkstraSolver {

    // Simple helper class to store the router and its current distance from start
    class DNode {
        Router router;
        int distance;

        public DNode(Router router, int distance) {
            this.router = router;
            this.distance = distance;
        }
    }

    public PathResult solve(NetworkGraph graph, Router start, Router end) {
        // Priority Queue sorting by shortest distance
        PriorityQueue<DNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        HashMap<Router, Integer> distMap = new HashMap<>();
        HashMap<Router, Router> parentMap = new HashMap<>();
        HashSet<Router> visited = new HashSet<>();

        int nodesVisited = 0;

        // 1. Setup starting node
        pq.add(new DNode(start, 0));
        distMap.put(start, 0);

        // 2. Main Dijkstra Loop
        while (!pq.isEmpty()) {
            DNode current = pq.poll();
            Router u = current.router;

            if (visited.contains(u))
                continue;

            visited.add(u);
            nodesVisited++;

            // If we reached the destination, stop searching
            if (u.id == end.id)
                break;

            // 3. Check neighbors
            for (Cable edge : graph.adjList.get(u)) {
                if (edge.isBroken)
                    continue; // Ignore broken cables!

                Router v = edge.targetRouter;
                if (visited.contains(v))
                    continue;

                int newDist = distMap.get(u) + edge.weight;

                // If we found a faster path, update it
                if (!distMap.containsKey(v) || newDist < distMap.get(v)) {
                    distMap.put(v, newDist);
                    parentMap.put(v, u);
                    pq.add(new DNode(v, newDist));
                }
            }
        }

        // 4. Reconstruct the path backwards
        if (!parentMap.containsKey(end) && start.id != end.id) {
            return new PathResult(new ArrayList<>(), -1, nodesVisited); // No path found
        }

        List<Router> finalPath = new ArrayList<>();
        Router currRouter = end;
        while (currRouter != null) {
            finalPath.add(currRouter);
            currRouter = parentMap.get(currRouter);
        }
        Collections.reverse(finalPath);

        return new PathResult(finalPath, distMap.getOrDefault(end, -1), nodesVisited);
    }
}