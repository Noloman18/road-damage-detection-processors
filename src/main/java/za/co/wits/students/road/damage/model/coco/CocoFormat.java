package za.co.wits.students.road.damage.model.coco;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class CocoFormat {
    private Info info;
    @Singular
    private List<Licence> licences;
    private List<Image> images;
    private List<Annotation> annotations;
    @Singular
    private List<Category> categories;
}
