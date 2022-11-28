package goxlarge.auth.client.ports.mechanism.client_cred;

import goxlarge.auth.client.model.AuthClient;
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
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class ClientCredentialsMechanism implements AuthorizationMechanism<String> {
    private static final Logger log = LoggerFactory.getLogger(ClientCredentialsMechanism.class);
    private final AuthClient client;
    private final String scopes;
    private final String clientDescription;

    public ClientCredentialsMechanism(AuthClient client, List<String> scopes) {
    	this(client, "", scopes);
    }

    public ClientCredentialsMechanism(AuthClient client, String clientDescription, List<String> scopeList) {
        this.client = client;
        this.clientDescription = clientDescription;

        StringJoiner scopesBuilder = new StringJoiner(" ");
        for (String scope : scopeList) {
            scopesBuilder.add(scope);
        }
        this.scopes = scopesBuilder.toString();
    }
    
    @Override
    public CompletableFuture<AuthorizationGrant<String>> authorize(String previousAuthorization) {
        try {
            final Instant start = Instant.now();
            if(log.isDebugEnabled()) {
                log.debug("Making client credentials grant call: {}", client);
            }
            final MultipartBody httpClient = Unirest.post(client.getAuthEndpoint() + "/token")
                    .basicAuth(client.getClientId(), client.getClientSecret())
                    .field("grant_type", "client_credentials");

            if(log.isDebugEnabled()) {
                log.debug("Including these scopes in /token call: " + scopes);
            }
            httpClient.field("scope", scopes);

            final HttpResponse<String> response;
            try {
                response = httpClient.asString();
            } catch (Exception e) {
                log.error("ClientCredentialsMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Failed to execute /token call", e);
            }
            if (response.getStatus() >= 300) {
                log.error("ClientCredentialsMechanism failed on client id: " + client.getClientId());
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
                log.error("ClientCredentialsMechanism failed on client id: " + client.getClientId());
                throw new AuthorizationFailure("Failed to parse Access Token from /cachedTokenRef response!\n" + buildResponseSummary(response), e);
            }
            return CompletableFuture.completedFuture(new AuthorizationGrant<>(token, expires));
        } catch (AuthorizationFailure trf) {
            log.error("ClientCredentialsMechanism failed on client id: " + client.getClientId());
            throw trf;
        } catch (Exception e) {
            log.error("ClientCredentialsMechanism failed on client id: " + client.getClientId());
            throw new AuthorizationFailure("An unexpected error occurred while retrieving a token", e);
        }
    }

    private static String buildResponseSummary(HttpResponse<String> response) {
        return String.format(
                "Response Code: %s\nHeaders:\n%s\nBody:\n%s",
                String.valueOf(response.getStatus()),
                String.valueOf(response.getHeaders()),
                String.valueOf(response.getBody())
        );
    }

    @Override
    public String toString() {
        return "ClientCredentialsMechanism{" +
                "client=" + client +
                ", scopes='" + scopes + '\'' +
                ", clientDescription='" + clientDescription + '\'' +
                '}';
    }
}
