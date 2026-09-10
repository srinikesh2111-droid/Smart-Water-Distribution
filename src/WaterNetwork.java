import java.util.*;

public class WaterNetwork {

    private Map<String, List<String>> graph;
    private Map<String, Reservoir> reservoirs;
    private Map<String, DemandArea> areas;
    private List<Pipeline> pipelines;

    public WaterNetwork() {
        graph = new HashMap<>();
        reservoirs = new HashMap<>();
        areas = new HashMap<>();
        pipelines = new ArrayList<>();
    }

    public void addReservoir(Reservoir reservoir) {
        reservoirs.put(reservoir.getId(), reservoir);
        graph.putIfAbsent(reservoir.getId(), new ArrayList<>());
    }

    public void addArea(DemandArea area) {
        areas.put(area.getId(), area);
        graph.putIfAbsent(area.getId(), new ArrayList<>());
    }

    public void addPipeline(Pipeline pipeline) {
        pipelines.add(pipeline);

        graph.get(pipeline.getFrom()).add(pipeline.getTo());
        graph.get(pipeline.getTo()).add(pipeline.getFrom());
    }

    private boolean pipelineExists(String a, String b) {
        for (Pipeline p : pipelines) {
            if (!p.isWorking()) {
                continue;
            }

            if ((p.getFrom().equals(a) && p.getTo().equals(b)) ||
                (p.getFrom().equals(b) && p.getTo().equals(a))) {
                return true;
            }
        }
        return false;
    }

    public boolean isReachable(String start, String target) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(target)) {
                return true;
            }

            for (String neighbour : graph.get(current)) {
                if (!visited.contains(neighbour)
                        && pipelineExists(current, neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        return false;
    }
}
