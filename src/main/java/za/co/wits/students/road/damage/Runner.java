package za.co.wits.students.road.damage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import org.xml.sax.SAXException;
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
import java.util.Collections;
import java.util.Random;

@Log
public class Runner {

    public void createCoco() throws ParserConfigurationException, JAXBException, SAXException, IOException {
        var cocoUtility = CocoUtility.builder().build();
        CocoFormat cocoFormat = cocoUtility.convertCocoXmlAnnotationFilesToCocoJson();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        String cocoJson = builder.create().toJson(cocoFormat);

        var outputPath = Paths.get("D:\\Masters\\2020\\maeda300\\roadDamage.json");
        Files.write(outputPath, cocoJson.getBytes());
        log.info(cocoJson);
        log.info("I am done!");
    }

    public void displayImage() throws Exception {
        var inputPath = Paths.get("D:\\Masters\\2020\\maeda300\\roadDamage.json");
        var cocoFormat =
                new Gson().fromJson(Files.readString(inputPath), CocoFormat.class);

        var canvas = new JPanel() {
            BufferedImage img = (BufferedImage) null;

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

        canvas.setImg(getImage(cocoFormat));

        var button = new JButton();
        button.setText("Next image");
        button.setSize(600, 30);
        button.addActionListener(event -> {
            canvas.setImg(getImage(cocoFormat));
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

    private BufferedImage getImage(CocoFormat cocoFormat) {
        Collections.shuffle(cocoFormat.getAnnotations());
        var annotation = cocoFormat.getAnnotations().get(0);
        var imageId = annotation.getImageId();

        var imgMetaData =
                cocoFormat.getImages().stream().filter(img -> img.getId().equals(imageId)).findFirst().get();

        var boundingBox = annotation.getBoundingBox();

        var x1 = boundingBox.get(0);
        var y1 = boundingBox.get(1);

        var x2 = boundingBox.get(2);
        var y2 = boundingBox.get(3);


        var cyan = Color.CYAN.getRGB();

        var image = (BufferedImage) null;
        int imageWidth = 0;
        int imageHeight = 0;

        int x = 0;
        int y = 0;

        try {
            image = ImageIO.read(new File(String.format("D:/Masters/2020/maeda300/images/%s", imgMetaData.getFileName())));

            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            for (x = x1; x <= x2; x++) {
                image.setRGB(x, y1, cyan);
                image.setRGB(x, y2, cyan);
            }

            for (y=y1;y<=y2;y++) {
                image.setRGB(x1, y, cyan);
                image.setRGB(x2, y, cyan);
            }

        } catch (Exception e) {
            System.out.printf("%d %d %d %d IMG[%d,%d] Guilty[%d,%d]%n",x1,x2,y1,y2,imageWidth,imageHeight,x,y);
            e.printStackTrace();
        }

        return image;
    }

    public static void main(String[] args) throws Exception {
        var me = new Runner();
        me.displayImage();
        //me.createCoco();
    }
}
