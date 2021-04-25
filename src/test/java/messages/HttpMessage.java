package messages;

public class HttpMessage  extends Message {
    int statusCode;

    public HttpMessage(String body) {
        super(body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
