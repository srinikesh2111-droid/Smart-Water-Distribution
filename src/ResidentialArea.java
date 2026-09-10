public class ResidentialArea extends DemandArea {

    public ResidentialArea(String id, String name, int demand) {
        super(id, name, demand);
    }

    @Override
    public int allocationPriority() {
        return 1;
    }
}
