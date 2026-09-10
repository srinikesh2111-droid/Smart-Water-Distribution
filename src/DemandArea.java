public class DemandArea {

    protected String id;
    protected String name;
    protected int demand;

    public DemandArea(String id, String name, int demand) {
        this.id = id;
        this.name = name;
        this.demand = demand;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDemand() {
        return demand;
    }

    public int allocationPriority() {
        return 1;
    }
}
