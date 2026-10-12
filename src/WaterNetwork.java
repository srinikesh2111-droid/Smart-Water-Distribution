import java.util.*;

public class WaterNetwork {
    private final Map<String, Reservoir> reservoirs = new LinkedHashMap<>();
    private final Map<String, DemandArea> areas = new LinkedHashMap<>();
    private final Map<String, List<String>> graph = new LinkedHashMap<>();
    private final List<Pipeline> pipelines = new ArrayList<>();

    public void addReservoir(Reservoir reservoir) {
        reservoirs.put(reservoir.getId(), reservoir);
        graph.putIfAbsent(reservoir.getId(), new ArrayList<>());
    }

    public void addArea(DemandArea area) {
        areas.put(area.getId(), area);
        graph.putIfAbsent(area.getId(), new ArrayList<>());
    }

    public void addPipeline(Pipeline pipeline) {
        if (!graph.containsKey(pipeline.getFrom()) || !graph.containsKey(pipeline.getTo())) {
            throw new IllegalArgumentException("Pipeline endpoints must exist: " + pipeline.getId());
        }
        pipelines.add(pipeline);
        graph.get(pipeline.getFrom()).add(pipeline.getTo());
        graph.get(pipeline.getTo()).add(pipeline.getFrom());
    }

    private boolean pipelineExists(String a, String b) {
        for (Pipeline p : pipelines) {
            if (p.isWorking() && ((p.getFrom().equals(a) && p.getTo().equals(b))
                    || (p.getFrom().equals(b) && p.getTo().equals(a)))) return true;
        }
        return false;
    }

    public boolean isReachable(String reservoirId, String areaId) {
        if (!reservoirs.containsKey(reservoirId) || !areas.containsKey(areaId)) return false;
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(reservoirId);
        visited.add(reservoirId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(areaId)) return true;
            for (String neighbour : graph.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbour) && pipelineExists(current, neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
        return false;
    }

    public List<String> getPath(String reservoirId, String areaId) {
        if (!reservoirs.containsKey(reservoirId) || !areas.containsKey(areaId)) return Collections.emptyList();
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        queue.add(reservoirId);
        visited.add(reservoirId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(areaId)) break;
            for (String next : graph.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(next) && pipelineExists(current, next)) {
                    visited.add(next);
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }
        if (!visited.contains(areaId)) return Collections.emptyList();
        LinkedList<String> path = new LinkedList<>();
        for (String at = areaId; at != null; at = previous.get(at)) {
            path.addFirst(at);
            if (at.equals(reservoirId)) break;
        }
        return path;
    }

    public Reservoir chooseReservoirForArea(String areaId) {
        Reservoir best = null;
        for (Reservoir r : reservoirs.values()) {
            if (isReachable(r.getId(), areaId)
                    && (best == null || r.getRemainingSupply() > best.getRemainingSupply()
                    || (r.getRemainingSupply() == best.getRemainingSupply()
                    && r.getId().compareTo(best.getId()) < 0))) {
                best = r;
            }
        }
        return best;
    }

    public Reservoir allocateToArea(String areaId, int litres) {
        Reservoir reservoir = chooseReservoirForArea(areaId);
        if (reservoir == null) return null;
        reservoir.allocateWater(litres);
        return reservoir;
    }

    public void displayReservoirs() {
        System.out.println("\\n--- RESERVOIRS ---");
        for (Reservoir r : reservoirs.values()) { r.displayDetails(); System.out.println(); }
    }
    public void displayAreas() {
        System.out.println("\\n--- DEMAND AREAS ---");
        for (DemandArea a : areas.values()) { a.displayDetails(); System.out.println(); }
    }
    public void displayPipelines() {
        System.out.println("\\n--- PIPELINES ---");
        for (Pipeline p : pipelines) p.displayDetails();
    }

    public DemandArea getArea(String id) { return areas.get(id); }
    public Reservoir getReservoir(String id) { return reservoirs.get(id); }
    public Pipeline getPipeline(String id) {
        for (Pipeline p : pipelines) if (p.getId().equals(id)) return p;
        return null;
    }
    public Collection<Reservoir> getReservoirs() { return Collections.unmodifiableCollection(reservoirs.values()); }
    public Collection<DemandArea> getAreas() { return Collections.unmodifiableCollection(areas.values()); }
    public List<Pipeline> getPipelines() { return Collections.unmodifiableList(pipelines); }

    public boolean failPipeline(String id) {
        Pipeline p = getPipeline(id);
        if (p == null || !p.isWorking()) return false;
        p.markFailed();
        return true;
    }
    public boolean repairPipeline(String id) {
        Pipeline p = getPipeline(id);
        if (p == null || p.isWorking()) return false;
        p.repair();
        return true;
    }
}
