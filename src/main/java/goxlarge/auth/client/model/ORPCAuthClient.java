package goxlarge.auth.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ORPCAuthClient extends AuthClient{
    @JsonProperty("username")
    private String userName;
    @JsonProperty("password")
    private char[] password;

    public ORPCAuthClient(String authEndpoint, String clientId, String clientSecret, String userName, char[] password) {
        super(authEndpoint, clientId, clientSecret);
        this.userName = userName;
        this.password = password;
    }

    public ORPCAuthClient(String authEndpoint, String clientId, String clientSecret, char[] password) {
        super(authEndpoint, clientId, clientSecret);
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ORPCAuthClient{" +
                "userName='" + userName + '\'' +
                '}';
    }
}

