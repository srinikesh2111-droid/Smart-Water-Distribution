public class HighPriorityArea extends DemandArea {
    private final String areaType;

    public HighPriorityArea(String id, String name, int demand, String areaType) {
        super(id, name, demand);
        this.areaType = areaType;
    }

    public String getAreaType() { return areaType; }
    @Override public int allocationPriority() { return 2; }

    @Override public void displayDetails() {
        System.out.println("Area Type      : High Priority");
        System.out.println("Area ID        : " + getId());
        System.out.println("Area Name      : " + getName());
        System.out.println("Water Demand   : " + getDemand() + " litres");
        System.out.println("Category       : " + areaType);
        System.out.println("Priority       : High");
    }
}
