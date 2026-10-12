public class Reservoir {
    private final String id;
    private final String name;
    private final int capacity;
    private int remainingSupply;

    public Reservoir(String id, String name, int capacity) {
        if (id == null || name == null || capacity < 0) {
            throw new IllegalArgumentException("Invalid reservoir data.");
        }
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.remainingSupply = capacity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getRemainingSupply() { return remainingSupply; }

    public int allocateWater(int litres) {
        if (litres <= 0) return 0;
        int allocated = Math.min(litres, remainingSupply);
        remainingSupply -= allocated;
        return allocated;
    }

    public void resetDailySupply() { remainingSupply = capacity; }

    public void setRemainingSupply(int litres) {
        remainingSupply = Math.max(0, Math.min(capacity, litres));
    }

    public void addSupply(int litres) {
        if (litres > 0) setRemainingSupply(remainingSupply + litres);
    }

    public void displayDetails() {
        System.out.println("Reservoir ID       : " + id);
        System.out.println("Reservoir Name     : " + name);
        System.out.println("Total Capacity     : " + capacity + " litres");
        System.out.println("Remaining Supply   : " + remainingSupply + " litres");
    }
}
