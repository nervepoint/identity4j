# Identity4J HTTP API Adapter

The purpose of this module is to provide a lightweight HTTP service that makes any Identity4J Connector available over HTTP.

## Core Concepts

 * The API is REST-like.
 * The general pattern is "Obtain Connector Instance Handle", "Perform multiple operations using that handle", "Close the handle".
 * Instances not closed when they are finished with will eventually time-out.
 * An *Identity* is more commonly known as a *User*. Identity4Js choice of this terminology is historical.
 * A *Role* is more commonly known as a group. Identity4Js choice of this terminology is historical.
 * *Identity* and *Role* both consists of their own set of basic attributes (no dotted namespace in key), `principal.*` attributes that are common across *Identity* and *Role* and `principal.attributes.*` attributes that will depend on  the connector in use. All values are treated as strings. When a key have have multiple values, array index notication is used for properties (`[0]`, `[1]` etc).

## Servlet Setup (`web.xml`)

```xml
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <filter>
        <filter-name>identity4jRemote</filter-name>
        <filter-class>com.identity4j.remote.Identity4JRemoteFilter</filter-class>

        <!-- Optional: override default secrets properties file location -->
        <init-param>
            <param-name>secretPropertiesFile</param-name>
            <param-value>/var/lib/myapp/identity4j-remote-secrets.properties</param-value>
        </init-param>

        <!-- Optional: override idle timeout for connector/session/stream handles -->
        <init-param>
            <param-name>handleTimeoutSeconds</param-name>
            <param-value>1800</param-value>
        </init-param>

        <!-- Optional: plug in your own SecretService implementation -->
        <!--
        <init-param>
            <param-name>secretServiceClass</param-name>
            <param-value>com.example.remote.MySecretService</param-value>
        </init-param>
        -->
    </filter>

    <filter-mapping>
        <filter-name>identity4jRemote</filter-name>
        <url-pattern>/i4j/*</url-pattern>
    </filter-mapping>
</web-app>
```

## API

* API will accept posted content in either `text/plain` dotted name value pairs or `application/json` content.
* Depending on `Accepts` header, API will respond with with either `application/json` response which is always a `200 OK` and the `message` and `code` part of the JSON payload. Or, dotted name values as `text/plain` and an **HTTP Response Code** with status message. When plain text responses are used, success (`200`) never response with the `message` text

### General API Features

#### Response Type

If the client declares it accepts `application/json` then JSON responses will be used. `POST` will accept form url encoded fields or `application/json` `Content-Type` unless otherwise stated. Success and error `HTTP` response will be used when `application/json` is not accepted or JSON wrapper objects when it is. 

#### Streams

Some API calls may return lists of data, e.g. `GET /password-characteristics/<handle>`. When they respond, they do not responsd with the actual data, instead they response with a *Stream Handle*. This may be used to retrieve the data one at a time or in chunks (or even all in one go for smaller lists). Once a `streamHandler` is obtained, the `/streams/<streamHandle>` end-point is used to control the stream.

**A stream should always been closed (see `DELETE /streams/<streamId>`) when it is finished with, although the server will clean up any unused streams periodically. Closing a connection handle will also close any streams that is has open.

##### Get Next Item / Page / All

```
GET /streams/<streamHandle>
```

#### Parameters

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `items` | 1 | Number of items to retrieve in this request. You can either skip or get items, not both  |
| `skip`  | 0 | Number of items to skip in this request. You can either skip or get items, not both |

#### Example 1

This example requests 10 items, but there are only 2 available so the first request responses with `EOF` represented by either the JSON key `eof` or two empty lines at the end of text response.

```
GET /streams/za123gq21j?items=10
```

#### Response

JSON

```json
{
    success: true,
    message: "Page of 2 items",
    code: 200,
    eof: true,
    results: [
        {
            key1: "val1",
            key2: "val2",
            // ...
        },
        {
            key1: "val3",
            key2: "val4",
            // ...
        }
    ]
}
```

Text

```
200 OK

key1=val1
key2=val2

key1=val3
key2=val4


```

**Note for text responses EOF is denoted by two empty lines**

#### Example 1

This example request a single item (the default), but there are many available so each request does not response with EOF until the final item. 

**It is also valid for a stream to NOT declare `EOF` on it's last item, as it may not know that it is yet at EOF, instead it can return ZERO items and indicate `EOF`.**

```
GET /streams/za123gq21j
```

#### Response

JSON

```json
{
    success: true,
    message: "Page of 2 items",
    code: 200,
    eof: true,
    results: [
        {
            key1: "val1",
            key2: "val2",
            // ...
        },
        {
            key1: "val3",
            key2: "val4",
            // ...
        }
    ]
}
```

Text

```
200 OK

key1=val1
key2=val2

key1=val3
key2=val4


```

**Note for text responses EOF is denoted by two empty lines**


#### Example 3

This example request a single item (the default), but there are many available so each request does not response with EOF until the final item. 


```
GET /streams/za123gq21j?items=999
```

#### Response

JSON

```json
{
    success: true,
    message: "Page of 2 items",
    code: 200,
    eof: true,
    results: []
}
```

Text

```
200 OK


```

**Note for text responses EOF is denoted by two empty lines**

#### Errors

JSON

```json
{
    success: false,
    message: "No such stream exists"
    code: 404
}
```

Text

```
404 No such stream exists
```


##### Close A Stream

When you have finished with a stream, it should always be closed. 

```
DELETE /streams/<streamHandle>
```

#### Response

JSON

```json
{
    success: true,
    message: "Stream <streamHandle> closed",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such stream exists"
    code: 404
}
```

Text

```
404 No such stream exists
```

### New Secret

Secrets are simply property sheets that can be stored against an **alias**. The server tries to store the secrets as safely as possibly, e.g. HSM. They are optional, but recommended. You  may if you wish just pass the secrets as plain text when they are needed (i.e. when connecting to a remote identity store that requires secrets).

```
POST /secrets/<alias>
```

#### Fields

###  Form URL Encoded

| *Name*      | *Description* |
| ----------- | ------------- |
| `<property1>` | Value 1 |
| `<property2>` | Value 2 |
| `<propertyX>` | Value X |


#### Example

##### Form URL Encoded
```
POST /secrets/ad-service-account

username=Administrator
password=5w0rdf15h
```

##### JSON

```json
{
    "property1" : "Value 1",
    "property2" : "Value 2",
    "property3" : "Value X"
}
```

#### Response

JSON

```json
{
    success: true,
    message: "Secret stored using local JKS.",
    code: 201
}
```

Text

```
201 Created
```

#### Errors

JSON

```json
{
    success: false,
    message: "The secret ad-service-account already exists"
    code: 409
}
```

Text

```
409 The secret ad-service-account already exists
```

### Patch Secret

The secret property sheet can be updated. Any provided property is updated, any properties that already exist but are not provided in the updated sheet are left alone.

```
PATCH /secrets/<alias>
```

#### Fields

| *Name*      | *Description* |
| ----------- | ------------- |
| <property1> | Arbitrary property 1 |
| <property2> | Arbitrary property 2 |
| <propertyX> | Arbitrary property X |

#### Example

```
PATCH /secrets/ad-service-account

password=5w0rdf15h
```

#### Response

JSON

```json
{
    success: true,
    message: "Secret updated using local JKS.",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "The secret ad-service-account does not exists"
    code: 404
}
```

Text

```
404 The secret ad-service-account does not exists
```

### List Secrets

You can list all of the secret aliases.

```
GET /secrets
```

#### Response

JSON

```json
{
    success: true,
    message: "There is 1 secret.",
    code: 200
    secrets: [
        "ad-service-account"
    ]
}
```

Text

```
200 OK

ad-service-account
```


### Validate Secret

You cannot get the contents of a secret, but you can check if it's alias exists.

```
HEAD /secrets/<alias>
```

#### Response

JSON

```json
{
    success: true,
    message: "Secret ad-service-account exists.",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "The secret ad-service-account does not exists"
    code: 404
}
```

Text

```
404 The secret ad-service-account does not exists
```

### Delete Secret

Securely delete the secret.

```
DELETE /secrets/<alias>
```

#### Response

JSON

```json
{
    success: true,
    message: "Secret ad-service-account deleted.",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "The secret ad-service-account does not exist"
    code: 404
}
```

Text

```
404 The secret ad-service-account does not exist
```

### Connector Instantiation

Starts the main functions of the API, connects a remote identity store. Upon success, you are given a `handle`, which is then used for all subsequent API calls for this connector instance.

```
POST /connectors/<i4jConnectorClass>
```

#### Fields


| *Name*      | *Description* |
| ----------- | ------------- |
| <property1> | A property the connector type supports |
| <property2> | A property the connector type supports |
| <propertyX> | A property the connector type supports |

#### Example

URL Encoded form fields.

```
POST /connectors/com.identity4j.connector.jndi.activedirectory.ActiveDirectoryConnector

directory.hostname=10.0.1.1
directory.domain=acme.lan
directory.serviceAccountUsername=%credential.ad-service-account.username%
directory.serviceAccountPassword=%credential.ad-service-account.password%
```

JSON

```
POST /connectors/com.identity4j.connector.jndi.activedirectory.ActiveDirectoryConnector
{
    "directory" : {
        "hostname": "10.0.1.1",
        "domain": "acme.lan",
        "serviceAccountUsername" : "%credential.ad-service-account.username%",
        "serviceAccountPassword" : "%credential.ad-service-account.password%"
    }
}
```

#### Response

JSON

```json
{
    success: true,
    code: 201
    message: "The connector was opened."
    handle: "sdfkljl432"
}
```

Text

```
201 Created

sdfkljl43258u9
```

#### Errors

JSON

```json
{
    success: false,
    message: "Connection refused. 10.0.1.1"
    code: 404
}
```

Text

```
404 Connection refused. 10.0.1.1
```

### Close Connector

Close a previously opened connector instance handle and release any associated server-side resources including open streams and sessions.

```
DELETE /connectors/<handle>
```

#### Response

JSON

```json
{
    success: true,
    message: "Connector <handle> closed.",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such connector instance with handle <handle>"
    code: 404
}
```

Text

```
404 No such connector instance with handle <handle>
```

### List Capabilities

Capabilities are binary flags that tell you what the connector instances abilities are. An exception will be thrown if you attempt anything not possible with the current instance, but this list allows you to check up front what *should* work.

`GET /capabilities/<handle>`

#### Response

JSON

```json
{
    success: true,
    message: "There are 20 capabilities.",
    code: 200
    capabilities: [
        "accountLocking",
        "accountUnlock",
        "passwordChange",
        "passwordSet",
        "createUser",
        "updateUser",
        "deleteUser"
    ]
}
```

Text

```
200 OK

accountLocking
accountUnlock
passwordChange
passwordSet
createUser
updateUser
deleteUser
```

### List Password Characteristics

List all available password characteristics for this connector. On Active Directory for example this would list all of the *Password Policies*.

```
GET /password-characteristics/<handle>
```


#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "The connector was opened."
    handle: "za123gq21j"
}
```

Text

```
200 OK

za123gq21j
```

The handle `sdfkljl43258u9` may then be used with with `/streams` API to retrieve the data. The stream response items will be in the same format as `GET /password-characteristics/default` below. 

#### Errors

JSON

```json
{
    success: false,
    message: "No such connector instance with handle <handle>"
    code: 404
}
```

Text

```
404 No such connector instance with handle <handle>
```


### Get Default Password Characteristics

Describe what is required for valid passwords for this type of connector, e.g. minimum length, minimum age, maximum age etc.

*The ID `default` is a special password characteristics name that will always exist if the connector supports password characteristics*

`GET /password-characteristics/<handle>/default`

#### Response

JSON

```json
{
    success: true,
    message: "ActiveDirectory password characteristics.",
    code: 200
    characteristics: {
        "veryStrongFactor": -1
        "minimumSize": 8
        "maximumSize": 20,
        "requiredMatches": 3,
        "minimumLowerCase": 2,
        "minimumUpperCase": 2,
        "minimumDigits": 2,
        "minimumSymbols": 1,
        "historySize": 10,
        "symbols": "!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\|`"
        "dictionaryWordsAllowed": false,
        "additionalAnalysis": false,
        "minStrength": 0.25
        "containUsername": false,
        "attributes": {
            "maxAge": 60,
            "minAge": 10
        }
    }
}
```

Text

```
200 OK

veryStrongFactor=1
minimumSize": 8
maximumSize": 20,
requiredMatches": 3,
minimumLowerCase": 2,
minimumUpperCase": 2,
minimumDigits": 2,
minimumSymbols": 1,
historySize": 10,
symbols=!"£$%^&*()-_=+[{]};:\'@#~,<.>/?\\|`
dictionaryWordsAllowed": false,
additionalAnalysis": false,
minStrength": 0.25
containUsername": false,
attributes.maxAge=60
attributes.minAge=10
```

### Logon

The connector may be capable of validating a username and password pair and able to produce a *Logon Session*.

```
POST /logon/<handle>
```

#### Fields


| *Name*      | *Description* |
| ----------- | ------------- |
| `username` | The username |
| `password` | The password |

#### Example

URL Encoded form fields.

```
POST /logon/sdfkljl43258u9

username=joe.b
password=53cR3t5qu1rre1
```

JSON

```
POST /logon/sdfkljl43258u9
{
    "username": "joe.b",
    "password": "53cR3t5qu1rre1"
}
```

#### Response

Success responses will either be `200` if the connector just validated the credentials and they were OK, or `201` if the connected validated the credentials and created a trackable session. In this case, a `sesssionHandle` will be returned as the response data. If a session is created, the `/logoff` call may be used to end the session.

##### With Session

JSON

```json
{
    success: true,
    code: 201
    message: "The credentials were validated as OK and a session was created."
    sessionHandle: "z4ty2lmn73"
}
```

Text

```
201 Created

sdfkljl43258u9
```
##### Without Session

```json
{
    success: true,
    code: 200
    message: "The credentials were validated as OK."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "Incorrect credentials"
    code: 404
}
```

Text

```
404 Incorrect credentials.
```

### Logoff

If the `/logon` call resulted in a *Session* being created, you will have a `sessionHandle`. If the target connector supports logging off, this call will action that. 

```
DELETE /logon/<sessionHandle>
```

#### Example

URL Encoded form fields.

```
DELETE /logon/sdfkljl43258u9
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "The session was logged off."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such session"
    code: 404
}
```

Text

```
404 No such session.
```

### Web Logon

Some connectors may support login via a browser, e.g. OAuth. 

```
GET /web-logon/<handle>?returnTo=<finalUrl>
```

#### Parameters

| *Name*      | *Description* |
| ----------- | ------------- |
| `redirectURI` | This is passed through the web authentication process until it is complete, at which point that client may choose to redirect to it. |

The I4J HTTP API will alter this `redirectURI` adding its own `webLogonHandle` parameter to it. The application can extract this parameter to get the handle back later on to complete this logon.

#### Example

URL Encoded form fields.

```
GET /web-logon/sdfkljl43258u9?returnTo=https%3A%2F%2Fmyserver%2Fauth-complete.html
```

**Note how the `returnTo` parameter is itself URL encoded**

#### Response

JSON

```json
{
    success: true,
    code: 302
    message: "The web logon session was started."
    location: "https://accounts.google.com/o/oauth2/v2/..........&redirectURI%3Dhttps%3A%2F%2Fmyserver%2Fauth-complete.html%3FwebLogonHandle%3Dui65cbas23"
}
```

Text

```
302 Found
Location: https://accounts.google.com/o/oauth2/v2/..........&redirectURI%3Dhttps%3A%2F%2Fmyserver%2Fauth-complete.html%3FwebLogonHandle%3Dui65cbas23
```

#### Errors

JSON

```json
{
    success: false,
    message: "Web logon not supported."
    code: 405
}
```

Text

```
405 Web logon not supported.
```

### Complete Web Logon

When the browser has redirected back to the client application, the application should call this to get details about the logon, whether it was successful. Other details may be available. It will also close the logon session. 

```
GET /complete-web-logon/<webLogonHandle>
```

#### Example

```
GET /complete-web-logon/ui65cbas23
```

#### Response

*Status* may be one of `COMPLETE`, `OPENED` or `STARTED`. *Return Status* may be one of `UNKNOWN`, `AUTHENTICATED` or `FAILED_TO_AUTHENTICATE`.

JSON

```json
{
    success: true,
    code: 200
    message: "The web logon session was started.",
    webLogon: {
        id: "ui65cbas23",
        username: "joe.b",
        state: "token_1234546",
        created: "1787838323000",
        status: "COMPLETE",
        returnStatus: "AUTHENTICATED"
    }
}
```

Text

```
302 Found
Location: https://accounts.google.com/o/oauth2/v2/..........&redirectURI%3Dhttps%3A%2F%2Fmyserver%2Fauth-complete.html%3FwebLogonHandle%3Dui65cbas23
```

#### Errors

JSON

```json
{
    success: false,
    message: No such web logon session."
    code: 404
}
```

Text

```
404 No such web logon session.
```

### Check Credentials

The connector may be capable of validating a username and password pair. Note, unlike `/logon`, a *Logon Session* will never be created.

```
POST /check-credentials/<handle>
```

#### Fields


| *Name*      | *Description* |
| ----------- | ------------- |
| `username` | The username |
| `password` | The password |

#### Example

URL Encoded form fields.

```
POST /check-credentials/sdfkljl43258u9

username=joe.b
password=53cR3t5qu1rre1
```

JSON

```
POST /check-credentials/sdfkljl43258u9
{
    "username": "joe.b",
    "password": "53cR3t5qu1rre1"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "The credentials were validated as OK."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "Incorrect credentials"
    code: 404
}
```

Text

```
404 Incorrect credentials.
```

### Change Password

Changing the password requires the users original password.

```
POST /change-password/<handle>/<name>
```

#### Fields


| *Name*      | *Description* |
| ----------- | ------------- |
| `currentPassword` | The new password |
| `password` | The new password |

#### Example

URL Encoded form fields.

```
POST /change-password/sdfkljl43258u9/joe.b

currentPassword=53cR3t5qu1rre1
password=5py5au5ag3
```

JSON

```
POST /change-password/sdfkljl43258u9/joe.b
{
    "currentPassword": "53cR3t5qu1rre1",
    "password": "5py5au5ag3"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "The credentials were updated."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "Incorrect credentials"
    code: 404
}
```

Text

```
404 Incorrect credentials.
```

### Set Password

Set the password does not require the original password, i.e. the is for *Password Resets*. It will almost certain  the account I4J is connecting to the target user database as will require high permission level.

```
POST /set-password/<handle>/<name>
```

#### Fields


| *Name*      | *Default* |*Description* |
| ----------- | ------------- |------------- |
| `password` | None | The new password | |
| `forceChangeAtNextLogon` | `true` | The user will be forced to change their password again at next logon |
| `passwordResetType` | `ADMINISTRATIVE` | `ADMINISTRATIVE` or `USER`. A hint as to the type of password reset this is (`USER` is an administrative user acting on behalf of a less privileged user). |

#### Example

URL Encoded form fields.

```
POST /set-password/sdfkljl43258u9/joe.b

password=5py5au5ag3
```

JSON

```
POST /change-password/sdfkljl43258u9/joe.b
{
    "password": "5py5au5ag3"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "The credentials were reset."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "Incorrect credentials"
    code: 404
}
```

Text

```
404 Incorrect credentials.
```
### List Identities

List all identities available to the connector with no additional filtering.

```
GET /identities/<handle>
```

#### Example

```
GET /identities/sdfkljl43258u9
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Listing users."
    handle: "3h6um216va"
}
```

Text

```
200 OK

3h6um216va
```

The handle `3h6um216va` may then be used with with `/streams` API to retrieve the data. The stream response items will be in the same format as `GET /identities/<name>` below. 

#### Errors

JSON

```json
{
    success: false,
    message: "No such connector instance with handle <handle>"
    code: 404
}
```

Text

```
404 No such connector instance with handle <handle>
```

### Count Identities

List all identities available to the connector with no additional filtering.

```
GET /identity-count/<handle>
```

#### Example

```
GET /identity-count/sdfkljl43258u9
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "There are 973 identities."
    users: 973
}
```

Text

```
200 OK

973
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such connector instance with handle <handle>"
    code: 404
}
```

Text

```
404 No such connector instance with handle <handle>
```

### Identity Exists

Check if a identity with the given name exists.

```
HEAD /identities/<handle>/<name>
```

#### Example

```
HEAD /identities/alice.z
```

#### Response

JSON

```json
{
    success: true,
    message: "Identity alice.z exists.",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "The identity alice.z does not exists"
    code: 404
}
```

Text

```
404 The identity alice.z does not exists
```

### Get Identity

Get an individual identity given its name.

```
GET /identities/<handle>/<name>
```

#### Parameters

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `withRoles` | `false` | Whether or not to include role details if the identity is part of any roles. Depending on connector may have a performance penalty.  |


#### Example

```
GET /identities/sdfkljl43258u9/joe.b?withRoles
```

#### Enums

`PasswordStatusType` can be one of `locked`, `upToDate`, `nearExpiry`, `expired`, `changeRequired`, `noChangeAllowed` or `neverExpires`.

`AccountStatusType` can be on of `locked`, `unlocked`, `expired` or  `disabled`.

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity joe.b."
    identity: {
        fullName: "Joe Z Bloggs",
        lastLogon: 1787838323000,
        passwordStatus: {
            expire: 1787839323000,
            lastChange: 1787839123000,
            unlocked: 0,
            type: "upToDate",
            warn: 0,
            disable: 0,
            needChange: false
        },
        roles: [
            {
                // See role details in /roles
            }
        ],
        accountStatus: {
            expire: 1787839323001,
            locked: 0,
            unlocked: 0,
            type: "unlocked",
            disabled: false
        },
        otherName: "Zaphod",
        addresses: {
            "email": "zeph@beeb.com"
            "mobile": "1234"
        },
        principal: {
            name, "joe.b",
            guid: "1ac5289e-9811-adbe-56ea-0000a12367e1"
            system: false,
            attributes: {
                "sAMAccountName": "JOEB",
                "displayName": "Joe Bloggs",
                "givenName": "Joe",
                "sn": "Bloggs",
                "initials": "Z",
                "description": "He's just this guy, you know",
                "cn": "Joe Bloggs",
                "memberOf": ["CN=Group_A,OU=Stuf,DC=lan,DC=acme","CN=Group_B,OU=Stuff,DC=lan,DC=acme"]
            }
        }
    }
}
```

Text

```
200 OK

fullName=Joe Z Bloggs
lastLogon=1787838323000
passwordStatus.expire=1787839323000
passwordStatus.lastChange=1787839123000
passwordStatus.unlocked=0
passwordStatus.type=upToDate
passwordStatus.warn=0
passwordStatus.disable=0
passwordStatus.needChange=false
accountStatus.expire=1787839323001
accountStatus.locked=0
accountStatus.unlocked=0
accountStatus.type=unlocked
accountStatus.disabled=false
# TODO
role[0].XXXXX=VVVVVVVV
role[0].YYYYY=ZZZZZZZZ
role[1].XXXXX=XXXXXXXX
role[1].RRRRR=SSSSSSSS
otherName=Zaphod
addresses.email=zeph@beeb.com
addresses.mobile=1234
principal.name=joe.b
principal.guid=1ac5289e-9811-adbe-56ea-0000a12367e1
principal.system=false
principal.attributes.sAMAccountName=JOEB
principal.attributes.displayName=Joe Bloggs
principal.attributes.givenName=Joe
principal.attributes.sn=Bloggs
principal.attributes.initials=Z
principal.attributes.description=He's just this guy, you know
principal.attributes.cn=Joe Bloggs
principal.attributes.memberOf[0]=CN=Group_A,OU=Stuf,DC=lan,DC=acme
principal.attributes.memberOf[1]=CN=Group_B,OU=Stuff,DC=lan,DC=acme
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such identity with name <name>"
    code: 404
}
```

Text

```
404 No such identity with name <name>
```

### Get Identity By Its GUID

When support you can lookup a user by it's *GUID*. This could be anything, a GUID for AD, a UID for UNIX, whatever.


```
GET /identities-by-guid/<handle>/<guid>
```

#### Parameters

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `withRoles` | `false` | Whether or not to include role details if the role is part of other roles. Depending on connector may have a performance penalty.  |


#### Example

```
GET /identities-by-guid/sdfkljl43258u9/1ac5289e-9811-adbe-56ea-0000a12367e
```

#### Response

See *Get Identity*.

### Create Identity

Create a new identity.

```
POST /identities/<handle>/<name>
```

#### Fields

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `password` | *None* | The identity's initial password |
| `fullName` | *None* | The identity's full name |
| `otherName` | *None* | The identity's alias |
| `forcePasswordChangeAtNextLogin` | `true` (when supported)| Whether to force a password change at next login. |
| `principal.attributes.<attr>` | *None* | Connector specific attributes (as many as are supported) |


#### Example

##### Form URL Encoded

```
POST /identities/sdfkljl43258u9/bob.a

password=changeme
fullName=Bob Anderson
```

##### JSON

```json
POST /identities/sdfkljl43258u9/bob.a

{
    "password" : "changeme",
    "fullName" : "Bob Anderson"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name>."
    identity: {
        // See Get Identity response
    }
}
```

Text

```
200 OK

# See Get Identity Response
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> already exists."
    code: 409
}
```

Text

```
409 <name> already exists.
```

### Delete Identity

Delete an existing identity.

```
DELETE /identities/<handle>/<name>
```

#### Example

```
DELETE /identities/sdfkljl43258u9/alice.a
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> deleted."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> does not exist."
    code: 404
}
```

Text

```
404 <name> does not exist.
```

### Update Identity

Update an existing identity.

```
PATCH /identities/<handle>/<name>
```

#### Fields

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `fullName` | *None* | The identity's full name |
| `otherName` | *None* | The identity's alias |
| `principal.attributes.<attr>` | *None* | Connector specific attributes (as many as are supported) |


#### Example

##### Form URL Encoded

```
PATCH /identities/sdfkljl43258u9/alice.a

fullName=Alice Anderson
```

##### JSON

```json
POST /identities/sdfkljl43258u9/alice.a

{
    "fullName" : "Alice Anderson"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> updated."
    identity: {
        // See Get Identity response
    }
}
```

Text

```
200 OK

# See Get Identity Response
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> does not exist."
    code: 404
}
```

Text

```
404 <name> does not exist.
```

### List Roles

List all roles available to the connector with no additional filtering.

```
GET /roles/<handle>
```

#### Example

```
GET /roles/sdfkljl43258u9
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Listing roles."
    handle: "au234iuq1n"
}
```

Text

```
200 OK

au234iuq1n
```

The handle `au234iuq1n` may then be used with with `/streams` API to retrieve the data. The stream response items will be in the same format as `GET /roles/<name>` below. 

#### Errors

JSON

```json
{
    success: false,
    message: "No such connector instance with handle <handle>"
    code: 404
}
```

Text

```
404 No such connector instance with handle <handle>
```

### Count Roles

List all roles available to the connector with no additional filtering.

```
GET /role-count/<handle>
```

#### Example

```
GET /role-count/sdfkljl43258u9
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "There are 11 roles."
    users: 11
}
```

Text

```
200 OK

11
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such connector instance with handle <handle>"
    code: 404
}
```

Text

```
404 No such connector instance with handle <handle>
```

### Role Exists

Check if a role with the given name exists.

```
HEAD /roles/<handle>/<name>
```

#### Example

```
HEAD /roles/staff
```

#### Response

JSON

```json
{
    success: true,
    message: "The role staff exists.",
    code: 200
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "The role staff does not exists"
    code: 404
}
```

Text

```
404 The role staff does not exists
```

### Get Role

Get an individual role given its name.

```
GET /roles/<handle>/<name>
```

#### Parameters

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| withGroups | false | Whether or not to include group details if the role is part of any other roles. Depending on connector may have a performance penalty.  |


#### Example

```
GET /identities/sdfkljl43258u9/staff
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Role staff."
    role: {
        roles: [
        ],
        principal: {
            name, "staff",
            guid: "89ae112e-4567-ce34-ffa1-0000bb56aae8"
            system: false,
            attributes: {
                "description": "Everyone who works here",
            }
        }
    }
}
```

Text

```
200 OK

principal.name=staff
principal.guid=89ae112e-4567-ce34-ffa1-0000bb56aae8
principal.system=false
principal.attributes.description=Everyone who works here
```

#### Errors

JSON

```json
{
    success: false,
    message: "No such identity with name <name>"
    code: 404
}
```

Text

```
404 No such identity with name <name>
```

### Get Role By Its GUID

When support you can lookup a user by it's *GUID*. This could be anything, a GUID for AD, a GID for UNIX, whatever.


```
GET /roles-by-guid/<handle>/<guid>
```

#### Parameters

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `withRoles` | `false` | Whether or not to include roles details if the role is part of any other roles. Depending on connector may have a performance penalty.  |


#### Example

```
GET /roles-by-guid/sdfkljl43258u9/89ae112e-4567-ce34-ffa1-0000bb56aae8
```

#### Response

See *Get Role*.

### Create Role

Create a new role.

```
POST /roles/<handle>/<name>
```

#### Fields

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `principal.attributes.<attr>` | *None* | Connector specific attributes (as many as are supported) |


#### Example

##### Form URL Encoded

```
POST /roles/sdfkljl43258u9/bob.a

principal.attributes.description=Everyone who works here full time
```

##### JSON

```json
POST /identities/sdfkljl43258u9/bob.a

{

    "principal.attributes.description" : "Everyone who works here full time"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Role <name> created."
    role: {
        // See Get Role response
    }
}
```

Text

```
200 OK

# See Get Role Response
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> already exists."
    code: 409
}
```

Text

```
409 <name> already exists.
```

### Delete Role

Delete an existing role.

```
DELETE /roles/<handle>/<name>
```

#### Example

```
DELETE /roles/sdfkljl43258u9/staff
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> deleted."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> does not exist."
    code: 404
}
```

Text

```
404 <name> does not exist.
```

### Update Role

Update an existing role.

```
PATCH /roles/<handle>/<name>
```

#### Fields

| *Name*      | *Default* | *Description* |
| ----------- | --------- | ------------- |
| `principal.attributes.<attr>` | *None* | Connector specific attributes (as many as are supported) |


#### Example

##### Form URL Encoded

```
PATCH /roles/sdfkljl43258u9/staff

description=Everyone who works where full time
```

##### JSON

```json
POST /identities/sdfkljl43258u9/staff

{
    "description": "Everyone who works where full time"
}
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Role <name> updated."
    identity: {
        // See Get Role response
    }
}
```

Text

```
200 OK

# See Get Role Response
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> does not exist."
    code: 404
}
```

Text

```
404 <name> does not exist.
```

### Lock Identity

Lock an identities account.

```
POST /locks/<handle>/<name>
```

#### Fields

None 

#### Example

```
POST /locks/sdfkljl43258u9/bob.a
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> locked."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> already locked."
    code: 409
}
```

Text

```
409 <name> already locked.
```

### Unlock Identity

Unlock an identities account.

```
DELETE /locks/<handle>/<name>
```

#### Fields

None 

#### Example

```
DELETE /locks/sdfkljl43258u9/bob.a
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> unlocked."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> not locked."
    code: 404
}
```

Text

```
404 <name> not locked.
```
### Enable Identity

Enable an identities account.

```
POST /enablement/<handle>/<name>
```

#### Fields

None 

#### Example

```
POST /enablement/sdfkljl43258u9/bob.a
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> enabled."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> already enabled."
    code: 409
}
```

Text

```
409 <name> already enabled.
```

### Disable Identity

Disable an identities account.

```
DELETE /enablement/<handle>/<name>
```

#### Fields

None 

#### Example

```
DELETE /enablement/sdfkljl43258u9/bob.a
```

#### Response

JSON

```json
{
    success: true,
    code: 200
    message: "Identity <name> disabled."
}
```

Text

```
200 OK
```

#### Errors

JSON

```json
{
    success: false,
    message: "<name> not disabled."
    code: 404
}
```

Text

```
404 <name> not disabled.
```