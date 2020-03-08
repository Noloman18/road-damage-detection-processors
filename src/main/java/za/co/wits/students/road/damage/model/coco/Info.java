package za.co.wits.students.road.damage.model.coco;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Info {
    private String description;
    private String url;
    private String version;
    private Integer year;
    private String contributor;
    private String dateCreated;
}
