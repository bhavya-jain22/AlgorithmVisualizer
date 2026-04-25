public class Cable {
    public Router targetRouter;
    public int weight; // Simulates network latency
    public boolean isBroken; // The chaos toggle!

    public Cable(Router targetRouter, int weight) {
        this.targetRouter = targetRouter;
        this.weight = weight;
        this.isBroken = false; // Cables start out working fine
    }
}