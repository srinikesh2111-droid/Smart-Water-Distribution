import java.util.List;
import java.util.ArrayList;

/** Dependency-free regression checks for the Review 2 demo. Run with: java -ea ProjectSelfTest */
public class ProjectSelfTest {
    private static int passed = 0;
    private static final List<String> results = new ArrayList<>();

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAIL: " + name);
        passed++;
        results.add("PASS: " + name);
    }

    public static void main(String[] args) {
        WaterNetwork n = SwingMain.createNetwork();
        check(n.getReservoirs().size() == 2, "sample network has 2 reservoirs");
        check(n.getAreas().size() == 10, "sample network has 10 demand areas");
        check(n.getPipelines().size() == 12, "sample network has 12 pipelines");
        check(n.isReachable("R1", "A3"), "hospital initially reachable from R1");
        check(n.isReachable("R2", "A7"), "emergency centre initially reachable from R2");
        check(n.isReachable("R1", "A10"), "A10 initially reachable through active links");
        check(!n.isReachable("R9", "A3"), "unknown reservoir is not reachable");
        check(!n.isReachable("R1", "A99"), "unknown demand area is not reachable");
        check(n.getPath("R1", "A3").size() >= 2, "BFS returns a valid non-empty path");
        check(n.getPath("R1", "A99").isEmpty(), "BFS returns empty path for unknown area");

        Reservoir r1 = n.getReservoir("R1");
        check(r1.allocateWater(12000) == 10000, "allocation is capped at available supply");
        check(r1.getRemainingSupply() == 0, "reservoir supply never becomes negative");
        check(r1.allocateWater(100) == 0, "dry reservoir allocates zero litres");
        check(r1.allocateWater(0) == 0, "zero-litre allocation safely returns zero");
        check(r1.allocateWater(-1) == 0, "negative allocation safely returns zero");
        r1.resetDailySupply();
        check(r1.getRemainingSupply() == r1.getCapacity(), "daily reset restores capacity");
        r1.setRemainingSupply(-50);
        check(r1.getRemainingSupply() == 0, "supply is clamped at zero");
        r1.setRemainingSupply(999999);
        check(r1.getRemainingSupply() == r1.getCapacity(), "supply is clamped at capacity");
        r1.setRemainingSupply(5000);

        WaterRequest request = new WaterRequest("WR-TEST", "A3", 1000, "Test");
        check(request.getUnmetLitres() == 1000, "new request starts with full unmet quantity");
        request.recordAllocation(300);
        check(request.getAllocatedLitres() == 300 && request.getUnmetLitres() == 700,
                "partial allocation retains unmet litres");
        request.recordAllocation(900);
        check(request.isComplete() && request.getAllocatedLitres() == 1000 && request.getUnmetLitres() == 0,
                "repeated allocation cannot exceed original request");
        request.recordAllocation(500);
        check(request.getAllocatedLitres() == 1000, "completed request cannot over-allocate");
        boolean rejectsZeroRequest = false;
        try { new WaterRequest("WR-BAD", "A3", 0, "Test"); }
        catch (IllegalArgumentException expected) { rejectsZeroRequest = true; }
        check(rejectsZeroRequest, "water request rejects zero quantity");

        check(n.failPipeline("P9"), "working pipeline can be failed");
        check(!n.failPipeline("P9"), "already-failed pipeline cannot fail twice");
        check(!n.isReachable("R1", "A9"), "P9 failure disconnects A9 from R1");
        check(!n.isReachable("R1", "A10"), "P9 failure disconnects A10 from R1");
        check(n.repairPipeline("P9"), "failed pipeline can be repaired");
        check(!n.repairPipeline("P9"), "already-working pipeline cannot repair twice");
        check(n.isReachable("R1", "A10"), "repair restores A10 reachability");
        check(!n.failPipeline("P999"), "unknown pipeline failure is rejected");
        check(!n.repairPipeline("P999"), "unknown pipeline repair is rejected");

        WaterAllocationPolicy policy = new DefaultWaterAllocationPolicy();
        List<DemandArea> priority = policy.orderAreas(n.getAreas(), true);
        check(priority.get(0).allocationPriority() == 3, "priority order starts with critical demand");
        check(priority.get(priority.size()-1).allocationPriority() == 1, "priority order ends with normal demand");
        check("A3".equals(priority.get(0).getId()), "critical tie-break is deterministic by area ID");
        List<DemandArea> idOrder = policy.orderAreas(n.getAreas(), false);
        check("A1".equals(idOrder.get(0).getId()), "ID-order mode starts at A1");
        check("A9".equals(idOrder.get(8).getId()), "ID-order mode places A9 before A10 using natural numeric ordering");
        check("A10".equals(idOrder.get(9).getId()), "ID-order mode ends with A10");

        for (String result : results) System.out.println(result);
        System.out.println("TOTAL: " + passed + " checks passed.");
    }
}
