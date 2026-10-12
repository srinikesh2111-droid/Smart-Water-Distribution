import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WaterRequest {
    private final String id;
    private final String areaId;
    private final int requestedLitres;
    private final String mode;
    private final LocalDateTime createdAt;
    private int allocatedLitres;
    private int unmetLitres;

    public WaterRequest(String id, String areaId, int requestedLitres, String mode) {
        this(id, areaId, requestedLitres, mode, LocalDateTime.now());
    }

    public WaterRequest(String id, String areaId, int requestedLitres, String mode, LocalDateTime createdAt) {
        if (requestedLitres <= 0) throw new IllegalArgumentException("Litres must be positive.");
        this.id = id;
        this.areaId = areaId;
        this.requestedLitres = requestedLitres;
        this.mode = mode;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.unmetLitres = requestedLitres;
    }

    public String getId() { return id; }
    public String getAreaId() { return areaId; }
    public int getRequestedLitres() { return requestedLitres; }
    public int getAllocatedLitres() { return allocatedLitres; }
    public int getUnmetLitres() { return unmetLitres; }
    public String getMode() { return mode; }
    public String getCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    public boolean isComplete() { return unmetLitres == 0; }

    public void recordAllocation(int litres) {
        int safe = Math.max(0, Math.min(litres, unmetLitres));
        allocatedLitres += safe;
        unmetLitres -= safe;
    }

    public String getStatus() {
        if (unmetLitres == 0) return "Fulfilled";
        if (allocatedLitres > 0) return "Partially fulfilled";
        return "Pending";
    }
}
