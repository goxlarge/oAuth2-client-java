package goxlarge.auth.client.ports.mechanism.refresh_token;

import goxlarge.auth.client.model.AuthClient;
import goxlarge.auth.client.model.AuthzCodeGrant;
import goxlarge.auth.client.model.ModelMapper;
import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.utils.AuthorizationFailure;
import goxlarge.auth.client.ports.utils.AuthorizationGrant;
import kong.unirest.HttpResponse;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public class RefreshTokenMechanism implements AuthorizationMechanism<AuthzCodeGrant> {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenMechanism.class);
    private final AuthClient client;

    public RefreshTokenMechanism(AuthClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<AuthorizationGrant<AuthzCodeGrant>> authorize(AuthzCodeGrant previousAuthorization) throws AuthorizationFailure {
        try {
            final Instant start = Instant.now();
            final MultipartBody httpClient = Unirest.post(client.getAuthEndpoint() + "/token")
                    .basicAuth(client.getClientId(), client.getClientSecret())
                    .field("grant_type", "refresh_token")
                    .field("access_token", previousAuthorization.getAccessToken())
                    .field("refresh_token", previousAuthorization.getRefreshToken());
            final HttpResponse<String> response;
            try {
                response = httpClient.asString();
            } catch (Exception e) {
                throw new AuthorizationFailure("Failed to execute /token call", e);
            }
            if (response.getStatus() >= 300) {
                throw new AuthorizationFailure("Received an invalid HTTP response code!\n" + buildResponseSummary(response), null);
            }

            final AuthzCodeGrant authz;
            final Instant expires;
            try {
                authz = ModelMapper.deserializeAuthzCodeGrant(response.getBody());
                expires = start.plus(authz.getExpiresIn(), ChronoUnit.SECONDS);
            } catch (Exception e) {
                throw new AuthorizationFailure("Failed to parse Authz Code Grant response!\n" + buildResponseSummary(response), e);
            }
            return CompletableFuture.completedFuture(new AuthorizationGrant<>(authz, expires));
        } catch (AuthorizationFailure trf) {
            throw trf;
        } catch (Exception e) {
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
        return "RefreshTokenMechanism{" +
                "client=" + client +
                '}';
    }
}
