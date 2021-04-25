package helpers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBAliases {

    private static volatile DBAliases instance;
    private static final String RESOURCE_PATH = StringUtils.concatPath("src", "test", "resources");
    private static final String ALIAS_FILE = "db.aliases";
    private static final String ALIAS_FILE_PATH = StringUtils.concatPathToFile(RESOURCE_PATH, ALIAS_FILE);
    private static final Logger log = LoggerFactory.getLogger(DBAliases.class);
    private static Map<String, DBAliasesNames> alias = new HashMap<>();

    public static DBAliases getInstance() {
        if (instance == null)
            synchronized (DBAliases.class) {
                instance = new DBAliases();
            }
        return instance;
    }

    public void loadApplicationPropertiesForSegment(String path) throws IOException {

        String propertyPath;

        if (System.getProperty(ALIAS_FILE) != null && System.getProperty(ALIAS_FILE).contains("/")) {
            propertyPath = System.getProperty(ALIAS_FILE);
        } else {
            String rootPath = System.getProperty("user.dir");
            String propPath = StringUtils.concatPath(rootPath, RESOURCE_PATH);
            propertyPath = StringUtils.concatPath(propPath, path);
        }
        StringReader aliasBody = new StringReader(new String(Files.readAllBytes(Paths.get(propertyPath)), StandardCharsets.UTF_8));
        CSVParser parser = CSVFormat.DEFAULT.withDelimiter(';').parse(aliasBody);
        List<CSVRecord> list = parser.getRecords();
        for (CSVRecord line : list) {
            alias.put(line.get(0), new DBAliasesNames(line.get(1),line.get(2),line.get(3),line.get(4),line.get(5),line.get(6),line.get(7),line.get(8),line.get(9)));
        }




        log.info("'{}' loaded successfully!", ALIAS_FILE_PATH);
    }

    public DBAliasesNames getValue(String value){
        return alias.get(value);
    }
}
