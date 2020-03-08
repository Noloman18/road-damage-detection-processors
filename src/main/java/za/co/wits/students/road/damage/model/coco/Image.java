package za.co.wits.students.road.damage.model.coco;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Builder(builderMethodName = "hiddenBuilder")
@Data
public class Image {
    private static final AtomicInteger instanceCounter = new AtomicInteger();
    private Integer licence;
    @SerializedName("file_name")
    private String fileName;
    private Integer height;
    private Integer width;
    @Builder.Default
    private final Integer id = instanceCounter.incrementAndGet();

    public static ImageBuilder builder(String fileName, Integer width, Integer height) {
        return hiddenBuilder().fileName(fileName).width(width).height(height);
    }
}
