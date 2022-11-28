package goxlarge.auth.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleBasedORPCAuthClient extends ORPCAuthClient{
    @JsonProperty("username")
    private RoleBasedUser roleBasedUser;

    public RoleBasedORPCAuthClient(String authEndpoint, String clientId, String clientSecret, RoleBasedUser user, char[] password) {
        super(authEndpoint, clientId, clientSecret, password);
        this.roleBasedUser = user;
    }

    public RoleBasedUser getRoleBasedUser() {
        return this.roleBasedUser;
    }

    public void setRoleBasedUser(RoleBasedUser user) {
        this.roleBasedUser = user;
    }

    @Override
    public String toString() {
        return "RoleBasedORPCAuthClient{" +
                "RoleBasedUser=" + roleBasedUser +
                '}';
    }
}

