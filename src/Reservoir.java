public class Reservoir {

    private String id;
    private String name;
    private int dailySupply;
    private int remainingSupply;

    public Reservoir(String id, String name, int dailySupply) {
        this.id = id;
        this.name = name;
        this.dailySupply = dailySupply;
        this.remainingSupply = dailySupply;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRemainingSupply() {
        return remainingSupply;
    }

    public boolean allocateWater(int litres) {
        if (litres <= remainingSupply) {
            remainingSupply -= litres;
            return true;
        }
        return false;
    }

    public void resetDailySupply() {
        remainingSupply = dailySupply;
    }
}
