import java.util.*;

public class pathresult {
    public List<router> path;
    public int totalCost;
    public int nodesVisited;

    public pathresult(List<router> path, int totalCost, int nodesVisited) {
        this.path = path;
        this.totalCost = totalCost;
        this.nodesVisited = nodesVisited;
    }
}