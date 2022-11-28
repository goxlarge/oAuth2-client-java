# oAuth2-client-java

The `oAuth2-client-java`is a java library provides a set of java utilities for interacting with OAuth 2.0 authorization servers.  
The library also refreshes cached token when it close to expiration.  
The library supports
 - OAuth 2.0 client credentials grant(sometimes called two-legged OAuth)
 - OAuth 2.0 Resource Owner Password Credentials grant (ROPC)

## How to use

Import it with Maven from:

```java
 <dependency>
    <groupId>com.codehub</groupId>
    <artifactId>oAuth2-client-java</artifactId>
    <version>0.0.1</version>
</dependency>
```
### APIs see details in `AuthorizationCache`

```java
// define a optional token consumer. in this example, we will save taken to temp file        
Consumer<String> savetoFile = token -> {
    try {
        TokenWriter.tokenToTempFile(token, "myToken");
    } catch (IOException e) {
        e.printStackTrace();
    }
};

// create AuthClient with token url, id and secret
AuthClient authClient = new AuthClient("https://api.thirdparty.com/identity/v1", "clientId","clientSecret");

// get the token cache wrapper
AuthorizationCache<String> authzCodeGrantAuthorizationCache =
        AuthorizationCache.clientCredentialsCache(authClient, Collections.singletonList(""), savetoFile);

// or you may want to get token explicitly
String token = authzCodeGrantAuthorizationCache.cachedAuthorization().join();
System.out.println(token);
```