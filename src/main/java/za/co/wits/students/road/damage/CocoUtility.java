package za.co.wits.students.road.damage;

import lombok.Builder;
import lombok.extern.java.Log;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import za.co.wits.students.road.damage.model.coco.*;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@Log
public class CocoUtility {
    public CocoFormat convertCocoXmlAnnotationFilesToCocoJson() throws IOException, ParserConfigurationException, SAXException, JAXBException {
        var xmlAnnotationBaseFolder = Paths.get("D:/Masters/2020/maeda300/annotations/xmls");
        var annotationPaths = Files.list(xmlAnnotationBaseFolder).collect(Collectors.toList());
        var pascalAnnotationList = getAnnotations(annotationPaths);

        var licenceList = getLicence();
        var info = getInformation();
        var categoryList = new ArrayList<Category>(10);
        var imagesList = new ArrayList<Image>(pascalAnnotationList.size());
        var cocoAnnotationsList = new ArrayList<Annotation>(pascalAnnotationList.size());

        for (var pascalAnnotation : pascalAnnotationList) {
            if (pascalAnnotation.getObject() == null)
                continue;//skip, no road damage...

            var boundingBox = pascalAnnotation.getObject().getBndbox();
            var width = boundingBox.getXmax()-boundingBox.getXmin() -1;
            var height = boundingBox.getYmax()-boundingBox.getYmin() -1;

            var totalWidth = width+boundingBox.getXmin();
            var totalHeight = height+boundingBox.getYmin();

            if (width<1 || height<1 || totalWidth>pascalAnnotation.getSize().getWidth() || totalHeight> pascalAnnotation.getSize().getHeight())
                continue;

            var image =
                    Image.builder(
                            pascalAnnotation.getFilename(),
                            pascalAnnotation.getSize().getWidth(),
                            pascalAnnotation.getSize().getHeight())
                            .licence(0)
                            .build();

            imagesList.add(image);

            var optionalCategory = categoryList.stream().filter(ithCat -> Objects.equals(pascalAnnotation.getObject().getName(), ithCat.getName())).findFirst();
            var category = (Category) null;
            if (optionalCategory.isPresent())
                category = optionalCategory.get();
            else {
                category = Category.builder(pascalAnnotation.getObject().getName()).build();
                categoryList.add(category);
            }

            var annotation = Annotation.builder()
                    .boundingBox(boundingBox.getXmin())
                    .boundingBox(boundingBox.getYmin())
                    .boundingBox(boundingBox.getXmax()-1)
                    .boundingBox(boundingBox.getYmax()-1)
                    .area(width*height)
                    .imageId(image.getId())
                    .categoryId(category.getId())
                    .isCrowd(0)
                    .build();
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

    private List<PascalAnnotation> getAnnotations(List<Path> annotationPaths) {
        List<PascalAnnotation> annotations = new ArrayList<>();

        annotationPaths.parallelStream().forEach(path -> {
            try {
                String xmlString =
                        new String(Files.readAllBytes(path));

                JAXBContext jaxbContext = JAXBContext.newInstance(PascalAnnotation.class);
                var unmarshaller = jaxbContext.createUnmarshaller();
                var pascalAnnotation = (PascalAnnotation) unmarshaller.unmarshal(new InputSource(new StringReader(xmlString)));

                var baseDirectory = path.getParent().getParent().getParent();

                var imageName = baseDirectory.resolve(pascalAnnotation.getFolder()).resolve(pascalAnnotation.getFilename());

                if (Files.exists(imageName)) {
                    var image = ImageIO.read(new File(imageName.toAbsolutePath().toString()));
                    pascalAnnotation.getSize().setWidth(image.getWidth());
                    pascalAnnotation.getSize().setHeight(image.getHeight());
                    synchronized (annotations) {
                        annotations.add(pascalAnnotation);
                    }
                }

                log.info(String.format("Processed [%s] %n", path));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return annotations;
    }

    private Info getInformation() {
        return Info.builder()
                .contributor("Road Maintenance and Repair Guidebook 2013 JRA (2013) in Japan")
                .year(2013)
                .contributor("Hiroya Maeda\u0003, Yoshihide Sekimoto, Toshikazu Seto, Takehiro Kashiyama, Hiroshi Omata University of Tokyo, 4-6-1 Komaba, Tokyo, Japan")
                .build();
    }

    private Licence getLicence() {
        return Licence.builder()
                .id(0)
                .name("MIT Licence")
                .url("https://github.com/sekilab/RoadDamageDetector/blob/master/LICENSE")
                .build();
    }
}
