public class Main {

    public static void main(String[] args) {

        WaterNetwork network = new WaterNetwork();

        // Reservoirs
        Reservoir r1 = new Reservoir("R1", "North Reservoir", 10000);
        Reservoir r2 = new Reservoir("R2", "South Reservoir", 8000);

        network.addReservoir(r1);
        network.addReservoir(r2);

        // Demand areas
        DemandArea a1 =
                new ResidentialArea("A1", "North Residential", 2000);

        DemandArea a2 =
                new ResidentialArea("A2", "East Residential", 1500);

        DemandArea a3 =
                new CriticalInstitution("A3", "Government Hospital", 3000);

        DemandArea a4 =
                new ResidentialArea("A4", "Central Residential", 2500);

        network.addArea(a1);
        network.addArea(a2);
        network.addArea(a3);
        network.addArea(a4);

        // Pipelines
        network.addPipeline(
                new Pipeline("P1", "R1", "A1"));

        network.addPipeline(
                new Pipeline("P2", "R1", "A2"));

        network.addPipeline(
                new Pipeline("P3", "A1", "A3"));

        network.addPipeline(
                new Pipeline("P4", "A2", "A4"));

        // Connectivity test
        System.out.println(
                "Can R1 reach A3? "
                + network.isReachable("R1", "A3"));

        System.out.println(
                "Can R1 reach A4? "
                + network.isReachable("R1", "A4"));
    }
}
