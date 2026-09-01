# Identity4J Remote Client

`identity4j-remote-client` exposes the `identity4j-remote` HTTP bridge as a standard Identity4J `Connector`.

## Configuration

Use `RemoteConnectorConfiguration` (`com.identity4j.remote.client.RemoteConnectorConfiguration`) with:

- `uri` - base URL to the remote servlet filter endpoint (for example `https://id.example.com/i4j`)
- `remote.connectorClass` - fully qualified target connector class on the remote server
- all other properties - forwarded to `POST /connectors/<i4jConnectorClass>`

## Secrets API

Secrets are outside the core `Connector` API. Use `RemoteSecretServiceClient`:

- `create(alias, values)`
- `patch(alias, values)`
- `aliases()`
- `exists(alias)`
- `delete(alias)`

## Example

```java
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.Connector;
import com.identity4j.connector.principal.Identity;
import com.identity4j.remote.client.RemoteConnectorConfiguration;
import com.identity4j.remote.client.RemoteSecretServiceClient;
import com.identity4j.remote.client.SecretServiceClient;
import com.identity4j.util.MultiMap;

import java.util.Map;

// Shared base endpoint for the servlet filter mapping.
String baseUri = "https://id.example.com/i4j";

// 1) Manage secret aliases over the remote Secrets API.
SecretServiceClient secrets = new RemoteSecretServiceClient(baseUri);
secrets.create("ad-service-account", Map.of(
	"username", "Administrator",
	"password", "changeMe"
));

// 2) Build a normal Identity4J Connector against the remote bridge.
MultiMap cfg = new MultiMap();
cfg.set("i4jConfigurationClass", RemoteConnectorConfiguration.class.getName());
cfg.set("uri", baseUri);
cfg.set("remote.connectorClass", "com.identity4j.connector.jndi.activedirectory.ActiveDirectoryConnector");
cfg.set("directory.hostname", "10.0.1.1");
cfg.set("directory.domain", "acme.lan");
cfg.set("directory.serviceAccountUsername", "%credential.ad-service-account.username%");
cfg.set("directory.serviceAccountPassword", "%credential.ad-service-account.password%");

Connector<?> connector = new ConnectorBuilder().buildConnector(cfg);
try {
	Identity identity = connector.getIdentityByName("joe.bloggs", true);
	System.out.println(identity.getFullName());
} finally {
	connector.close();
}
```

## Notes

- Uses Java built-in `java.net.http.HttpClient`.
- Uses Jackson for JSON payloads.
- Does not depend on `identity4j-http`.
