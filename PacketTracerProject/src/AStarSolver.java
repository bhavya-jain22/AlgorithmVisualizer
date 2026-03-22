import java.util.*;

public class AStarSolver {

    // Helper class to store the router and its costs for our priority queue.
    // Like how we make a custom struct for graph problems to track weights.
    class NodeCost {
        Router router;
        int gCost; // Actual distance from the starting router
        int fCost; // gCost + heuristic (our guess to the end)

        public NodeCost(Router router, int gCost, int fCost) {
            this.router = router;
            this.gCost = gCost;
            this.fCost = fCost;
        }
    }

    // Calculating the Manhattan distance here.
    // Formula: |x1 - x2| + |y1 - y2|
    private int getManhattanDist(Router current, Router target) {
        // finding absolute difference of coordinates, no need for heavy math libraries
        int dx = Math.abs(current.x - target.x);
        int dy = Math.abs(current.y - target.y);
        
        return dx + dy; 
    }

    // Main solver function. This is what the UI and main method will call.
    public PathResult solve(NetworkGraph graph, Router start, Router end) {

        // This is our min-heap. We are using a lambda function to tell it to sort by
        // fCost (lowest first).
        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));

        // We need these to track our progress.
        // gCostMap stores the shortest distance from start to a specific router.
        HashMap<Router, Integer> gCostMap = new HashMap<>();

        // parentMap remembers where we came from so we can draw the line backward at
        // the end.
        HashMap<Router, Router> parentMap = new HashMap<>();

        // visited set keeps us from going in circles
        HashSet<Router> visited = new HashSet<>();

        int nodesVisitedCount = 0; // Tracking this for Bhavya's UI scoreboard

        // 1. Setup the starting node
        pq.add(new NodeCost(start, 0, getManhattanDist(start, end)));
        gCostMap.put(start, 0);

        // 2. The main A* Loop
        while (!pq.isEmpty()) {
            NodeCost current = pq.poll();
            Router currNode = current.router;

            // If we already fully processed this router, skip it
            if (visited.contains(currNode)) {
                continue;
            }

            visited.add(currNode);
            nodesVisitedCount++; // We officially visited a new node!

            // Did we reach the destination?
            if (currNode.id == end.id) {
                // We found it! Now we just need to reconstruct the path backwards.
                // We will write the buildPath helper method in the next step.
                return buildPath(parentMap, end, gCostMap.get(end), nodesVisitedCount);
            }

            // 3. Check all the cables connected to our current router
            // Note: Assuming Vansh named the adjacency list 'adjList' in NetworkGraph
            ArrayList<Cable> connections = graph.adjList.get(currNode);

            if (connections != null) {
                for (Cable c : connections) {

                    // CRITICAL: If the user clicked and broke this cable, pretend it doesn't exist
                    if (c.isBroken) {
                        continue;
                    }

                    Router neighbor = c.targetRouter;

                    if (visited.contains(neighbor)) {
                        continue; // Already locked in the shortest path for this neighbor
                    }

                    // Calculate the new gCost (current gCost + weight of this cable)
                    int newGCost = gCostMap.get(currNode) + c.weight;

                    // If we haven't seen this neighbor before, OR we found a faster way to get to
                    // it
                    if (!gCostMap.containsKey(neighbor) || newGCost < gCostMap.get(neighbor)) {

                        gCostMap.put(neighbor, newGCost);

                        // f(n) = g(n) + h(n)
                        int fCost = newGCost + getManhattanDist(neighbor, end);

                        pq.add(new NodeCost(neighbor, newGCost, fCost));
                        parentMap.put(neighbor, currNode); // update the parent to draw the new path
                    }
                }
            }
        }

        // If the queue empties out and we never hit the return statement inside the
        // loop,
        // it means the network is completely broken and there is no possible path.
        return new PathResult(new ArrayList<>(), -1, nodesVisitedCount);
    }
    // Helper method to backtrack and build the actual list of routers.
    // We call this the second our main loop hits the destination router.
    private PathResult buildPath(HashMap<Router, Router> parentMap, Router current, int totalCost, int nodesVisited) {
        List<Router> finalPath = new ArrayList<>();
        
        // Trace backwards from the destination to the start
        while (current != null) {
            finalPath.add(current);
            // Move up the chain to the router that discovered this current one
            current = parentMap.get(current); 
        }
        
        // Since we started at the end and went backward, our list is currently End -> Start.
        // We need to flip it so the UI draws it correctly from Start -> End.
        Collections.reverse(finalPath);
        
        // Package it all up into the result object so Bhavya's UI can read the stats.
        return new PathResult(finalPath, totalCost, nodesVisited);
    }
} 