import java.util.*;

public class NetworkGraph {
    
    // The Adjacency List: Maps a Router to a list of its connected Cables
    public HashMap<Router, ArrayList<Cable>> adjList;
    
    // A flat list of all routers just to make drawing them on the UI easier later
    public List<Router> allRouters;

    public NetworkGraph() {
        adjList = new HashMap<>();
        allRouters = new ArrayList<>();
    }

    // Helper to add a router to our lists
    private void addRouter(Router r) {
        allRouters.add(r);
        adjList.put(r, new ArrayList<>());
    }

    // Undirected graph: A cable goes both ways!
    private void addCable(Router a, Router b, int weight) {
        adjList.get(a).add(new Cable(b, weight));
        adjList.get(b).add(new Cable(a, weight));
    }

    // This builds the 6x6 test environment automatically
    public void generateGrid() {
        int rows = 11;
        int cols = 12;
        int spacing = 55; // px
        int startX = 40;   
        int startY = 40;

        Router[][] grid = new Router[rows][cols];
        int idCounter = 0;
        Random rand = new Random();

        // 1. Create all 36 routers and assign their X, Y screen coordinates
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Router newRouter = new Router(idCounter++, startX + (c * spacing), startY + (r * spacing));
                grid[r][c] = newRouter;
                addRouter(newRouter);
            }
        }

        // 2. Connect them horizontally and vertically with random weights (1 to 10)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Connect to the right
                if (c < cols - 1) {
                    int weight = rand.nextInt(10) + 1;
                    addCable(grid[r][c], grid[r][c + 1], weight);
                }
                // Connect downwards
                if (r < rows - 1) {
                    int weight = rand.nextInt(10) + 1;
                    addCable(grid[r][c], grid[r + 1][c], weight);
                }
            }
        }
    }
}