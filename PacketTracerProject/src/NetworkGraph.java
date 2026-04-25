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

    // ============ TOPOLOGY 2: Ring ============
    public void generateRing() {
        resetGraph();
        currentTopology = "Ring";
        int n = 16;
        Random rand = new Random();
        int cx = 380, cy = 320, radius = 260;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = (int)(cx + radius * Math.cos(angle));
            int y = (int)(cy + radius * Math.sin(angle));
            addRouter(new Router(i, x, y));
        }

        for (int i = 0; i < n; i++) {
            addCable(allRouters.get(i), allRouters.get((i + 1) % n), rand.nextInt(10) + 1);
            // Add some cross-connections for interest
            if (i % 3 == 0) addCable(allRouters.get(i), allRouters.get((i + n/2) % n), rand.nextInt(15) + 5);
        }
    }

    // ============ TOPOLOGY 3: Star ============
    public void generateStar() {
        resetGraph();
        currentTopology = "Star";
        int spokes = 12, nodesPerSpoke = 4;
        Random rand = new Random();
        int cx = 400, cy = 330;
        int idCounter = 0;

        Router center = new Router(idCounter++, cx, cy);
        addRouter(center);

        for (int s = 0; s < spokes; s++) {
            double angle = 2 * Math.PI * s / spokes;
            Router prev = center;
            for (int n = 1; n <= nodesPerSpoke; n++) {
                int x = (int)(cx + n * 85 * Math.cos(angle));
                int y = (int)(cy + n * 75 * Math.sin(angle));
                Router r = new Router(idCounter++, x, y);
                addRouter(r);
                addCable(prev, r, rand.nextInt(10) + 1);
                prev = r;
            }
        }
    }

    // ============ TOPOLOGY 4: Full Mesh (small) ============
    public void generateMesh() {
        resetGraph();
        currentTopology = "Mesh";
        int n = 20;
        Random rand = new Random();
        int cx = 390, cy = 330, radius = 270;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = (int)(cx + radius * Math.cos(angle));
            int y = (int)(cy + radius * Math.sin(angle));
            addRouter(new Router(i, x, y));
        }

        // Connect every node to every other node within distance threshold (partial mesh)
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Router a = allRouters.get(i), b = allRouters.get(j);
                double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
                if (dist < 320) {
                    addCable(a, b, rand.nextInt(15) + 1);
                }
            }
        }
    }
}