package goxlarge.auth.client.ports.cache;

import goxlarge.auth.client.model.AuthClient;
import goxlarge.auth.client.model.AuthzCodeGrant;
import goxlarge.auth.client.model.ORPCAuthClient;
import goxlarge.auth.client.model.RoleBasedORPCAuthClient;
import goxlarge.auth.client.ports.cache.preemptive.PreemptiveAuthorizationCache;
import goxlarge.auth.client.ports.cache.preemptive.PreemptiveSchedulingAlgorithm;
import goxlarge.auth.client.ports.mechanism.ORPC.ORPCMechanism;
import goxlarge.auth.client.ports.mechanism.AuthorizationMechanism;
import goxlarge.auth.client.ports.mechanism.ORPC.RoleBasedORPCMechanism;
import goxlarge.auth.client.ports.mechanism.client_cred.ClientCredentialsMechanism;
import goxlarge.auth.client.ports.mechanism.store_wrapper.StoreWrapperMechanism;
import goxlarge.auth.client.ports.store.filesystem.GenericFileStore;
import net.jodah.failsafe.RetryPolicy;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Cache's an authorization for more efficient access across multiple components.
 */
public interface AuthorizationCache<T> {
    /**
     * @return A promise of an authorization. If an authorization is already cached, the returned future should be pre-completed.
     *         If there isn't a valid cached authorization, the future will return when the authorization mechanism retrieves a new authorization.
     *         It's advised that you use a timeout and error out in the case of an auth server outage.
     */
    CompletableFuture<T> cachedAuthorization();

    /**
     * This will clear out the current cached authorization if there is one.
     */
    void invalidateCache();

    /**
     * Creates a default authorization cache with the given parameters sourced via client credentials
     * @param client The auth client variables
     * @param scopes resource server scope
     * @param tokenConsumer custom token consumer called after receiving token from token server
     * @return
     */
     static AuthorizationCache<String> clientCredentialsCache(AuthClient client, List<String> scopes, Consumer<String> tokenConsumer) {
        final RetryPolicy retryPolicy = new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS);
        final AuthorizationMechanism<String> authorizationMechanism = new ClientCredentialsMechanism(client, scopes);
        final PreemptiveSchedulingAlgorithm refreshAlgorithm = PreemptiveSchedulingAlgorithm.variableBuffer(0.75);

        return new PreemptiveAuthorizationCache<>(authorizationMechanism, refreshAlgorithm, tokenConsumer, retryPolicy);
    }

    /**
     * Creates Azure Access Control(ACS) authorization cache with the given parameters sourced via client credentials
     * @param client The auth client variables
     * @param scopes resource server scope
     * @param tokenConsumer custom token consumer called after receiving token from token server
     * @return
     */
    static AuthorizationCache<String> clientCredentialsORPCCache(ORPCAuthClient client, List<String> scopes, Consumer<String> tokenConsumer) {
        final RetryPolicy retryPolicy = new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS);
        final AuthorizationMechanism<String> authorizationMechanism = new ORPCMechanism(client, scopes);
        final PreemptiveSchedulingAlgorithm refreshAlgorithm = PreemptiveSchedulingAlgorithm.variableBuffer(0.75);

        return new PreemptiveAuthorizationCache<>(authorizationMechanism, refreshAlgorithm, tokenConsumer, retryPolicy);
    }
    /**
     * Creates authorization cache with the given parameters sourced via client credentials
     * @param client The auth client object variables
     * @param tokenConsumer custom token consumer called after receiving token from token server
     * @return
     */
    static AuthorizationCache<String> roleBasedCredentialsORPCCache(RoleBasedORPCAuthClient client, List<String> scopes, Consumer<String> tokenConsumer) {
        final RetryPolicy retryPolicy = new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS);
        final AuthorizationMechanism<String> authorizationMechanism = new RoleBasedORPCMechanism(client, scopes);
        final PreemptiveSchedulingAlgorithm refreshAlgorithm = PreemptiveSchedulingAlgorithm.variableBuffer(0.75);

        return new PreemptiveAuthorizationCache<>(authorizationMechanism, refreshAlgorithm, tokenConsumer, retryPolicy);
    }
    /**
     * Creates a default authorization cache with the given parameters sourced via a static s3 access token
     * @param pathSupplier the path to use for fetching the access token
     * @return
     */
     static AuthorizationCache<String> fileAccessTokenFileCache(Supplier<Path> pathSupplier) {
        if(pathSupplier == null){
            pathSupplier = () -> Path.of("/tmp/token");
        }
        final RetryPolicy retryPolicy = new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS);

        final AuthorizationMechanism<String> authorizationMechanism = new StoreWrapperMechanism<>(GenericFileStore.ofAccessToken(pathSupplier), retryPolicy);
        final PreemptiveSchedulingAlgorithm refreshAlgorithm = PreemptiveSchedulingAlgorithm.variableBuffer(0.9);
        return new PreemptiveAuthorizationCache<>(authorizationMechanism, refreshAlgorithm, null, retryPolicy);
    }

     static AuthorizationCache<AuthzCodeGrant> fileAccessAuthzCodeGrantFileCache(Supplier<Path> pathSupplier) {
        if(pathSupplier == null){
            pathSupplier = () -> Path.of("/tmp/token");
        }
        final RetryPolicy retryPolicy = new RetryPolicy().withBackoff(1, 30, ChronoUnit.SECONDS);

        final AuthorizationMechanism<AuthzCodeGrant> authorizationMechanism = new StoreWrapperMechanism<>(GenericFileStore.ofAuthzCodeGrant(pathSupplier), retryPolicy);
        final PreemptiveSchedulingAlgorithm refreshAlgorithm = PreemptiveSchedulingAlgorithm.variableBuffer(0.9);
        return new PreemptiveAuthorizationCache<>(authorizationMechanism, refreshAlgorithm, null, retryPolicy);
    }
}
