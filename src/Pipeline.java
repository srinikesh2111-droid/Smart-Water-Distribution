public class Pipeline {

    private String id;
    private String from;
    private String to;
    private boolean working;

    public Pipeline(String id, String from, String to) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.working = true;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isWorking() {
        return working;
    }

    public void markFailed() {
        working = false;
    }

    public void repair() {
        working = true;
    }
}
