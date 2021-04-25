package Exceptions;

public class SazanException extends Exception {
    public SazanException(){
        super();
    }
    public SazanException (String message){
        super(message);
    }
    public SazanException(String message, Throwable cause){
        super(message, cause);
    }
    public SazanException(Throwable cause){
        super(cause);
    }
}
