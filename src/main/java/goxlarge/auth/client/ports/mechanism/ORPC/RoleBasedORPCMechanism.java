package goxlarge.auth.client.ports.mechanism.ORPC;

import goxlarge.auth.client.model.RoleBasedORPCAuthClient;
import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.utils.AuthorizationFailure;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RoleBasedORPCMechanism implements AuthorizationMechanism<String> {
    private static final Logger log = LoggerFactory.getLogger(RoleBasedORPCMechanism.class);
    private RoleBasedORPCAuthClient client;
    private List<String> scopes;

    //token will be renew every 23hours
    private final long TOKEN_EXPIRATION = 23*60*60;

    public RoleBasedORPCMechanism(RoleBasedORPCAuthClient client, List<String> scopes) {
        this.client = client;
        this.scopes = scopes;
    }

    @Override
    public CompletableFuture<AuthorizationGrant<String>> authorize(String previousAuthorization) throws AuthorizationFailure {

        try {
            final Instant start = Instant.now();
            if(log.isDebugEnabled()) {
                log.debug("Making client credentials grant call: {}", client);
            }
            JSONObject body = buildBody(client);

            final HttpResponse<String> response;
            try {
                response = Unirest.post(client.getAuthEndpoint() + "/token")
                        .header("Content-Type", "application/json")
                        .body(body)
                        .asString();
            } catch (Exception e) {
                log.error("RoleBasedORPCMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Failed to execute /token call", e);
            }
            if (response.getStatus() >= 300) {
                log.error("RoleBasedORPCMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Received an invalid HTTP response code!\n" + buildResponseSummary(response), null);
            }

            final String token;
            final Instant expires;
            try {
                final JSONObject json = new JsonNode(response.getBody()).getObject();
                token = json.getString("access_token");
                expires = start.plus(TOKEN_EXPIRATION, ChronoUnit.SECONDS);
            } catch (Exception e) {
                log.error("RoleBasedORPCMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Failed to parse Access Token from /cachedTokenRef response!\n" + buildResponseSummary(response), e);
            }
            return CompletableFuture.completedFuture(new AuthorizationGrant<>(token, expires));
        } catch (AuthorizationFailure trf) {
            log.error("RoleBasedORPCMechanism failed on client id: " + client.getClientId());
            throw trf;
        } catch (Exception e) {
            log.error("RoleBasedORPCMechanism failed on client id: " + client.getClientId());
            throw new AuthorizationFailure("An unexpected error occurred while retrieving a token", e);
        }
    }

    static String buildResponseSummary(HttpResponse<String> response) {
        return String.format(
                "Response Code: %s\nHeaders:\n%s\nBody:\n%s",
                String.valueOf(response.getStatus()),
                String.valueOf(response.getHeaders()),
                String.valueOf(response.getBody())
        );
    }

    public JSONObject buildBody (RoleBasedORPCAuthClient client){
        JSONObject username = new JSONObject();
        username.put("username", client.getRoleBasedUser().getUserName());
        username.put("role", client.getRoleBasedUser().getRole());
        JSONObject obj = new JSONObject();
        obj.put("client_id", client.getClientId());
        obj.put("client_secret", client.getClientSecret());
        obj.put("grant_type", "password");
        obj.put("username",username);
        obj.put("password", new String(client.getPassword()));
        return obj;
    }

    @Override
    public String toString() {
        return "RoleBasedORPCMechanism{" +
                "client=" + client +
                ", scopes=" + scopes +
                '}';
    }
}
