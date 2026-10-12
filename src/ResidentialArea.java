public class ResidentialArea extends DemandArea {
    private final int numberOfHouses;

    public ResidentialArea(String id, String name, int demand, int numberOfHouses) {
        super(id, name, demand);
        this.numberOfHouses = numberOfHouses;
    }

    public int getNumberOfHouses() { return numberOfHouses; }
    @Override public int allocationPriority() { return 1; }

    @Override public void displayDetails() {
        System.out.println("Area Type      : Residential");
        System.out.println("Area ID        : " + getId());
        System.out.println("Area Name      : " + getName());
        System.out.println("Water Demand   : " + getDemand() + " litres");
        System.out.println("Number Houses  : " + numberOfHouses);
        System.out.println("Priority       : Normal");
    }
}
