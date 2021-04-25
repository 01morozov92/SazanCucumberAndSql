package helpers;

import Exceptions.SazanException;
import io.cucumber.datatable.dependency.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configurier {
    private static volatile Configurier instance;
    private static final Logger log = LoggerFactory.getLogger(Configurier.class);
    private static final String RESOURCE_PATH = StringUtils.concatPath("src", "test", "resources");
    private static final String APP_FILE_NAME = "application.properties";
    private static final String APP_FILE_PATH = StringUtils.concatPathToFile(RESOURCE_PATH, APP_FILE_NAME);
    private static final String ENV_FILE_NAME = "conf.json";
    private static final String ENV_FILE_PATH = StringUtils.concatPathToFile(RESOURCE_PATH, ENV_FILE_NAME);
    private static final String DB_USER = "db.username";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_HOST = "db.host";
    private static final String DB_PORT = "db.port";
    private static final String DB_SID = "db.sid";
    private static final String DB_ENCODING = "db.encoding";
    private static final String DB_PROTOCOL = "db.protocol";
    private static final String ISO_HOST = "iso.host";
    private static final String ISO_PORT = "iso.port";
    private static final String ENVIROMENT = "env";
    private static final String COUNT = "count";
    private static final String WAVE = "wave";
    private static final String DB_DRIVER_TYPE = "db.driver.type";
    private final String enviroment = System.getProperty(ENVIROMENT);
    private final String count = System.getProperty(COUNT);
    private final String wave = System.getProperty(WAVE);

    public String getEnviroment() {
        return enviroment;
    }

    public String getCount() {
        return count;
    }

    public String getWave() {
        return wave;
    }

    private Configurier() {
    }

    public static Configurier getInstance() {
        if (instance == null)
            synchronized (Configurier.class) {
                instance = new Configurier();
                try {
                    instance.loadApplicationPropertiesForSegment();
                } catch (SazanException e) {
                    log.error(e.getMessage());
                }
            }
        return instance;
    }

    public Map<String, String> getApplicationProperties() {
        return applicationProperties;
    }

    private Map<String, String> applicationProperties = new HashMap<>();
    private String postfix = "";

    /**
     * @return isLoaded признак, загружены ли проперти
     * @throws SazanException something wrong
     */
    public boolean loadApplicationPropertiesForSegment() throws SazanException {
        String propertyPath;
        String envPropertyPath;
        if (System.getProperty(APP_FILE_NAME) != null) {
            propertyPath = System.getProperty(APP_FILE_NAME);
            envPropertyPath = System.getProperty(ENV_FILE_NAME);
        } else {
            String rootPath = System.getProperty("user.dir");
            propertyPath = StringUtils.concatPath(rootPath, APP_FILE_PATH);
            envPropertyPath = StringUtils.concatPath(rootPath, ENV_FILE_PATH);
        }
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(propertyPath)) {
            properties.load(fileInputStream);
        } catch (Exception e) {
            log.error("Can't load properties file: " + propertyPath, e);
            throw new SazanException(e);
        }
        if (properties.isEmpty()) {
            return false;
        }

        Map<String, String> envProperty = new HashMap<>();

        if (System.getProperty(ENVIROMENT) != null) {
            postfix = "." + System.getProperty(ENVIROMENT);
            try (FileInputStream fileInputStream = new FileInputStream(envPropertyPath)) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, HashMap<String, String>> confProp = mapper.readValue(fileInputStream, Map.class);
                envProperty.putAll(confProp.get(System.getProperty(ENVIROMENT).toUpperCase()));
            } catch (Exception e) {
                log.error("Can't load env properties file: " + envPropertyPath, e);
                throw new SazanException(e);
            }
        }

        log.debug("'{}' loaded successfully!", APP_FILE_PATH);

        for (String key : properties.stringPropertyNames()) {
            if (properties.getProperty(key).contains("conf."+ENVIROMENT)) {
                String envKey = properties.getProperty(key).split("conf\\."+ENVIROMENT+"\\.")[1];
                applicationProperties.put(key, envProperty.get(envKey));
            } else if (key.contains(postfix)) {
                applicationProperties.put(key.replace(postfix, ""), properties.getProperty(key));
            } else if (!applicationProperties.containsKey(key.replace(postfix, ""))) {
                applicationProperties.put(key, properties.getProperty(key));
            }
        }
        if (System.getProperty(DB_PORT) != null) {
            applicationProperties.put(DB_PORT, System.getProperty(DB_PORT));
        }
        if (System.getProperty("profile.host") != null) {
            applicationProperties.put(DB_HOST, System.getProperty(DB_HOST));
            applicationProperties.put("ssh.host", System.getProperty(DB_HOST));
        }
        if (System.getProperty("profile.iso.port") != null) {
            applicationProperties.put(ISO_PORT, System.getProperty(ISO_PORT));
        }
        if (System.getProperty("profile.iso.host") != null) {
            applicationProperties.put(ISO_HOST, System.getProperty(ISO_HOST));
        }


//        applicationProperties.put("db.url", dbUrl());

        return true;
    }

    public String getAppProp(String propertyKey) {
        String valueString = applicationProperties != null ? applicationProperties.get(propertyKey) : null;
        if (valueString == null) {
            log.error("Can't get value for Application key '{}'", propertyKey);
        }
        return valueString;
    }


}
