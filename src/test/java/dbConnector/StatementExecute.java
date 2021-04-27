package dbConnector;

import Exceptions.CustomException;
import helpers.Configurier;
import helpers.DBAliases;
import helpers.DBAliasesNames;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementExecute {

    private static final String DB_HOST = "db.host";
    private static final String DB_PORT = "db.port";
    private static final String DB_SID = "db.sid";
    private static final String DB_ENCODING = "db.encoding";
    private static final String DB_PROTOCOL = "db.protocol";
    private static final String DB_DRIVER_TYPE = "db.driver.type";
    private static final Logger log = LoggerFactory.getLogger(StatementExecute.class);

    public StatementExecute() {
    }

    private static String dbUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String,String> applicationProperties = Configurier.getInstance().getApplicationProperties();
        if(applicationProperties.get(DB_DRIVER_TYPE).equalsIgnoreCase("Sanchez")) {
            stringBuilder.append("protocol=");
            stringBuilder.append(applicationProperties.get(DB_PROTOCOL));
            stringBuilder.append("/database=");
            stringBuilder.append(applicationProperties.get(DB_HOST));
            stringBuilder.append(":");
            stringBuilder.append(applicationProperties.get(DB_PORT));
            stringBuilder.append(":");
            stringBuilder.append(applicationProperties.get(DB_SID));
            stringBuilder.append("/locale=US:ENGLISH/timeOut=2/transType=MTM/rowPrefetch=30/signOnType=1/processMRPC=0/fileEncoding=");
            stringBuilder.append(applicationProperties.get(DB_ENCODING));
        }else if(applicationProperties.get(DB_DRIVER_TYPE).equalsIgnoreCase("jdbc")){
            stringBuilder.append(applicationProperties.get(DB_PROTOCOL));
//            stringBuilder.append(":thin:@");
            stringBuilder.append("://");
            stringBuilder.append(applicationProperties.get(DB_HOST));
            stringBuilder.append(":");
            stringBuilder.append(applicationProperties.get(DB_PORT));
            stringBuilder.append("/");
            stringBuilder.append(applicationProperties.get(DB_SID));
        } else if (applicationProperties.get(DB_DRIVER_TYPE).equalsIgnoreCase("postgresjdbc")) {
            stringBuilder.append(applicationProperties.get(DB_PROTOCOL));
            stringBuilder.append("://");
            stringBuilder.append(applicationProperties.get(DB_HOST));
            stringBuilder.append(":");
            stringBuilder.append(applicationProperties.get(DB_PORT));
            stringBuilder.append("/");
            stringBuilder.append(applicationProperties.get(DB_SID));
        } else if(applicationProperties.get(DB_DRIVER_TYPE).equalsIgnoreCase("mssqljdbc")){
            stringBuilder.append(applicationProperties.get(DB_PROTOCOL));
            stringBuilder.append("://");
            stringBuilder.append(applicationProperties.get(DB_HOST));
            stringBuilder.append(":");
            stringBuilder.append(applicationProperties.get(DB_PORT));
            stringBuilder.append(";");
            stringBuilder.append("database=");
            stringBuilder.append(applicationProperties.get(DB_SID));
            stringBuilder.append(";");
            stringBuilder.append("user=");
            stringBuilder.append(applicationProperties.get("db.username"));
            stringBuilder.append(";");
            stringBuilder.append("password=");
            stringBuilder.append(applicationProperties.get("db.password"));
        }
        return stringBuilder.toString();
    }

    public Map<String, List<String>> executeSQLQueryNew(Map<String, String> property, String body, String dbAlias) throws SQLException, CustomException, IOException {
        String response = "";
        DBAliases.getInstance().loadApplicationPropertiesForSegment(Configurier.getInstance().getAppProp("db.aliases"));
        DBAliasesNames dbConnectData = DBAliases.getInstance().getValue(dbAlias);
        property.put(DB_DRIVER_TYPE,dbConnectData.getDriverType());//jdbc
        property.put("db.driver.class.name",dbConnectData.getDriverClassName());//oracle.jdbc.OracleDriver
        property.put("db.username",dbConnectData.getUsername());//RKK_DB_RELEASE
        property.put("db.password",dbConnectData.getPassword());//RKK_DB_RELEASE_123
        property.put(DB_HOST,dbConnectData.getHost());//10.128.0.26
        property.put(DB_PORT, dbConnectData.getPort());//1521
        property.put(DB_SID, dbConnectData.getSid());//ORACLE
        property.put(DB_PROTOCOL,dbConnectData.getProtocol());//jdbc:oracle
        property.put(DB_ENCODING,dbConnectData.getEncoding());//UTF16
        return doSelect(property, body);//body = SELECT max(wave_id) FROM WAVE
    }

    private Map<String, List<String>> doSelect(Map<String, String> property, String body) throws SQLException, CustomException {
        log.trace("Try get connection");
        Connection connection = getConnection(property);
        log.trace("Try create statement");
        Statement statement = connection.createStatement();
        log.trace("Try execute select");
        final Map<String, List<String>> result = executeSelect(body, false, statement);
        connection.close();
        return result;
    }

    private Map<String, List<String>> executeSelect(final String sqlQuery, final boolean firstOnly, Statement statement) throws CustomException {
        //Пришлось разделить вызовы для санчеза и jdbc т.к. ojdbc не поддерживает result.first()
         if (Configurier.getInstance().getAppProp("db.driver.type").contains("jdbc")) {
            return executeSelectJDBC(sqlQuery, firstOnly, statement);
        } else {
            throw new CustomException("No driver type found");
        }
    }

    private Map<String, List<String>> executeSelectJDBC(final String sqlQuery, final boolean firstOnly, Statement statement) throws CustomException {
        final Map<String, List<String>> resultMap = new HashMap<>();
        try (ResultSet result = statement.executeQuery(sqlQuery)) {
            final ResultSetMetaData md = result.getMetaData();
            final int colCount = md.getColumnCount();

            if (colCount>0){
                for (int i = 1; i <= colCount; i++) {
                    resultMap.put(md.getColumnName(i), new ArrayList<>());
                }
            }

            if (!firstOnly) {
                while (result.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        resultMap.get(md.getColumnName(i)).add(result.getString(i));
                    }
                }
            } else {
                result.next();
                for (int i = 1; i <= colCount; i++) {
                    resultMap.get(md.getColumnName(i)).add(result.getString(i));
                }
            }
        } catch (final SQLException cause) {
            log.error("Error :: {}", cause);
            throw new CustomException(String.format("Невозможно выполнить запрос. Текст запроса:'%s'.", sqlQuery), cause);
        }
        return resultMap;
    }

    private static Connection getConnection(Map<String, String> property) throws SQLException {
        String dburl = dbUrl();
        Connection connection;
        log.debug("Try connecting to DB");
        BasicDataSource dataSource = new BasicDataSource();
        log.debug("driver class {}", property.get("db.driver.class.name"));
        dataSource.setDriverClassName(property.get("db.driver.class.name"));
        log.debug("DB connection URL {}", dburl);
        dataSource.setUrl(dburl);
        dataSource.setUsername(property.get("db.username"));
        dataSource.setPassword(property.get("db.password"));
        connection = dataSource.getConnection();
        return connection;
    }

    public void executeUpdateOrInsertSQLQuery(Map<String, String> property, String body,String dbAlias) throws IOException, SQLException {
        String response = "";
        DBAliases.getInstance().loadApplicationPropertiesForSegment(Configurier.getInstance().getAppProp("db.aliases"));
        DBAliasesNames dbConnectData = DBAliases.getInstance().getValue(dbAlias);
        property.put(DB_DRIVER_TYPE,dbConnectData.getDriverType());
        property.put("db.driver.class.name",dbConnectData.getDriverClassName());
        property.put("db.username",dbConnectData.getUsername());
        property.put("db.password",dbConnectData.getPassword());
        property.put(DB_HOST,dbConnectData.getHost());
        property.put(DB_PORT, dbConnectData.getPort());
        property.put(DB_SID, dbConnectData.getSid());
        property.put(DB_PROTOCOL,dbConnectData.getProtocol());
        property.put(DB_ENCODING,dbConnectData.getEncoding());
        doUpdateOrInsert(property,body);

    }
    private void doUpdateOrInsert(Map<String, String> property, String body) throws SQLException {
        log.trace("Try get connection");
        Connection connection = getConnection(property);
        log.trace("Try create statement");
        Statement statement = connection.createStatement();
        log.trace("Try execute select");
        statement.execute(body);
        connection.close();
    }

}
