public class CriticalInstitution extends DemandArea {
    private final String institutionType;
    private final int emergencyBeds;

    public CriticalInstitution(String id, String name, int demand,
                               String institutionType, int emergencyBeds) {
        super(id, name, demand);
        this.institutionType = institutionType;
        this.emergencyBeds = emergencyBeds;
    }

    public String getInstitutionType() { return institutionType; }
    public int getEmergencyBeds() { return emergencyBeds; }

    @Override public int allocationPriority() { return 3; }

    @Override public void displayDetails() {
        System.out.println("Area Type      : Critical Institution");
        System.out.println("Area ID        : " + getId());
        System.out.println("Area Name      : " + getName());
        System.out.println("Water Demand   : " + getDemand() + " litres");
        System.out.println("Institution    : " + institutionType);
        System.out.println("Emergency Beds : " + emergencyBeds);
        System.out.println("Priority       : Critical");
    }
}
