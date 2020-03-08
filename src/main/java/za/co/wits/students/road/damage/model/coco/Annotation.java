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
    private Integer isCrowd;
    @SerializedName("image_id")
    private Integer imageId;
    @SerializedName("category_id")
    private Integer categoryId;
    @Singular("boundingBox")
    @SerializedName("bbox")
    private List<Integer> boundingBox;
    @Builder.Default
    private final Integer id = instanceCounter.incrementAndGet();
}
