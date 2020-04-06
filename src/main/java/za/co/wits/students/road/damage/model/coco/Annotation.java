package za.co.wits.students.road.damage.model.coco;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
public class Annotation {
    private static final AtomicInteger instanceCounter = new AtomicInteger();
    @SerializedName("is_crowd")
    private List<Integer> isCrowd;
    @SerializedName("image_id")
    private Integer imageId;
    @Singular("categoryId")
    @SerializedName("category_id")
    private List<Integer> categoryId;
    @Singular("area")
    private List<Integer> area;
    @Singular("boundingBox")
    @SerializedName("bbox")
    private List<List<Integer>> boundingBox;
    @Builder.Default
    private final Integer id = instanceCounter.incrementAndGet();
}
