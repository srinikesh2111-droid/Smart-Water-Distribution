public abstract class DemandArea {
    private final String id;
    private final String name;
    private final int demand;

    public DemandArea(String id, String name, int demand) {
        if (id == null || name == null || demand < 0) {
            throw new IllegalArgumentException("Invalid demand-area data.");
        }
        this.id = id;
        this.name = name;
        this.demand = demand;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getDemand() { return demand; }
    public abstract int allocationPriority();

    public String priorityLabel() {
        switch (allocationPriority()) {
            case 3: return "Critical";
            case 2: return "High";
            default: return "Normal";
        }
    }

    public abstract void displayDetails();
}
