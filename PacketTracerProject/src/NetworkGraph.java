import java.util.*;

public class NetworkGraph {
    
    // The Adjacency List: Maps a Router to a list of its connected Cables
    public HashMap<Router, ArrayList<Cable>> adjList;
    
    // A flat list of all routers just to make drawing them on the UI easier later
    public List<Router> allRouters;
    
    public String currentTopology = "Grid";

    public NetworkGraph() {
        adjList = new HashMap<>();
        allRouters = new ArrayList<>();
    }

    public void resetGraph() {
        adjList.clear();
        allRouters.clear();
    }

    // Helper to add a router to our lists
    private void addRouter(Router r) {
        allRouters.add(r);
        adjList.put(r, new ArrayList<>());
    }

    // Undirected graph: A cable goes both ways!
    public void addCable(Router a, Router b, int weight) {
        adjList.get(a).add(new Cable(b, weight));
        adjList.get(b).add(new Cable(a, weight));
    }

    // ============ TOPOLOGY 1: Grid (default) ============
    public void generateGrid() {
        resetGraph();
        currentTopology = "Grid";
        int rows = 11, cols = 12, spacing = 55, startX = 40, startY = 40;
        Router[][] grid = new Router[rows][cols];
        int idCounter = 0;
        Random rand = new Random();

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                Router rr = new Router(idCounter++, startX + (c * spacing), startY + (r * spacing));
                grid[r][c] = rr;
                addRouter(rr);
            }

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (c < cols - 1) addCable(grid[r][c], grid[r][c + 1], rand.nextInt(10) + 1);
                if (r < rows - 1) addCable(grid[r][c], grid[r + 1][c], rand.nextInt(10) + 1);
            }
    }


}