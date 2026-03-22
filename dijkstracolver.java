import java.util.*;

public class dijkstracolver {

    public pathresult solve(networkgraph graph, router start, router end) {
        Map<router, Integer> dist = new HashMap<>();
        Map<router, router> prev = new HashMap<>();
        PriorityQueue<router> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (router r : graph.routers) {
            dist.put(r, Integer.MAX_VALUE);
        }

        dist.put(start, 0);
        pq.add(start);

        int nodesVisited = 0;

        while (!pq.isEmpty()) {
            router current = pq.poll();
            nodesVisited++;

            if (current == end) break;

            for (cable cable : graph.adjList.get(current)) {
                if (cable.isBroken) continue;

                router neighbor = cable.target;
                int newDist = dist.get(current) + cable.weight;

                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        // Reconstruct path
        List<router> path = new ArrayList<>();
        router step = end;

        while (step != null && prev.containsKey(step)) {
            path.add(step);
            step = prev.get(step);
        }

        if (step == start) {
            path.add(start);
            Collections.reverse(path);
        }

        return new pathresult(path, dist.get(end), nodesVisited);
    }
}