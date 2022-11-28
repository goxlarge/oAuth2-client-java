package goxlarge.auth.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthClient {
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("auth_endpoint")
    private String authEndpoint;

    public AuthClient() { }

    public AuthClient(String authEndpoint, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authEndpoint = authEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    @Override
    public String toString() {
        return "AuthClient{" +
                "clientId='" + clientId + '\'' +
                ", clientSecret=" + (clientSecret == null || clientSecret.length() == 0 ? "none" : "present") +
                ", authEndpoint='" + authEndpoint + '\'' +
                '}';
    }
}
