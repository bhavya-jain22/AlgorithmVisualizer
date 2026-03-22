public class cable {
    public router target;
    public int weight;
    public boolean isBroken;

    public cable(router target, int weight) {
        this.target = target;
        this.weight = weight;
        this.isBroken = false;
    }
}