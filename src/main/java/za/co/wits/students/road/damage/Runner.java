package za.co.wits.students.road.damage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import org.xml.sax.SAXException;
import za.co.wits.students.road.damage.model.coco.Annotation;
import za.co.wits.students.road.damage.model.coco.CocoFormat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.function.Predicate;

@Log
public class Runner {

    private final CocoUtility roadDamageDatasetConverter() {
        return CocoUtility.builder()
                .annotationDirectory("D:\\Masters\\2020\\maeda300\\annotations\\xmls")
                .imagesDirectory("D:\\Masters\\2020\\maeda300\\images")
                .description("Road Maintenance and Repair Guidebook 2013 JRA (2013) in Japan")
                .contributor("Hiroya Maeda\u0003, Yoshihide Sekimoto, Toshikazu Seto, Takehiro Kashiyama, Hiroshi Omata University of Tokyo, 4-6-1 Komaba, Tokyo, Japan")
                .github("https://github.com/sekilab/RoadDamageDetector/blob/master/LICENSE")
                .offset(1)
                .build();
    }

    private final CocoUtility kanagarooProcessor() {
        return CocoUtility.builder()
                .annotationDirectory("D:\\university\\datasets\\Kangaroo\\kangaroo\\annots")
                .imagesDirectory("D:\\university\\datasets\\Kangaroo\\kangaroo\\images")
                .description("Kangaroo dataset")
                .contributor("Huynh Ngoc Anh")
                .github("https://github.com/experiencor/kangaroo")
                .offset(1)
                .build();
    }


    public void createCoco(CocoUtility cocoUtility, String outputFilename) throws ParserConfigurationException, JAXBException, SAXException, IOException {
        CocoFormat cocoFormat = cocoUtility.convertCocoXmlAnnotationFilesToCocoJson();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        String cocoJson = builder.create().toJson(cocoFormat);

        var outputPath = Paths.get(outputFilename);
        Files.write(outputPath, cocoJson.getBytes());
        log.info(cocoJson);
        log.info("I am done!");
    }

    public void browseDataset(String imageBaseFolder, String cocoFileName, Predicate<Annotation> filter) throws Exception {
        var inputPath = Paths.get(cocoFileName);
        var cocoFormat =
                new Gson().fromJson(Files.readString(inputPath), CocoFormat.class);

        var canvas = new JPanel() {
            BufferedImage img = null;

            void setImg(BufferedImage img) {
                this.img = img;
                this.setSize(this.img.getWidth(), this.img.getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, this);
            }
        };

        canvas.setImg(getImage(imageBaseFolder, cocoFormat, filter));

        var button = new JButton();
        button.setText("Next image");
        button.setSize(600, 30);
        button.addActionListener(event -> {
            canvas.setImg(getImage(imageBaseFolder, cocoFormat, filter));
            canvas.repaint();
            button.repaint();
        });

        var frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Show random image");
        frame.setSize(600, 630);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(button, BorderLayout.SOUTH);
    }

    private BufferedImage getImage(String imageBase, CocoFormat cocoFormat, Predicate<Annotation> filter) {
        Collections.shuffle(cocoFormat.getAnnotations());
        var annotation = cocoFormat.getAnnotations().stream().filter(filter).findFirst().get();
        var imageId = annotation.getImageId();

        var imgMetaData =
                cocoFormat.getImages().stream().filter(img -> img.getId().equals(imageId)).findFirst().get();

        var image = (BufferedImage) null;
        var randomUtil = new Random();
        var colors = Arrays.asList(
                Color.CYAN, Color.YELLOW, Color.RED, Color.MAGENTA, Color.ORANGE, Color.BLUE
        );

        int imageWidth = 0, imageHeight = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0, x = 0, y = 0;

        try {
            image = ImageIO.read(new File(String.format("%s/%s", imageBase, imgMetaData.getFileName())));

            for (var boundingBox : annotation.getBoundingBox()) {
                x1 = boundingBox.get(0);
                y1 = boundingBox.get(1);

                x2 = boundingBox.get(2);
                y2 = boundingBox.get(3);

                var color = colors.get(randomUtil.nextInt(colors.size())).getRGB();

                x = 0;
                y = 0;

                for (x = x1; x <= x2; x++) {
                    image.setRGB(x, y1, color);
                    image.setRGB(x, y2, color);
                }

                for (y = y1; y <= y2; y++) {
                    image.setRGB(x1, y, color);
                    image.setRGB(x2, y, color);
                }
            }
        } catch (Exception e) {
            System.out.printf("%d %d %d %d IMG[%d,%d] Guilty[%d,%d]%n", x1, x2, y1, y2, imageWidth, imageHeight, x, y);
            e.printStackTrace();
        }

        return image;
    }

    public static void main(String[] args) throws Exception {
        var me = new Runner();
        //me.createCoco(me.roadDamageDatasetConverter(), "D:\\Masters\\2020\\maeda300\\roadDamage.json");
//        me.browseDataset(
//                "D:\\Masters\\2020\\maeda300\\images",
//                "D:\\Masters\\2020\\maeda300\\roadDamage.json",
//                Annotation::containsCrowd);
        //me.createCoco(me.kanagarooProcessor(), "D:\\university\\datasets\\Kangaroo\\kangaroo\\kangaroo.json");
//        me.browseDataset(
//                "D:\\university\\datasets\\Kangaroo\\kangaroo\\images",
//                "D:\\\\university\\\\datasets\\\\Kangaroo\\\\kangaroo\\\\kangaroo.json",
//                Annotation::containsCrowd);
    }
}
