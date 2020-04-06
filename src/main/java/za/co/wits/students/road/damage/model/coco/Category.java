package za.co.wits.students.road.damage.model.coco;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder(builderMethodName = "hiddenBuilder")
public class Category {
    private static final AtomicInteger instanceCounter = new AtomicInteger();

    @SerializedName("supercategory")
    @Builder.Default
    private final String superCategory = "Road Damage";
    @Builder.Default
    private final Integer id = instanceCounter.incrementAndGet();
    private String name;

    public static CategoryBuilder builder(String name) {
        return hiddenBuilder().name(name);
    }
}
