package messages;

import java.util.HashMap;

public class Message {

    String body;
    HashMap<String, String> headers;

    public Message(String body) {
        this.body = body;
        this.headers = new HashMap<>();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getHeaderValue(String key) {
        return headers.get(key);
    }

    public void setHeaders(HashMap<String, String> headers) {
        for (String s : headers.keySet()) {
            this.headers.put(s, headers.get(s));
        }
    }


    public void setHeader(String key, String value) {
        headers.put(key, value);
    }
}
