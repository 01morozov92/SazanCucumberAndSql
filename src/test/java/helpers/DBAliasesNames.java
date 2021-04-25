package helpers;

public class DBAliasesNames {
    String driverType;
    String driverClassName;
    String username;
    String password;
    String host;
    String port;
    String sid;
    String encoding;
    String protocol;

    public DBAliasesNames(String driverType, String driverClassName, String username, String password, String host, String port, String sid,String encoding,String protocol) {
        this.driverType = driverType;
        this.driverClassName = driverClassName;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.sid = sid;
        this.encoding = encoding;
        this.protocol = protocol;
    }

    public String getDriverType() {
        return driverType;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getSid() {
        return sid;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getProtocol() {
        return protocol;
    }

}
