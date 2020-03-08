package za.co.wits.students.road.damage.model.coco;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "annotation")
public class PascalAnnotation {

    private String folder;
    private String filename;
    private Size size;
    private MetaData object;
    @Data
    public static class Size {
        private Integer width;
        private Integer height;
        private Integer depth;
    }
    @Data
    public static class MetaData {
        private String name;
        private String pose;
        private BoundingBox bndbox;
    }

    @Data
    public static class BoundingBox {
        private Integer xmin;
        private Integer xmax;
        private Integer ymin;
        private Integer ymax;
    }
}
