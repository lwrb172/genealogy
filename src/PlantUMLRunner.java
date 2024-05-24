import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PlantUMLRunner {
    private static String plantUMLPath;

    public static void setPlantUMLPath(String path) {
        plantUMLPath = path;
    }

    public static void generateDiagram(String umlData,String outputDirPath,String outputFileName) {

        File outputFile = new File(outputDirPath + outputFileName+".txt");
        try(FileWriter outputWriter = new FileWriter(outputFile, StandardCharsets.UTF_8)){
            outputWriter.write(umlData);
            outputWriter.close();

            String command= "java -jar " + plantUMLPath + " -charset UTF-8 " + outputFile.getPath()
                    + " -o "+outputDirPath+ " " + outputFileName;

            Process process =  Runtime.getRuntime().exec(command);
            process.waitFor();
//            outputFile.delete();
        }
        catch (InterruptedException | IOException e){e.printStackTrace();}
    }
}