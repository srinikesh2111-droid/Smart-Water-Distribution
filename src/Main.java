import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        WaterNetwork network = new WaterNetwork();

        Reservoir r1 = new Reservoir("R1","North Reservoir",10000);
        Reservoir r2 = new Reservoir("R2","South Reservoir",8000);

        network.addReservoir(r1);
        network.addReservoir(r2);

        network.addArea(new ResidentialArea("A1","North Residential",2000,100));
        network.addArea(new ResidentialArea("A2","East Residential",1500, 75));
        network.addArea(new CriticalInstitution("A3", "Government Hospital",3000,"Hospital",100));
        network.addArea(new ResidentialArea("A4","Central Residential",2500,125));
        network.addArea(new ResidentialArea("A5","Government School Area",1800, 90));
        network.addArea(new ResidentialArea("A6","South Residential",2200,110));
        network.addArea(new CriticalInstitution("A7","Emergency Centre",3500,"Emergency Centre",50));
        network.addArea(new ResidentialArea("A8","West Residential",1700,85));

        network.addPipeline(new Pipeline("P1", "R1", "A1"));
        network.addPipeline(new Pipeline("P2", "R1", "A2"));
        network.addPipeline(new Pipeline("P3", "A1", "A3"));
        network.addPipeline(new Pipeline("P4", "A2", "A4"));
        network.addPipeline(new Pipeline("P5", "R2", "A5"));
        network.addPipeline(new Pipeline("P6", "R2", "A6"));
        network.addPipeline(new Pipeline("P7", "A5", "A7"));
        network.addPipeline(new Pipeline("P8", "A6", "A8"));

        int choice;

        do {

            System.out.println("\n--- SMART WATER DISTRIBUTION ---");

            System.out.println("1. Display Reservoirs");
            System.out.println("2. Display Demand Areas");
            System.out.println("3. Display Pipelines");
            System.out.println("4. Check Connectivity");
            System.out.println("5. Fail Pipeline");
            System.out.println("6. Repair Pipeline");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    network.displayReservoirs();
                    break;

                case 2:
                    network.displayAreas();
                    break;

                case 3:
                    network.displayPipelines();
                    break;

                case 4:

                    System.out.print(
                            "Enter Reservoir ID (R1/R2): ");

                    String reservoirId = sc.next();

                    System.out.print(
                            "Enter Area ID (A1-A8): ");

                    String areaId = sc.next();

                    Reservoir reservoir =
                            network.getReservoir(reservoirId);

                    DemandArea area =
                            network.getArea(areaId);

                    if (reservoir == null) {

                        System.out.println(
                                "Invalid Reservoir ID.");

                    } else if (area == null) {

                        System.out.println(
                                "Invalid Area ID.");

                    } else {

                        boolean connected =
                                network.isReachable(
                                        reservoirId,
                                        areaId);

                        if (connected) {

                            System.out.println(
                                    "Water can reach "
                                    + areaId
                                    + " from "
                                    + reservoirId);

                        } else {

                            System.out.println(
                                    "Water cannot reach "
                                    + areaId
                                    + " from "
                                    + reservoirId);
                        }
                    }

                    break;

                case 5:

                    System.out.print(
                            "Enter Pipeline ID to fail: ");

                    String failId = sc.next();

                    network.failPipeline(failId);

                    System.out.println(
                            "Pipeline marked as failed.");

                    break;

                case 6:

                    System.out.print(
                            "Enter Pipeline ID to repair: ");

                    String repairId = sc.next();

                    network.repairPipeline(repairId);

                    System.out.println(
                            "Pipeline repaired.");

                    break;

                case 7:

                    System.out.println(
                            "Exiting application.");

                    break;

                default:

                    System.out.println(
                            "Invalid choice.");
            }

        } while (choice != 7);

        sc.close();
    }
}