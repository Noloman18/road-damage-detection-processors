package za.co.wits.students.road.damage.model.coco;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Licence {
    private String url;
    private Integer id;
    private String name;
}
