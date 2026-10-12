import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MaintenanceRequest {
    private final String id;
    private final String pipelineId;
    private final String description;
    private final String createdAt;
    private boolean resolved;

    public MaintenanceRequest(String id, String pipelineId, String description) {
        this(id, pipelineId, description, LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), false);
    }

    public MaintenanceRequest(String id, String pipelineId, String description, String createdAt, boolean resolved) {
        this.id = id;
        this.pipelineId = pipelineId;
        this.description = description;
        this.createdAt = createdAt == null ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : createdAt;
        this.resolved = resolved;
    }

    public String getId() { return id; }
    public String getPipelineId() { return pipelineId; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public boolean isResolved() { return resolved; }
    public void markResolved() { resolved = true; }
}
