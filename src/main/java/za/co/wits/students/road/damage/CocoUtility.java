package za.co.wits.students.road.damage;

import lombok.Builder;
import lombok.extern.java.Log;
import org.xml.sax.InputSource;
import za.co.wits.students.road.damage.model.coco.Image;
import za.co.wits.students.road.damage.model.coco.*;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Log
public class CocoUtility {
    private String annotationDirectory;
    private String imagesDirectory;
    private String contributor;
    private String description;
    private String github;
    private Integer offset;

    public CocoFormat convertPascalFormatToCocoFormat(PascalType pascalType) throws IOException {
        var annotationBaseFolder = Paths.get(annotationDirectory);
        var annotationPaths = Files.list(annotationBaseFolder).collect(Collectors.toList());
        return convertToCocoFormat(pascalType==PascalType.PASCAL?getPascalAnnotations(annotationPaths):getPascalVocAnnotations(annotationPaths));
    }

    private CocoFormat convertToCocoFormat(List<PascalAnnotation> pascalAnnotationList) {
        var licenceList = getLicence();
        var info = getInformation();
        var categoryList = new ArrayList<Category>(10);
        var imagesList = new ArrayList<Image>(pascalAnnotationList.size());
        var cocoAnnotationsList = new ArrayList<Annotation>(pascalAnnotationList.size());

        target:
        for (var pascalAnnotation : pascalAnnotationList) {
            if (pascalAnnotation.getObject() == null)
                continue;//skip, no road damage...

            var annotationBuilder = Annotation.builder();

            var zeros = new ArrayList<Integer>();
            for (var objectInstance : pascalAnnotation.getObject()) {
                var boundingBox = objectInstance.getBndbox();
                var width = boundingBox.getXmax() - boundingBox.getXmin() - offset;
                var height = boundingBox.getYmax() - boundingBox.getYmin() - offset;

                var totalWidth = width + boundingBox.getXmin();
                var totalHeight = height + boundingBox.getYmin();

                if (width < offset || height < offset || totalWidth > pascalAnnotation.getSize().getWidth() || totalHeight > pascalAnnotation.getSize().getHeight())
                    continue target;

                var optionalCategory = categoryList.stream().filter(ithCat -> Objects.equals(objectInstance.getName(), ithCat.getName())).findFirst();
                var category = (Category) null;
                if (optionalCategory.isPresent())
                    category = optionalCategory.get();
                else {
                    category = Category.builder(objectInstance.getName()).build();
                    categoryList.add(category);
                }

                annotationBuilder.boundingBox(
                        Arrays.asList(
                                boundingBox.getXmin(),
                                boundingBox.getYmin(),
                                boundingBox.getXmax() - offset,
                                boundingBox.getYmax() - offset));
                annotationBuilder.categoryId(category.getId());
                annotationBuilder.area(width * height);
                zeros.add(0);
            }

            var image =
                    Image.builder(
                            pascalAnnotation.getFilename(),
                            pascalAnnotation.getSize().getWidth(),
                            pascalAnnotation.getSize().getHeight())
                            .licence(0)
                            .build();

            imagesList.add(image);

            var annotation = annotationBuilder.isCrowd(zeros).imageId(image.getId()).build();

            for (int i = 0; i < annotation.getBoundingBox().size() - 1; i++) {
                if (annotation.getIsCrowd().get(i) == 0) {
                    var ithList = annotation.getBoundingBox().get(i);
                    var xmin = ithList.get(0);
                    var xmax = ithList.get(1);
                    var ymin = ithList.get(2);
                    var ymax = ithList.get(3);

                    Integer ithCategory = annotation.getCategoryId().get(i);

                    for (int j = i + 1; j < annotation.getBoundingBox().size(); j++) {
                        if (ithCategory.equals(annotation.getCategoryId().get(j))) {
                            var jthList = annotation.getBoundingBox().get(j);
                            var listOfPoint = new ArrayList<Point>();
                            listOfPoint.add(new Point(jthList.get(0), jthList.get(1)));
                            listOfPoint.add(new Point(jthList.get(0), jthList.get(3)));
                            listOfPoint.add(new Point(jthList.get(2), jthList.get(1)));
                            listOfPoint.add(new Point(jthList.get(2), jthList.get(3)));

                            for (var point : listOfPoint) {
                                if (point.x > -xmin && point.x < xmax && point.y > ymin && point.y < ymax) {
                                    annotation.getIsCrowd().set(i, 1);
                                    annotation.getIsCrowd().set(j, 1);
                                }
                            }
                        }
                    }
                }
            }

            cocoAnnotationsList.add(annotation);
        }

        var cocoFormat = CocoFormat.builder()
                .licence(licenceList)
                .info(info)
                .images(imagesList)
                .categories(categoryList)
                .annotations(cocoAnnotationsList)
                .build();
        return cocoFormat;
    }

    private List<PascalAnnotation> getPascalVocAnnotations(List<Path> annotationPaths) {
        List<PascalAnnotation> annotations = new ArrayList<>();

        annotationPaths.parallelStream().forEach(path -> {
            try {
                String xmlString =
                        new String(Files.readAllBytes(path));

                JAXBContext jaxbContext = JAXBContext.newInstance(PascalAnnotation.class);
                var unmarshaller = jaxbContext.createUnmarshaller();
                var pascalAnnotation = (PascalAnnotation) unmarshaller.unmarshal(new InputSource(new StringReader(xmlString)));

                validateImageExists(annotations, path, pascalAnnotation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return annotations;
    }

    private List<PascalAnnotation> getPascalAnnotations(List<Path> annotationPaths) {
        List<PascalAnnotation> annotations = new ArrayList<>();

        annotationPaths.parallelStream().forEach(path -> {
            var pascalAnnotation = new PascalAnnotation();
            pascalAnnotation.setObject(new ArrayList<>());

            try {
                try (var reader = Files.newBufferedReader(path)) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("#"))
                            continue;

                        if (line.startsWith("Image filename"))
                            pascalAnnotation.setFilename(line.substring(line.lastIndexOf('/') + 1, line.length() - 1));
                        else if (line.startsWith("Image size")) {
                            var dimensions = line.split(":")[1].split("x");
                            var size = new PascalAnnotation.Size();
                            size.setWidth(Integer.valueOf(dimensions[0].trim()));
                            size.setHeight(Integer.valueOf(dimensions[1].trim()));
                            pascalAnnotation.setSize(size);
                        } else if (line.startsWith("Bounding box for object")) {
                            var coordinates = line.split(":")[1].replaceAll("[ ()]", "").replace("-", ",").split(",");
                            var object = new PascalAnnotation.MetaData();
                            object.setName(line.split("\\\"")[1]);
                            object.setBndbox(new PascalAnnotation.BoundingBox());
                            object.getBndbox().setXmin(Integer.valueOf(coordinates[0]));
                            object.getBndbox().setYmin(Integer.valueOf(coordinates[1]));
                            object.getBndbox().setXmax(Integer.valueOf(coordinates[2]));
                            object.getBndbox().setYmax(Integer.valueOf(coordinates[3]));
                            pascalAnnotation.getObject().add(object);
                        }
                    }
                }

                validateImageExists(annotations, path, pascalAnnotation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return annotations;
    }

    private void validateImageExists(List<PascalAnnotation> annotations, Path path, PascalAnnotation pascalAnnotation) throws IOException {
        var baseDirectory = Paths.get(imagesDirectory);

        var imageName = baseDirectory.resolve(pascalAnnotation.getFilename());

        if (Files.exists(imageName)) {
            var image = ImageIO.read(new File(imageName.toAbsolutePath().toString()));
            pascalAnnotation.getSize().setWidth(image.getWidth());
            pascalAnnotation.getSize().setHeight(image.getHeight());
            synchronized (annotations) {
                annotations.add(pascalAnnotation);
            }
        }

        log.info(String.format("Processed [%s] %n", path));
    }

    private Info getInformation() {
        return Info.builder()
                .description(description)
                .year(2013)
                .contributor(contributor)
                .build();
    }

    private Licence getLicence() {
        return Licence.builder()
                .id(0)
                .name("MIT Licence")
                .url(github)
                .build();
    }

    public static enum PascalType {
        PASCAL_VOC, PASCAL
    }
}
