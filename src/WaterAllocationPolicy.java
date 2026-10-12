import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface WaterAllocationPolicy {
    List<DemandArea> orderAreas(Collection<DemandArea> areas, boolean priorityMode);
}

class DefaultWaterAllocationPolicy implements WaterAllocationPolicy {
    @Override
    public List<DemandArea> orderAreas(Collection<DemandArea> areas, boolean priorityMode) {
        List<DemandArea> result = new ArrayList<>(areas);
        if (priorityMode) {
            result.sort(Comparator.comparingInt(DemandArea::allocationPriority).reversed()
                    .thenComparing(DemandArea::getId, IdOrder.NATURAL));
        } else {
            result.sort(Comparator.comparing(DemandArea::getId, IdOrder.NATURAL));
        }
        return result;
    }
}
