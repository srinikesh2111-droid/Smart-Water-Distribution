public class Reservoir {

    private String id;
    private String name;
    private int capacity;
    private int remainingSupply;

    public Reservoir(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.remainingSupply = capacity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRemainingSupply() {
        return remainingSupply;
    }

    public int allocateWater(int litres) {

        int allocated = Math.min(litres, remainingSupply);

        remainingSupply -= allocated;

        return allocated;
    }

    public void resetDailySupply() {
        remainingSupply = capacity;
    }

    public void displayDetails() {

        System.out.println("Reservoir ID       : " + id);
        System.out.println("Reservoir Name     : " + name);
        System.out.println("Total Capacity     : " + capacity + " litres");
        System.out.println("Remaining Supply   : " + remainingSupply + " litres");
    }
}