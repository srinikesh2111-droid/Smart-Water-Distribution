public abstract class DemandArea {

    private String id;
    private String name;
    private int demand;

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

    public abstract int allocationPriority();

    public abstract void displayDetails();
}
