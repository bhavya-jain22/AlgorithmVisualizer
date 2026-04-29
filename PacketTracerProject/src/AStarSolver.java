import java.util.*;

public class AStarSolver {

    private class NodeRecord implements Comparable<NodeRecord> {
        Router node; // node we are working
        int fCost; // Total cost (Actual weight + Heuristic Cost)

        public NodeRecord(Router node, int fCost) { // constructor
            this.node = node;
            this.fCost = fCost;
        }

        public int compareTo(NodeRecord other) { // for PQ comparing two objects
            return Integer.compare(this.fCost, other.fCost);
        }
    }

    // Distance between two routers (scaled down to remain admissible)
    private int getHeuristic(Router a, Router b) {
        // Grid spacing is 55, min cable weight is 1. Dividing by 55 ensures h(n) <= true cost.
        return (Math.abs(a.x - b.x) + Math.abs(a.y - b.y)) / 55;
    }

    public PathResult solve(NetworkGraph graph, Router start, Router target) {
        PriorityQueue<NodeRecord> pq = new PriorityQueue<>(); // shortest path first Node,Cost
        Map<Router, Integer> gCosts = new HashMap<>(); // Actual cost from start used to compare if we found any new
                                                       // cost
        Map<Router, Router> previousNode = new HashMap<>(); // store where are we coming from

        int nodesVisitedCount = 0;

        //  all costs to "Infinity"
        for (Router i : graph.allRouters) {
            gCosts.put(i, Integer.MAX_VALUE);
        }
        gCosts.put(start, 0);
        pq.add(new NodeRecord(start, getHeuristic(start, target)));

        while (!pq.isEmpty()) {
            Router current = pq.poll().node; // top of pq grab it and remove it return node of it from Node Record

            nodesVisitedCount++; // count for our performance scoreboard

            if (current.id == target.id)
                break;

            // return blank if no neighbor or the cables connected safe for handling
            // exception inside java.util.map
            for (Cable edge : graph.adjList.getOrDefault(current, new ArrayList<>())) {

                if (edge.isBroken)
                    continue; // Skip broken cables 

                Router neighbor = edge.targetRouter; // return the other end of the cable
                int tentativeGCost = gCosts.get(current) + edge.weight; // total cost for this path

                if (tentativeGCost < gCosts.get(neighbor)) {
                    gCosts.put(neighbor, tentativeGCost);
                    previousNode.put(neighbor, current);

                    int fCost = tentativeGCost + getHeuristic(neighbor, target);
                    pq.add(new NodeRecord(neighbor, fCost));
                }
            }
        }

        // building path backward adding in front using LinkedList
        List<Router> path = new LinkedList<>();

        // If we never reached the target (cost is still Infinity)
        if (gCosts.get(target) == Integer.MAX_VALUE && start.id != target.id) {
            return new PathResult(path, -1, nodesVisitedCount);
        }

        Router curr = target;
        while (curr != null) {
            path.add(0, curr); // Adds directly to the front
            curr = previousNode.get(curr);
        }

        return new PathResult(path, gCosts.get(target), nodesVisitedCount);
    }
}