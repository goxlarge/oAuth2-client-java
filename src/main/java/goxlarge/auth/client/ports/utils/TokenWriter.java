package goxlarge.auth.client.ports.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TokenWriter {
    public static void tokenToTempFile(String token, String filename) throws IOException {

        writeFileJava11(Path.of("/tmp",filename), token);
    }

    private static void writeFileJava11(Path path, String content)
            throws IOException {

        // default utf_8
        // file does not exists, create and write it
        // if the file exists, override the content
        Files.writeString(path, content);

        // Append mode
        // Files.writeString(path, content,
        //	StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static String readTokenFromTmp(String apiName){
        String fileName = "/tmp/"+ apiName;
        String results = "";
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName),
                    StandardCharsets.UTF_8);
            results = lines.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("get audi token from /tmp/ " + apiName +":" +results);

        return results;
    }
}
