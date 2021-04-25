package vars;

import messages.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestVars {

    private Map<String, Message> messages = new HashMap<>();

    private Message response;

    private Map<String, String> variables = new HashMap<>();

    public Map<String, List<String>> getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(Map<String, List<String>> queryResult) {
        this.queryResult = queryResult;
    }

    private Map<String, List<String>> queryResult;

    public TestVars() {
        //Comment for Sonar, explaining why this method is empty. Its empty because I decided it to be
    }

    public void setResponse(Message response) {
        this.response = response;
    }

    public Message getMessage(String key){
        return messages.get(key);
    }

    public Message getResponse() {
        return response;
    }

    public Map<String, String> getVariables() {
        return this.variables;
    }

    public void setVariables(String variableName, String tagValue) {
        variables.put(variableName, tagValue);
    }
}

