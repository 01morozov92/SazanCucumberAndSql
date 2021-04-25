package testData;

import Exceptions.SazanException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrepareBody {
    private String fullPath;
    private String fileName;
    private String filePath;
    private static final String SLESH = "/";

    public PrepareBody(String path, String fileName) {
        this.fullPath = path;
        this.fileName = fileName;
    }

    public void cutPath() {
        String appPath = System.getProperty("user.dir");//текущий рабочий каталог
        StringBuilder bld = new StringBuilder();
        bld.append(appPath);
        bld.append(SLESH);
        String[] pathPart = this.fullPath.split("/");
        for (int i = 0; i < pathPart.length - 1; i++) {
            bld.append(pathPart[i]);
            bld.append(SLESH);
        }
        this.filePath = bld.append(this.fileName).toString(); //this.fileName = sql/okr/PskToPdn/insertWave.sql
        //bld = C:\Users\MI\IdeaProjects\sazan_core/src/test/resources/features/test/
    }

    public String loadBody() throws IOException, SazanException {
        cutPath();
        if (!Paths.get(this.filePath).toFile().exists()) {//если файл не найден то бросается исключение
            throw new SazanException(String.format("File not found: %s", this.filePath));
        }
        return new String(Files.readAllBytes(Paths.get(this.filePath)), StandardCharsets.UTF_8);
        //return = C:\Users\MI\IdeaProjects\sazan_core/src/test/resources/features/test/sql/okr/PskToPdn/insertWave.sql
    }

    public String getFilePath() {
        cutPath();
        return filePath;
    }
}
