package za.co.wits.students.road.damage;

import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import za.co.wits.students.road.damage.model.coco.CocoFormat;

import java.nio.file.Files;
import java.nio.file.Paths;

@Log
public class Runner {
    public static void main(String[] args) throws Exception {
        var cocoUtility = CocoUtility.builder().build();
        CocoFormat cocoFormat = cocoUtility.convertCocoXmlAnnotationFilesToCocoJson();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        String cocoJson = builder.create().toJson(cocoFormat);

        var outputPath = Paths.get("D:\\Masters\\2020\\maeda300\\roadDamage.json");
        Files.write(outputPath,cocoJson.getBytes());
        log.info(cocoJson);
        log.info("I am done!");
    }
}
