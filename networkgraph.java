import java.util.*;

public class networkgraph {
    public Map<router, List<cable>> adjList = new HashMap<>();
    public List<router> routers = new ArrayList<>();

    public void addRouter(router r) {
        adjList.putIfAbsent(r, new ArrayList<>());
        routers.add(r);
    }

    public void addCable(router from, router to, int weight) {
        adjList.get(from).add(new cable(to, weight));
        adjList.get(to).add(new cable(from, weight)); // undirected
    }

    public void generateGrid() {
        int id = 0;
        int size = 6;
        router[][] grid = new router[size][size];

        // Create routers
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                router r = new router(id++, 50 + j * 80, 50 + i * 80);
                grid[i][j] = r;
                addRouter(r);
            }
        }

        // Connect neighbors
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i > 0) addCable(grid[i][j], grid[i - 1][j], 1);
                if (j > 0) addCable(grid[i][j], grid[i][j - 1], 1);
            }
        }
    }
}