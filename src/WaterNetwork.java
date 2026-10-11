import java.util.*;

public class WaterNetwork {

    private Map<String, Reservoir> reservoirs;
    private Map<String, DemandArea> areas;
    private Map<String, List<String>> graph;
    private List<Pipeline> pipelines;

    public WaterNetwork() {

        reservoirs = new HashMap<>();
        areas = new HashMap<>();
        graph = new HashMap<>();
        pipelines = new ArrayList<>();
    }

    public void addReservoir(Reservoir reservoir) {

        reservoirs.put(reservoir.getId(), reservoir);

        graph.putIfAbsent(
                reservoir.getId(),
                new ArrayList<>()
        );
    }

    public void addArea(DemandArea area) {

        areas.put(area.getId(), area);

        graph.putIfAbsent(
                area.getId(),
                new ArrayList<>()
        );
    }

    public void addPipeline(Pipeline pipeline) {

        pipelines.add(pipeline);

        graph.get(pipeline.getFrom())
             .add(pipeline.getTo());

        graph.get(pipeline.getTo())
             .add(pipeline.getFrom());
    }

    private boolean pipelineExists(String a, String b) {

        for (Pipeline p : pipelines) {

            if (!p.isWorking()) {
                continue;
            }

            if ((p.getFrom().equals(a)
                    && p.getTo().equals(b))
                ||
                (p.getFrom().equals(b)
                    && p.getTo().equals(a))) {

                return true;
            }
        }

        return false;
    }

    public boolean isReachable(
            String reservoirId,
            String areaId) {

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(reservoirId);
        visited.add(reservoirId);

        while (!queue.isEmpty()) {

            String current = queue.poll();

            if (current.equals(areaId)) {
                return true;
            }

            for (String neighbour : graph.get(current)) {

                if (!visited.contains(neighbour)
                        && pipelineExists(
                                current,
                                neighbour)) {

                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        return false;
    }

    public void displayReservoirs() {

        System.out.println("\n--- RESERVOIRS ---");

        for (Reservoir r : reservoirs.values()) {
            r.displayDetails();
            System.out.println();
        }
    }

    public void displayAreas() {

        System.out.println("\n--- DEMAND AREAS ---");

        for (DemandArea area : areas.values()) {

            area.displayDetails();
            System.out.println();
        }
    }

    public void displayPipelines() {

        System.out.println("\n--- PIPELINES ---");

        for (Pipeline p : pipelines) {
            p.displayDetails();
        }
    }

    public DemandArea getArea(String id) {
        return areas.get(id);
    }

    public Reservoir getReservoir(String id) {
        return reservoirs.get(id);
    }

    public void failPipeline(String id) {

        for (Pipeline p : pipelines) {

            if (p.getId().equals(id)) {
                p.markFailed();
                return;
            }
        }
    }

    public void repairPipeline(String id) {

        for (Pipeline p : pipelines) {

            if (p.getId().equals(id)) {
                p.repair();
                return;
            }
        }
    }
}