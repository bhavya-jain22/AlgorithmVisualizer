import java.util.Objects;

public class Router {
    public int id;
    public int x;
    public int y;

    public Router(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    // This tells Java's HashMap how to check if two routers are the exact same node
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Router router = (Router) o;
        return id == router.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}