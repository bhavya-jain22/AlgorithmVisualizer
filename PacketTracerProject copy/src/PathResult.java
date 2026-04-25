import java.util.List;

public class PathResult {
    public List<Router> path;
    public int totalCost;
    public int nodesVisited;

    public PathResult(List<Router> path, int totalCost, int nodesVisited) {
        this.path = path;
        this.totalCost = totalCost;
        this.nodesVisited = nodesVisited;
    }
}