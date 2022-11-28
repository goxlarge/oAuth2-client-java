package goxlarge.auth.client.ports.mechanism.ORPC;

import goxlarge.auth.client.model.ORPCAuthClient;
import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.utils.AuthorizationFailure;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/*
https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth-ropc
The Microsoft identity platform supports the OAuth 2.0 Resource Owner Password Credentials (ROPC) grant, which allows
an application to sign in the user by directly handling their password. This article describes how to program directly
against the protocol in your application. When possible, we recommend you use the supported
Microsoft Authentication Libraries (MSAL) instead to acquire tokens and call secured web APIs. Also take a look at the sample apps that use MSAL.

Warning
Microsoft recommends you do not use the ROPC flow. In most scenarios, more secure alternatives are available and recommended.
This flow requires a very high degree of trust in the application, and carries risks which are not present in other flows.
You should only use this flow when other more secure flows can't be used.
 */
public class ORPCMechanism implements AuthorizationMechanism<String> {
    private static final Logger log = LoggerFactory.getLogger(ORPCMechanism.class);
    private ORPCAuthClient client;
    private List<String> scopes;

    public ORPCMechanism(ORPCAuthClient client, List<String> scopes) {
        this.client = client;
        this.scopes = scopes;
    }

    @Override
    public CompletableFuture<AuthorizationGrant<String>> authorize(String previousAuthorization) throws AuthorizationFailure {

        try {
            final Instant start = Instant.now();
            if(log.isDebugEnabled()) {
                log.debug("Making client credentials grant call: {}", client);
                log.info("Including these scopes in /token call: " + scopes);
            }
            final MultipartBody httpClient = Unirest.post(client.getAuthEndpoint() + "/token")
                    .field("client_id", client.getClientId())
                    .field("client_secret", client.getClientSecret())
                    .field("grant_type", "password")
                    .field("username", client.getUserName())
                    .field("password", new String(client.getPassword()))
                    .field("scope", scopes);

            final HttpResponse<String> response;
            try {
                response = httpClient.asString();
            } catch (Exception e) {
                log.error("ORPCMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Failed to execute /token call", e);
            }
            if (response.getStatus() >= 300) {
                log.error("ORPCMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Received an invalid HTTP response code!\n" + buildResponseSummary(response), null);
            }

            final String token;
            final Instant expires;
            try {
                final JSONObject json = new JsonNode(response.getBody()).getObject();
                token = json.getString("access_token");
                final long expiresIn = json.getLong("expires_in");
                expires = start.plus(expiresIn, ChronoUnit.SECONDS);
            } catch (Exception e) {
                log.error("ORPCMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Failed to parse Access Token from /cachedTokenRef response!\n" + buildResponseSummary(response), e);
            }
            return CompletableFuture.completedFuture(new AuthorizationGrant<>(token, expires));
        } catch (AuthorizationFailure trf) {
            log.error("ORPCMechanism failed on client id: " + client.getClientId());
            throw trf;
        } catch (Exception e) {
            log.error("ORPCMechanism failed on client id: " + client.getClientId());
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

    @Override
    public String toString() {
        return "ORPCMechanism{" +
                "client=" + client +
                ", scopes=" + scopes +
                '}';
    }
}
