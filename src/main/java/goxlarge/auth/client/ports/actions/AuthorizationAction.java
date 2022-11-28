package goxlarge.auth.client.ports.actions;


import goxlarge.auth.client.common.BaseAction;

public class AuthorizationAction extends BaseAction<AuthorizationAction> {

    public AuthorizationAction() {
        super("AuthorizationGrant");
    }

    public AuthorizationAction withMechanism(String m) {
        return withDetail("Mechanism", m);
    }

    public AuthorizationAction isClientCredentials() {
        return withMechanism("ClientCredentials");
    }

    public AuthorizationAction isRefreshCode() {
        return withMechanism("RefreshCode");
    }

    public AuthorizationAction isS3ObjectMechanism() {
        return withMechanism("S3Object");
    }

    public AuthorizationAction unknownMechanism() {
        return withMechanism("Unknown");
    }

    @Override
    protected AuthorizationAction getThis() {
        return this;
    }
}
