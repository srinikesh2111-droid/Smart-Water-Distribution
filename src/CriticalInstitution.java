public class CriticalInstitution extends DemandArea {

    public CriticalInstitution(String id, String name, int demand) {
        super(id, name, demand);
    }

    @Override
    public int allocationPriority() {
        return 3;
    }
}
