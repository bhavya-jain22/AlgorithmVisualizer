import java.util.*;

// Feature 2: Kruskal's MST using Union-Find (Disjoint Set Union)
// Finds the minimum cost "backbone" network that connects all routers
// Complexity: O(E log E) for sorting edges
public class KruskalMST {

    // Union-Find data structure - the core of Kruskal's
    private int[] parent;
    private int[] rank;

    private int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // Path compression
        return parent[x];
    }

    private boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false; // Already in same set - adding would create a cycle!
        
        // Union by rank for efficiency
        if (rank[rootA] < rank[rootB]) { int t = rootA; rootA = rootB; rootB = t; }
        parent[rootB] = rootA;
        if (rank[rootA] == rank[rootB]) rank[rootA]++;
        return true;
    }

    public static class Edge {
        public Router from, to;
        public int weight;
        public Edge(Router from, Router to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    public List<Edge> computeMST(NetworkGraph graph) {
        List<Edge> allEdges = new ArrayList<>();
        
        // Collect all unique edges
        for (Router r : graph.allRouters) {
            for (Cable c : graph.adjList.get(r)) {
                if (!c.isBroken && r.id < c.targetRouter.id) { // Avoid duplicates
                    allEdges.add(new Edge(r, c.targetRouter, c.weight));
                }
            }
        }
        
        // Sort by weight ascending (Kruskal's core step)
        allEdges.sort(Comparator.comparingInt(e -> e.weight));
        
        int n = graph.allRouters.size();
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; rank[i] = 0; }
        
        // Map router id to index
        Map<Integer, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < graph.allRouters.size(); i++) {
            idToIndex.put(graph.allRouters.get(i).id, i);
        }

        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;
        
        for (Edge e : allEdges) {
            int idxA = idToIndex.get(e.from.id);
            int idxB = idToIndex.get(e.to.id);
            
            if (union(idxA, idxB)) { // Only add if it doesn't create a cycle
                mstEdges.add(e);
                totalCost += e.weight;
                if (mstEdges.size() == n - 1) break; // MST is complete
            }
        }
        return mstEdges;
    }
}
