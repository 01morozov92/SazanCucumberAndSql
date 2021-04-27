package steps;

import Exceptions.CustomException;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.ru.Тогда;
import dbConnector.StatementExecute;
import gherkin.deps.com.google.gson.Gson;
import helpers.Configurier;
import helpers.NetworkUtils;
import helpers.TestVarsInstance;
import io.cucumber.datatable.DataTable;
import messages.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testData.PrepareBody;
import testData.Templater;
import vars.LocalThead;
import vars.TestVars;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

public class MyStepdefs {

    private static final Logger log = LoggerFactory.getLogger(MyStepdefs.class);
    private static final Configurier configer = Configurier.getInstance();
    private static final String FILE = "file:";
    private Scenario scenario;
    private static final TestVarsInstance testVarsInstance = TestVarsInstance.getTestVarsInstance();
    private static final Map<String, String> VarMemory = new HashMap<>();

    @Before
    public void beforeScenario(final Scenario scenario) throws CustomException {
        TestVars testVars = new TestVars();
        LocalThead.setTestVars(testVars);
        this.scenario = scenario;
        System.out.println(String.format("Start test: {}", this.scenario.getUri()));
        configer.loadApplicationPropertiesForSegment();
        LocalThead.setTestVars(testVarsInstance.getTestVars());
    }

    @After
    public void afterScenario() {
        LocalThead.setTestVars(null);
        System.out.println(String.format("End test: %s", this.scenario.getUri()));
    }


    @Тогда("^Выполнить обновление БД ([^\"]*) запросом ([^\"]*)$")
    public void makeInsertOrUpdateDB(String dbalias, String sqlrequest) throws CustomException, SQLException, ParseException, IOException, org.apache.velocity.runtime.parser.ParseException {
        TestVars testVars = LocalThead.getTestVars();
        executeSqlRequestWithoutReturn(sqlrequest, testVars.getVariables(), dbalias);
    }

    @Тогда("^(?:Сгенерировать переменную) ([a-zA-Z0-9а-яА-Я_]+)? по ответному http-сообщению")
    public void generateByResponseHttp(String variableName) {
        TestVars testVars = LocalThead.getTestVars();
        String messageBody = testVars.getResponse().getBody();
        testVars.setVariables(variableName, messageBody);
        LocalThead.setTestVars(testVars);
    }

    @Тогда("^Проверить содержится ли ([^\"]*) в json-теле http-сообщения ([^\"]*)")
    public void checkContainJsonValueHttp(String varname, String messageAlias) {
        TestVars testVars = LocalThead.getTestVars();
        String messagebody = testVars.getVariables().get(messageAlias);
        if (messagebody.contains(varname)) {
            log.info(String.format("Response contain string %s", varname));
        } else {
            fail(String.format("Response does not contain string %s", varname));
        }
    }

    @Тогда("^Убедиться в наличии записей в БД ([^\"]*) по запросу ([^\"]*)$")
    public void checkIfDbHasData(String dbAlias, String sqlrequest) throws CustomException, SQLException, org.apache.velocity.runtime.parser.ParseException, IOException {
        TestVars testVars = LocalThead.getTestVars();
        testVars.setQueryResult(executeSqlRequestWithReturn(sqlrequest, testVars.getVariables(), dbAlias));
        Map<String, List<String>> queryresult = testVars.getQueryResult();
        boolean isReturnNonEmpty = false;
        for (String s : queryresult.keySet()) {
            if (queryresult.get(s).size() > 0) {
                isReturnNonEmpty = true;
                break;
            }
        }
        Assert.assertTrue(String.format("Выборка по запросу %s пуста", sqlrequest), isReturnNonEmpty);
        log.info(String.format("Выборка по запросу %s не пуста", sqlrequest));
    }

    @Тогда("^Сохранить ([^\"]*) из json-тела сообщения ([^\"]*) в переменную ([^\"]*)$")
    public void saveJsonValue(String jsonPath, String messageAlias, String varname) {
        TestVars testVars = LocalThead.getTestVars();
        String messagebody = testVars.getVariable(messageAlias);
        String jsonPathVal = getValueFromJsonPath(messagebody, jsonPath);
        testVars.setVariables(varname, jsonPathVal);
        System.out.println((String.format("Variable with value %s stored to %s", jsonPathVal, varname)));
    }

    @Тогда("^вывести в консоль переменную ([^\"]*)$")
    public void printVar(String varname) {
        TestVars testVars = LocalThead.getTestVars();
        System.out.println(testVars.getVariable(varname));
    }

    @Тогда("Убедиться в истинности числового выражения (.*)")
    public void checkEvalsTrueInt(String eval) throws ScriptException {
        TestVars testVars = LocalThead.getTestVars();
        String streval = eval;
        for (String key : testVars.getVariables().keySet()) {
            if (streval.contains(key)) {
                streval = streval.replaceAll(key, testVars.getVariables().get(key));
            }
        }
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        log.info("Checking expression: {}", streval);
        assertTrue("Expression is false", (Boolean) engine.eval(streval));
        LocalThead.setTestVars(testVars);
    }

    @Тогда("^Изменить значение переменной (.*) на (.*)")
    public void changeTestVariable(String varName, String varValue) {
        TestVars testVars = LocalThead.getTestVars();
        if (checkVars(varValue)) {
            varValue = replaceTestVariableValue(varValue, testVars);
        }
        testVars.setVariables(varName, varValue);
        LocalThead.setTestVars(testVars);
        replaceAllTestVariableValue();
    }

    @Тогда("Убедиться в истинности выражения ([^\"]*) == ([^\"]*)")
    public void checkEvalsTrue(String param1, String param2) throws ScriptException {
        TestVars testVars = LocalThead.getTestVars();
        String parameter1 = testVars.getVariable(param1);
        String parameter2 = testVars.getVariable(param2);
        assertEquals("Expression is false", parameter1, parameter2);
        LocalThead.setTestVars(testVars);
    }

    @Тогда("^Выполнить SQL-запрос ([^\"]*) в БД ([^\"]*) и сохранить массив в переменную ([^\"]*)$")
    public void executeSQLqueryAndSaveMassInVal(String sqlrequest, String dbAlias, String var) throws CustomException, SQLException, org.apache.velocity.runtime.parser.ParseException, IOException {
        TestVars testVars = LocalThead.getTestVars();
        testVars.setQueryResult(executeSqlRequestWithReturn(sqlrequest, testVars.getVariables(), dbAlias));
        Map<String, List<String>> queryresult = testVars.getQueryResult();
        if (queryresult.keySet().size() > 1) {
            List<String> resultList = new ArrayList<>();
            for (String key : queryresult.keySet()) {
                if (queryresult.get(key).size() < 1) {
                    throw new CustomException("Результат выполнения скрипта выдал пустое множество");
                }
                resultList.add(queryresult.get(key).get(0));
            }
            String jsonVar = new Gson().toJson(resultList);
            testVars.setVariables(var, jsonVar);
        } else {
            for (String key : queryresult.keySet()) {
                if (queryresult.get(key).size() >= 1) {
                    String jsonVar = new Gson().toJson(queryresult.get(key));
                    testVars.setVariables(var, jsonVar);
                } else if (queryresult.get(key).size() < 1) {
                    throw new CustomException("Результат выполнения скрипта выдал пустое множество");
                }
            }
        }
        log.info(String.format("Variable %s stored with value %s", var, testVars.getVariables().get(var)));
    }

    @Тогда("^Послать HTTP запрос ?(.*) в эндпоинт ([^\\s]*) c дефолтными заголовками$")
    public void sendHttp(String bodyFile, String endPoint) throws IOException, org.apache.velocity.runtime.parser.ParseException, CustomException {
        TestVars testVars = LocalThead.getTestVars();
        Message message;
        if (checkVars(endPoint)) {
            endPoint = endPoint.replace("${env}", configer.getEnviroment().toLowerCase());
            endPoint = replaceTestVariableValue(endPoint, testVars);
        }
        if (!bodyFile.equals("")) {
            String body = new PrepareBody(this.scenario.getUri().replaceFirst("file:", ""), bodyFile).loadBody();
            String filledBody = new Templater(body, testVars.getVariables()).fillTemplate();
            message = new Message(filledBody);
        } else {
            message = new Message("");
        }
        message.setHeader("Content-Type", "text/xml");
        if (checkVars(endPoint)) {
            endPoint = replaceTestVariableValue(endPoint, testVars);
        }
        testVars.setResponse(NetworkUtils.sendHttp(message, endPoint));
        LocalThead.setTestVars(testVars);
    }

    @Тогда("^Послать HTTP запрос ?(.*) в эндпоинт ([^\\s]*)$")
    public void sendHttp(String bodyFile, String endPoint, DataTable dataTable) throws IOException, org.apache.velocity.runtime.parser.ParseException, CustomException {
        TestVars testVars = LocalThead.getTestVars();
        Message message;
        if (checkVars(endPoint)) {
            endPoint = endPoint.replace("${env}", configer.getEnviroment().toLowerCase());
            endPoint = replaceTestVariableValue(endPoint, testVars);
        }
        if (!bodyFile.equals("")) {
            String body = new PrepareBody(this.scenario.getUri().replaceFirst("file:", ""), bodyFile).loadBody();
            String filledBody = new Templater(body, testVars.getVariables()).fillTemplate();
            message = new Message(filledBody);
        } else {
            message = new Message("");
        }
        Map<String, String> headers = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            message.setHeader(entry.getKey(), entry.getValue());
        }
        testVars.setResponse(NetworkUtils.sendHttp(message, endPoint));
        LocalThead.setTestVars(testVars);
    }

    @Тогда("^Выполнить SQL-запрос ([^\"]*) в БД ([^\"]*) и сохранить значение ячейки в переменную ([^\"]*)$")
    public void executeSQLqueryAndSaveVal(String sqlrequest, String dbAlias, String var) throws CustomException, SQLException, org.apache.velocity.runtime.parser.ParseException, IOException {
        TestVars testVars = LocalThead.getTestVars();
        testVars.setQueryResult(executeSqlRequestWithReturn(sqlrequest, testVars.getVariables(), dbAlias));
        Map<String, List<String>> queryresult = testVars.getQueryResult();
        if (queryresult.keySet().size() > 1) {
            throw new CustomException("Результат выполнения скрипта выдал больше одного столбца. Невозможно сохранить в переменную");
        } else {
            for (String key : queryresult.keySet()) {
                if (queryresult.get(key).size() > 1) {
                    throw new CustomException("Результат выполнения скрипта выдал больше одной строки. Невозможно сохранить в переменную");
                } else if (queryresult.get(key).size() < 1) {
                    throw new CustomException("Результат выполнения скрипта выдал пустое множество");
                } else {
                    testVars.setVariables(var, queryresult.get(key).get(0));
                }
            }
        }
        System.out.println((String.format("Variable %s stored with value %s", var, testVars.getVariables().get(var))));
    }

    public void replaceAllTestVariableValue() {
        TestVars testVars = LocalThead.getTestVars();
        for (Map.Entry entry : testVars.getVariables().entrySet()) {
            if (Objects.nonNull(VarMemory.get(entry.getKey())) && VarMemory.get(entry.getKey()).contains("$")) {
                testVars.setVariables(entry.getKey().toString(), replaceTestVariableValue(VarMemory.get(entry.getKey()), testVars));
            }
        }
        LocalThead.setTestVars(testVars);
    }

    private String getValueFromJsonPath(String jsonmessage, String jsonPath) {
        JSONObject jsonObject1 = null;
        JSONObject jsonObject = new JSONObject(jsonmessage);
        if (jsonPath.contains("[")) {
            JSONObject jsonObjectData = new JSONObject();
            String[] jsonArray = jsonPath.split(":\\[");
            String jsonPathsArr[] = jsonArray[1].split(":");
            if (jsonArray[0].contains(":")) {
                String jsonPaths[] = jsonArray[0].split(":");
                for (int i = 0; i < jsonPaths.length - 1; i++) {
                    try {
                        jsonObjectData = (JSONObject) jsonObject.get(jsonPaths[i]);
                    } catch (JSONException e) {
                        return "NOCURRENTKEY";
                    }
                }
                jsonArray[0] = jsonPaths[jsonPaths.length - 1];
            }
            JSONArray arr = jsonObjectData.getJSONArray(jsonArray[0]);
            for (int i = 0; i < arr.length(); i++) {
                if (arr.getJSONObject(i).toString().contains(jsonPathsArr[jsonPathsArr.length - 1])) {
                    try {
                        jsonObject = arr.getJSONObject(i);
                    } catch (JSONException e) {
                        return "NOCURRENTKEY";
                    }
                }
            }
            return jsonObject.get(jsonPathsArr[jsonPathsArr.length - 1]).toString();
        } else if (jsonPath.contains(":")) {
            String[] jsonPaths = jsonPath.split(":");
            for (int i = 0; i < jsonPaths.length - 1; i++) {
                try {
                    String newJsonPath = jsonObject.get(jsonPaths[i]).toString();
                    String res1 = newJsonPath.replace("[", "");
                    String res2 = res1.replace("]", "");
                    jsonObject1 = new JSONObject(res2);
//                    jsonObject = (JSONObject) jsonObject.get(jsonPaths[i]);
                } catch (JSONException e) {
                    return "NOCURRENTKEY";
                }
            }
            return jsonObject1.get(jsonPaths[jsonPaths.length - 1]).toString();
        } else {
            String jsonPaths[] = jsonPath.split(":");
            for (int i = 0; i < jsonPaths.length - 1; i++) {
                try {
                    jsonObject = (JSONObject) jsonObject.get(jsonPaths[i]);
                } catch (JSONException e) {
                    return "NOCURRENTKEY";
                }
            }
            return jsonObject.get(jsonPaths[jsonPaths.length - 1]).toString();
        }
    }

    public boolean checkVars(String entry) {
        return entry.contains("$") ? true : false;
    }

    private void executeSqlRequestWithoutReturn(String sqlFile, Map<String, String> templateParam, String dbalias) throws ParseException, IOException, CustomException, SQLException, org.apache.velocity.runtime.parser.ParseException {
        String lines = new PrepareBody(this.scenario.getUri().replaceFirst(FILE, ""), sqlFile).loadBody();
        //lines возвращает путь до файла типа стринг C:\Users\MI\IdeaProjects\sazan_core/src/test/resources/features/test/sql/okr/PskToPdn/GetLastNameByActorId.sql  .
        String filledTempl = new Templater(lines, templateParam).fillTemplate(); //заполняется шаблон из предыстории и подставляется в sql запрос на место переменных
        System.out.println(String.format("Executing SQL Query: %s", filledTempl));
        StatementExecute exec = new StatementExecute();
        exec.executeUpdateOrInsertSQLQuery(configer.getApplicationProperties(), filledTempl, dbalias);
    }

    private Map<String, List<String>> executeSqlRequestWithReturn(String sqlFile, Map<String, String> templateParam, String dbalias) throws org.apache.velocity.runtime.parser.ParseException, IOException, CustomException, SQLException {
        String lines = new PrepareBody(this.scenario.getUri().replaceFirst(FILE, ""), sqlFile).loadBody();
        String filledTempl = new Templater(lines, templateParam).fillTemplate();
        System.out.println(String.format("Executing SQL Query: %s", filledTempl));
        StatementExecute exec = new StatementExecute();
        return exec.executeSQLQueryNew(configer.getApplicationProperties(), filledTempl, dbalias);
    }

    @Тогда("^Заполнить шаблон значениями$")
    public void fillTemplate(DataTable dataTable) {
        TestVars testVars = LocalThead.getTestVars();
        Map<String, String> tmpl = dataTable.asMap(String.class, String.class);
        for (Map.Entry entry : tmpl.entrySet()) {
            if (entry.getValue().toString().startsWith("$")) {
                VarMemory.put(entry.getKey().toString(), entry.getValue().toString());
                testVars.setVariables(entry.getKey().toString(), replaceTestVariableValue(entry.getValue().toString(), testVars));
            } else if (entry.getValue().toString().contains("$")) {
                VarMemory.put(entry.getKey().toString(), entry.getValue().toString());
                testVars.setVariables(entry.getKey().toString(), replaceTestVariableValue(entry.getValue().toString(), testVars));
            } else {
                testVars.setVariables(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        LocalThead.setTestVars(testVars);
    }

    public String replaceTestVariableValue(String oldValue, TestVars testVars) {
        String[] vars = oldValue.split("\\$");
        String finalEntry = oldValue;
        for (int i = 1; i < vars.length; i++) {
            String var = vars[i].substring(vars[i].indexOf('{') + 1, vars[i].indexOf('}'));
            finalEntry = finalEntry.replace("${" + var + "}", testVars.getVariables().get(var));
        }
        return finalEntry;
    }
}
