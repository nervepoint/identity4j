/*
 * FILE:        WindowsLiveLogin.java
 *
 * DESCRIPTION: Sample implementation of Web Authentication and Delegated 
 *              Authentication protocol in Java. Also includes trusted 
 *              sign-in and application verification sample 
 *              implementations.
 *
 * VERSION:     1.1
 *
 * Copyright (c) 2008 Microsoft Corporation.  All Rights Reserved.
 */

package com.identity4j.connector.office365.services.token.handler;

import java.util.*;
import java.lang.String;
import java.lang.StringBuilder;
import java.net.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.net.ssl.*;
import java.util.regex.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.*;

public class WindowsLiveLogin {

    /**
     * All fatal errors in this class will throw this exception
     * (WindowsLiveLogin.WLLException).
     */
    public static class WLLException extends RuntimeException { 
        public WLLException(String s) {
            super(s);
        }
    }

    private static boolean debug = false;

    /**
     * Stub implementation for logging errors. If you want to enable
     * debugging output, set this to true. You will be able to view
     * warnings from the Java Enterprise Edition (EE) Admin Console.
     */
    public static void setDebug(boolean debugFlag) {
        debug = debugFlag;
    }
    
    /**
     * Stub implementation for logging errors. By default, this
     * function does nothing if the debug flag has not been set with
     * setDebug. Otherwise, you will be able to view warnings from
     * the Java EE Admin Console.
     */
    private static void debug(String s) {
        if (debug && (!isVoid(s))) {
            String out = "\nWindows Live ID Authentication SDK ";
            out += s;
            System.err.println(out);
        }
    }

    /**
     * Stub implementation for handling a fatal error.
     */
    private static void fatal(String s) {
        debug(s);
        throw new WLLException(s);    
    }

    /**
     * Initialize the WindowsLiveLogin module with the
     * application ID and secret key.
     * <p>
     * We recommend that you employ strong measures to protect
     * the secret key. The secret key  should never be
     * exposed to the Web or other users.
     */
    public WindowsLiveLogin(String appId, String secret) {
        this(appId, secret, null);
    }
        
    /**
     * Initialize the WindowsLiveLogin module with the
     * application ID, secret key, and security algorithm to use.
     * <p>
     * We recommend that you employ strong measures to protect
     * the secret key. The secret key should never be
     * exposed to the Web or other users.
     */
    public WindowsLiveLogin(String appId, String secret, String securityAlgorithm) {
        this(appId, secret, securityAlgorithm, false);
    }

    /**
     * Initialize the WindowsLiveLogin module with the
     * forceDelAuthNonProvisioned flag, policy URL, and return URL to use.
     * <p>
     * The 'force_delauth_nonprovisioned' flag also indicates whether
     * your application is registered for Delegated Authentication 
     * (that is, whether it uses an application ID and secret key). We 
     * recommend that your Delegated Authentication application always 
     * be registered for enhanced security and functionality.
     */
    public WindowsLiveLogin(boolean forceDelAuthNonProvisioned, String policyUrl, String returnUrl) {
        setForceDelAuthNonProvisioned(forceDelAuthNonProvisioned);
        setPolicyUrl(policyUrl);
        setReturnUrl(returnUrl);
    }

    /**
     * Initialize the WindowsLiveLogin module with the
     * application ID, secret key, security algorithm and 
     * forceDelAuthNonProvisioned to use.
     * <p>
     * We recommend that you employ strong measures to protect
     * the secret key. The secret key should never be
     * exposed to the Web or other users.
     * <p>
     * The 'force_delauth_nonprovisioned' flag also indicates whether
     * your application is registered for Delegated Authentication 
     * (that is, whether it uses an application ID and secret key). We 
     * recommend that your Delegated Authentication application always 
     * be registered for enhanced security and functionality.
     */
    public WindowsLiveLogin(String appId, String secret, String securityAlgorithm, boolean forceDelAuthNonProvisioned) {
        this(appId, secret, securityAlgorithm, forceDelAuthNonProvisioned, null);
    }

    /**
     * Initialize the WindowsLiveLogin module with the
     * application ID, secret key, and security algorithm to use.
     * <p>
     * We recommend that you employ strong measures to protect
     * the secret key. The secret key should never be
     * exposed to the Web or other users.
     * <p>
     * For Delegated Authentication, you may optionally specify the
     * privacy policy URL.
     * <p>
     * The 'force_delauth_nonprovisioned' flag also indicates whether
     * your application is registered for Delegated Authentication 
     * (that is, whether it uses an application ID and secret key). We 
     * recommend that your Delegated Authentication application always 
     * be registered for enhanced security and functionality.
     */
    public WindowsLiveLogin(String appId, String secret, String securityAlgorithm, boolean forceDelAuthNonProvisioned, String policyUrl) {
        this(appId, secret, securityAlgorithm, forceDelAuthNonProvisioned, policyUrl, null);
    }

    /**
     * Initialize the WindowsLiveLogin module with the
     * application ID, secret key, and security algorithm to use.
     * <p>
     * We recommend that you employ strong measures to protect
     * the secret key. The secret key should never be
     * exposed to the Web or other users.
     * <p>
     *  For Delegated Authentication, you may optionally specify the
     *  privacy policy URL and return URL. If you do not specify these
     *  values here, the default values that you specified when you
     *  registered your application will be used.  
     * <p>
     *  The 'force_delauth_nonprovisioned' flag also indicates whether
     *  your application is registered for Delegated Authentication 
     *  (that is, whether it uses an application ID and secret key). We 
     *  recommend that your Delegated Authentication application always 
     *  be registered for enhanced security and functionality.
     */
    public WindowsLiveLogin(String appId, String secret, String securityAlgorithm, boolean forceDelAuthNonProvisioned, String policyUrl, String returnUrl) {    
        setForceDelAuthNonProvisioned(forceDelAuthNonProvisioned);        
        setAppId(appId);
        setSecret(secret);
        setSecurityAlgorithm(securityAlgorithm);
        setPolicyUrl(policyUrl);
        setReturnUrl(returnUrl);
    }

    /**
     * Initialize the WindowsLiveLogin module from a settings file. 
     * <p>
     * 'settingsFile' specifies the location of the XML settings file
     * containing the application ID, secret key, and an optional security
     * algorithm. The file is of the following format:
     * <p>
     * <pre>
     * &lt;windowslivelogin&gt;
     *   &lt;appid&gt;APPID&lt;/appid&gt;
     *   &lt;secret&gt;SECRET&lt;/secret&gt;
     *   &lt;securityalgorithm&gt;wsignin1.0&lt;/securityalgorithm&gt;
     * &lt;/windowslivelogin&gt;
     * </pre>
     * <p>
     * In a Delegated Authentication scenario, you may also specify
     * 'returnurl' and 'policyurl' in the settings file, as shown in the
     * Delegated Authentication samples.
     * <p>
     *  We recommend that you store the WindowsLiveLogin settings file
     *  in an area on your server that cannot be accessed through the 
     *  Internet. This file contains important confidential information.
     */
    public WindowsLiveLogin(String settingsFile) {
        Map<String, String> settings = parseSettings(settingsFile);

        if ("true".equals(settings.get("debug"))) {
            setDebug(true);
        } else {
            setDebug(false);
        }

        if ("true".equals(settings.get("force_delauth_nonprovisioned"))) {
            setForceDelAuthNonProvisioned(true);
        } else {
            setForceDelAuthNonProvisioned(false);
        }

        setAppId(settings.get("appid"));
        setSecret(settings.get("secret"));
        setOldSecret(settings.get("oldsecret"));
        setOldSecretExpiry(settings.get("oldsecretexpiry"));
        setSecurityAlgorithm(settings.get("securityalgorithm"));
        setPolicyUrl(settings.get("policyurl"));
        setReturnUrl(settings.get("returnurl"));
        setBaseUrl(settings.get("baseurl"));
        setSecureUrl(settings.get("secureurl"));
        setConsentBaseUrl(settings.get("consenturl"));
    }

    /**
     * Initialize the WindowsLiveLogin module. You will have to
     * manually set the application ID, secret key, and security algorithm
     * using the appropriate setters as desired.
     */
    public WindowsLiveLogin() { }

    private String appId;

    /**
     * Sets the application ID. Use this method if you did not specify
     * an application ID at initialization.
     **/
    public void setAppId(String appId)
    {
        if (isVoid(appId)) {
            if (forceDelAuthNonProvisioned) {
                return;
            }
                
            fatal("Error: setAppId: Attempt to set null application ID.");
        }
        
        Pattern p = Pattern.compile("^\\w+$");
        Matcher m = p.matcher(appId);
        if (!m.matches()) {
            fatal("Error: setAppId: Application ID must be alphanumeric: " 
                  + appId);
        }

        this.appId = appId;
    }

    /**
     * Returns the application ID.
     */
    public String getAppId() {
        if (isVoid(appId)) {
            fatal("Error: getAppId: Application ID was not set. Aborting.");
        }
        return appId;
    }

    private byte[] cryptKey;
    private byte[] signKey;

    /**
     * Sets your secret key. Use this method if you did not specify
     * a secret key at initialization.
     */
    public void setSecret(String secret)
    {
        if (isVoid(secret)|| secret.length() < 16) {
            if (forceDelAuthNonProvisioned) {
                return;
            }
            
            fatal("Error: setSecret: Secret key is expected to be non-null and longer than 16 characters.");
        }
        
        signKey  = derive(secret, "SIGNATURE");
        cryptKey = derive(secret, "ENCRYPTION");
    }

    private byte[] oldCryptKey;
    private byte[] oldSignKey;

    /**
     * Sets your old secret key.
     * <p>
     * Use this property to set your old secret key if you are in the
     * process of transitioning to a new secret key. You may need this 
     * property because the Windows Live ID servers can take up to 
     * 24 hours to propagate a new secret key after you have updated 
     * your application settings.
     * <p>
     * If an old secret key is specified here and has not expired
     * (as determined by the oldsecretexpiry setting), it will be used
     * as a fallback if token decryption fails with the new secret 
     * key.
     */
    public void setOldSecret(String secret) {
        if (isVoid(secret)) {
            return;
        }

        if (secret.length() < 16) {
            fatal("Error: setOldSecret: Secret key is expected to be non-null and longer than 16 characters.");
        }

        oldSignKey = derive(secret, "SIGNATURE");
        oldCryptKey = derive(secret, "ENCRYPTION");
    }
                    
    private Date oldSecretExpiry;

    /**
     * Sets the expiry time for your old secret key.
     *
     * After this time has passed, the old secret key will no longer be
     * used even if token decryption fails with the new secret key.
     *
     * The old secret expiry time is represented as the number of seconds
     * elapsed since January 1, 1970. 
     */
    public void setOldSecretExpiry(String timestamp) {
        if (isVoid(timestamp)) {
            return;
        }

        try {
            long timestampLong = Long.parseLong(timestamp);
            this.oldSecretExpiry = new Date(timestampLong * 1000);
        } catch (Exception e) {
            fatal("Error: setOldSecretExpiry: Invalid timestamp: " + timestamp);
        }
    }

    /**
     * Gets the old secret key expiry time.
     */
    public Date getOldSecretExpiry() {
        return oldSecretExpiry;
    }

    private String securityAlgorithm;

    /**
     * Sets the version of the security algorithm being used.
     */
    public void setSecurityAlgorithm(String securityAlgorithm)
    {
        this.securityAlgorithm = securityAlgorithm;
    }

    /**
     * Gets the version of the security algorithm being used.
     */
    public String getSecurityAlgorithm() {
        if (isVoid(securityAlgorithm)) {
            return "wsignin1.0";
        }
        return securityAlgorithm;
    }

    /**
     * Sets a flag that indicates whether Delegated Authentication
     * is non-provisioned (i.e. does not use an application ID or secret
     * key).
     */
    private boolean forceDelAuthNonProvisioned = false;

    public void setForceDelAuthNonProvisioned(boolean forceDelAuthNonProvisioned) {
        this.forceDelAuthNonProvisioned = forceDelAuthNonProvisioned;
    }

    private String policyUrl;

    /**
     * Sets the privacy policy URL if you did not provide one at initialization time.
     */
    public void setPolicyUrl(String policyUrl) {
        if (isVoid(policyUrl)) {
            if (forceDelAuthNonProvisioned) {
                fatal("Error: setPolicyUrl: Null policy URL given.");
            }
        }
        
        this.policyUrl = policyUrl;
    }

    /**
     * Gets the privacy policy URL for your site.
     */
    public String getPolicyUrl() {
        if (isVoid(policyUrl)) {
            debug("Warning: In the initial release of Delegated Auth, a Policy URL must be configured in the SDK for both provisioned and non-provisioned scenarios.");
            
            if (forceDelAuthNonProvisioned) {
                fatal("Error: getPolicyUrl: Policy URL must be set in a Delegated Auth non-provisioned scenario. Aborting.");
            }
        }

        return policyUrl;
    }

    private String returnUrl;

    /**
     * Sets the return URL--the URL on your site to which the consent 
     * service redirects users (along with the action, consent token, 
     * and application context) after they have successfully provided 
     * consent information for Delegated Authentication. This value will 
     * override the return URL specified during registration.
     */
    public void setReturnUrl(String returnUrl) {
        if (isVoid(returnUrl)) {
            if (forceDelAuthNonProvisioned) {
                fatal("Error: setReturnUrl: Null return URL given.");
            }
        }
        
        this.returnUrl = returnUrl;
    }

    /**
     * Returns the return URL of your site.
     */
    public String getReturnUrl() {
        if (isVoid(returnUrl)) {
            if (forceDelAuthNonProvisioned) {
                fatal("Error: getReturnUrl: Return URL must be set in a Delegated Auth non-provisioned scenario. Aborting.");
            }
        }

        return returnUrl;
    }

    private String baseUrl;

    /**
     * Sets the base URL to use for the Windows Live Login server. 
     * You should not have to change this property. Furthermore, we recommend 
     * that you use the Sign In control instead of the URL methods
     * provided here.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Gets the base URL to use for the Windows Live Login server. 
     * You should not have to use this property. Furthermore, we recommend 
     * that you use the Sign In control instead of the URL methods 
     * provided here.
     */
    public String getBaseUrl() {
        if (isVoid(baseUrl)) {
            return "http://login.live.com/";
        }

        return baseUrl;
    }

    private String secureUrl;

    /**
     * Sets the secure (HTTPS) URL to use for the Windows Live Login 
     * server. You should not have to change this property.
     */
    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    /**
     * Gets the secure (HTTPS) URL to use for the Windows Live Login 
     * server. You should not have to use this functon directly.
     */
    public String getSecureUrl() {
        if (isVoid(secureUrl)) {
            return "https://login.live.com/";
        }

        return secureUrl;
    }

    private String consentUrl;

    /**
     * Sets the Consent Base URL to use for the Windows Live Consent 
     * server. You should not have to use or change this property directly.
     */
    public void setConsentBaseUrl(String consentUrl) {
        this.consentUrl = consentUrl;
    }

    /**
     * Gets the URL to use for the Windows Live Consent server. You
     * should not have to use or change this directly.
     */
    public String getConsentBaseUrl() {
        if (isVoid(consentUrl)) {
            return "https://consent.live.com/";
        }

        return consentUrl;
    }

    /* Methods for Web Authentication support. */

    /**
     * Returns the sign-in URL to use for the Windows Live Login server. 
     * We recommend that you use the Sign In control instead.
     */
    public URL getLoginUrl() {
        return getLoginUrl(null);
    }

    /**
     * Returns the sign-in URL to use for the Windows Live Login server. 
     * We recommend that you use the Sign In control instead.
     * <p>  
     * 'context' will be returned as-is in the sign-in response for 
     * site-specific use.     
     */
    public URL getLoginUrl(String context) {
        return getLoginUrl(context, null);
    }

    /**
     * Returns the sign-in URL to use for the Windows Live Login server,
     * for a specified market. 
     * We recommend that you use the Sign In control instead.
     * <p>  
     * If you specify it, 'context' will be returned as-is in the sign-in
     * response for site-specific use.     
     */
    public URL getLoginUrl(String context, String market) {           
        String url = getBaseUrl();
        url += "wlogin.srf?appid=" + getAppId();
        url += "&alg=" + getSecurityAlgorithm();

        if (!isVoid(context)) {
            url += "&appctx=" + escape(context);
        }
        
        if (!isVoid(market)) {
            url += "&mkt=" + escape(market);
        }

        try {
            return new URL(url);
        } catch (Exception e) {
            debug("Error: getLoginUrl: Unable to create login URL: " + url 
                  + ": " + e);
        }

        return null;
    }

    /**
     * Returns the sign-out URL to use for the Windows Live Login server. 
     * We recommend that you use the Sign In control instead.
     */
    public URL getLogoutUrl() {
        return getLogoutUrl(null);
    }

    /**
     * Returns the sign-out URL to use for the Windows Live Login server,
     * for a specified market.
     * We recommend that you use the Sign In control instead.
     */
    public URL getLogoutUrl(String market) {        
        String url = getBaseUrl();

        url += "logout.srf?appid=" + getAppId();

        if (!isVoid(market)) {
            url += "&mkt=" + escape(market);
        }

        try {
            return new URL(url);
        } catch (Exception e) {
            debug("Error: getLogoutUrl: Unable to create logout URL: " + url
                  + ": " + e);
        }

        return null;
    }

    /**
     * Holds the user information after a successful sign-in.
     */
    public static class User {

        /**
        * Initialize the User with time stamp, userid, flags, context and token.
        */
        public User(String timestamp, String id, String flags, String context, String token) {
            setTimestamp(timestamp);
            setId(id);
            setFlags(flags);
            setContext(context);
            setToken(token);
        }

        private Date timestamp;

        /**
         * Returns the Unix timestamp as obtained from the SSO token.
         */
        public Date getTimestamp() {
            return timestamp;
        }

        /**
         * Sets the Unix timestamp.
         */
        private void setTimestamp(String timestamp) {
            if (isVoid(timestamp)) {
                throw new WLLException("Error: User: Null timestamp in token.");
            }

            long timestampLong;

            try {
                timestampLong = Long.parseLong(timestamp);
            } catch (Exception e) {
                throw new WLLException("Error: User: Invalid timestamp: "
                                       + timestamp);
            }

            this.timestamp = new Date(timestampLong * 1000);
        }

        private String id;

        /**
         * Returns the pairwise unique ID for the user.
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the pairwise unique ID for the user.
         */
        private void setId(String id) {
            if (isVoid(id)) {
                throw new WLLException("Error: User: Null id in token.");
            }

            Pattern p = Pattern.compile("^\\w+$");
            Matcher m = p.matcher(id);
            if (!m.matches()) {
                throw new WLLException("Error: User: Invalid id: " + id);
            }

            this.id = id;
        }


        private boolean usePersistentCookie;

        /**
         * Indicates whether the application is expected to store the
         * user token in a session or persistent cookie.
         */
        public boolean usePersistentCookie() {
            return usePersistentCookie;
        }

        /**
         * Sets the usePersistentCookie flag for the user.
         */
        private void setFlags(String flags) {
            this.usePersistentCookie = false;
            if (!isVoid(flags)) {
                try {
                    int flagsInt = Integer.parseInt(flags);
                    this.usePersistentCookie = ((flagsInt % 2) == 1);
                } catch(Exception e) { 
                    throw new WLLException("Error: User: Invalid flags: " + flags);
                }
            }
        }

        private String context;

        /** 
         * Returns the application context that was originally passed
         * to the sign-in request, if any.
         */
        public String getContext() {
            return context;
        }

        /**
         * Sets the the Application context.
         */
        private void setContext(String context) {
            this.context = context;
        }

        private String token;

        /**
         * Returns the encrypted Web Authentication token containing 
         * the UID. This can be cached in a cookie and the UID can be
         * retrieved by calling the ProcessToken method.
         */
        public String getToken() {
            return token;
        }

        /**
         * Sets the the User token.
         */
        private void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * Processes the sign-in response from the Windows Live Login server.
     * 
     * @param query Contains the preprocessed POST query, a map of
     *              Strings to an an array of Strings, such as that 
     *              returned by ServletRequest.getParameterMap().
     * @return      A User object on successful sign-in; otherwise null.
     */
    public User processLogin(Map<String, String[]> query) {
        if (query == null) {
            debug("Error: processLogin: Invalid query map.");
            return null;
        }

        String[] values = query.get("action");
        if ((values == null) || (values.length != 1)) {
            debug("Warning: processLogin: Single action parameter not found.");
            return null;            
        }
        String action = values[0];

        if (!"login".equals(action)) {
            debug("Warning: processLogin: query action ignored: " + action);
            return null;
        }

        values = query.get("stoken");
        if ((values == null) || (values.length != 1)) {
            debug("Warning: processLogin: Single stoken parameter not found.");
            return null;            
        }
        String token = values[0];

        String context = null;
        values = query.get("appctx");
        if ((values != null) && (values.length == 1)) {
            context = values[0];
            context = escape(context);
        }

        return processToken(token, context);
    }

    /**
     * Decodes and validates a Web Authentication token. Returns a User object
     * on success.
     */
    public User processToken(String token) {    
        return processToken(token, null);
    }

    /**
     * Decodes and validates a Web Authentication token. Returns a User 
     * object on success. If a context is passed in, it will be returned 
     * as the context field in the User object.
     */
    public User processToken(String token, String context) {
        if (isVoid(token)) {
            debug("Error: processToken: Invalid token specified.");
            return null;
        }

        String decodedToken = decodeAndValidateToken(token);

        if (isVoid(decodedToken)) {
            debug("Error: processToken: Failed to decode/validate token: " 
                  + token);
            return null;
        }

        Map<String, String> parsedToken = parse(decodedToken);

        if ((parsedToken == null) || (parsedToken.size() < 3)) {
            debug("Error: processToken: Failed to parse token after decoding: " + token);
            return null;
        }

        String appId = getAppId();
        String tokenAppId = parsedToken.get("appid");

        if (!appId.equals(tokenAppId)) {
            debug("Error: processToken: Application ID in token did not match ours: " + tokenAppId +  ", " + appId);
            return null;
        }
        
        User user = null;
        
        try {
            user = new User(parsedToken.get("ts"), 
                            parsedToken.get("uid"),
                            parsedToken.get("flags"),
                            context, token);
        } catch (WLLException e) {
            debug("Error: processToken: Contents of token considered invalid: " + e);
        }
        
        return user;
    }

    /**
     * Returns an appropriate content type and body response that the 
     * application handler can return to signify a successful sign-out 
     * from the application.
     * <p>
     * When a user signs out of Windows Live or a Windows Live
     * application, a best-effort attempt is made at signing the user out
     * from all other Windows Live applications the user might be signed
     * in to. This is done by calling the handler page for each
     * application with 'action' set to 'clearcookie' in the query
     * string. The application handler is then responsible for clearing
     * any cookies or data associated with the sign-in. After successfully
     * signing the user out, the handler should return a GIF (any GIF)
     * image as response to the 'action=clearcookie' query.
     * <p>
     * @see #getClearCookieResponseBody()
     */
    public String getClearCookieResponseType() {
        return "image/gif";
    }

    /**
     * Returns an appropriate content body for the response that 
     * the application handler can return to signify a successful 
     * sign-out from the application.
     * <p> 
     * When a user signs out of Windows Live or a Windows Live
     * application, a best-effort attempt is made at signing out the
     * user from all other Windows Live applications the user might be
     * signed in to. This is done by calling the handler page for
     * each application with 'action' set to 'clearcookie' in the
     * query string. The application handler is then responsible for
     * clearing any cookies or data associated with the sign-in. After
     * successfully logging out the user, the handler should return a
     * GIF (any GIF) as response to the action=clearcookie query.

     *
     * @see #getClearCookieResponseType()
     */
    public byte[] getClearCookieResponseBody() {
        String gif = 
          "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAEALAAAAAABAAEAAAIBTAA7";
        return Base64.decode(gif);
    }

    /* Methods for Delegated Authentication support. */

    /*
     * Returns the consent URL to use for Delegated Authentication for
     * the given comma-delimited list of offers.
     */
    public URL getConsentUrl(String offers) {
        return getConsentUrl(offers, null);
    }

    /*
     * Returns the consent URL to use for Delegated Authentication for
     * the given comma-delimited list of offers.
     * <p>
     * If you specify it, 'context' will be returned as-is in the consent
     * response for site-specific use.
     */
    public URL getConsentUrl(String offers, String context) {
        return getConsentUrl(offers, context, null);
    }

    /*
     * Returns the consent URL to use for Delegated Authentication for
     * the given comma-delimited list of offers.
     * <p>
     * If you specify it, 'context' will be returned as-is in the consent
     * response for site-specific use.
     * <p>
     * The registered/configured return URL can also be overridden by 
     * specifying 'ru' here.
     */
    public URL getConsentUrl(String offers, String context, String ru) {
        return getConsentUrl(offers, context, ru, null);
    }

    /*
     * Returns the consent URL to use for Delegated Authentication for
     * the given comma-delimited list of offers.
     * <p>
     * If you specify it, 'context' will be returned as-is in the consent
     * response for site-specific use.
     * <p>
     * The registered/configured return URL can also be overridden by 
     * specifying 'ru' here.
     * <p>
     * You can change the language in which the consent page is displayed
     * by specifying a culture ID (For example, 'fr-fr' or 'en-us') in the
     * 'market' parameter.
     */
    public URL getConsentUrl(String offers, String context, String ru, String market) {
        if (isVoid(offers)) {
            throw new WLLException("Error: getConsentUrl: Invalid offers list.");
        }

        String url = getConsentBaseUrl() + "Delegation.aspx";

        url += "?ps=" + escape(offers);

        if (!isVoid(context)) {
            url += "&appctx=" + escape(context);
        }

        if (isVoid(ru)) {
            ru = getReturnUrl();
        }

        if (!isVoid(ru)) {
            url += "&ru=" + escape(ru);
        }
        
        if (!isVoid(market)) {
            url += "&mkt=" + escape(market);
        }

        String policyUrl = getPolicyUrl();

        if (!isVoid(policyUrl)) {
            url += "&pl=" + escape(policyUrl);
        }
        
        if (!forceDelAuthNonProvisioned) {
            url += "&app=" + getAppVerifier();
        }

        try {
            return new URL(url);
        } catch (Exception e) {
            throw new WLLException("Error: getConsentUrl: Unable to create consent URL: " + url + ": " + e);
        }
    }

    /*
     * Returns the URL to use to download a new consent token, given the 
     * offers and refresh token.
     */
    public URL getRefreshConsentTokenUrl(String offers, String refreshToken) {
        return getRefreshConsentTokenUrl(offers, refreshToken, null);
    }

    /*
     * Returns the URL to use to download a new consent token, given the 
     * offers and refresh token.
     * <p>
     * The registered/configured return URL can also be overridden by 
     * specifying 'ru' here. 
     */
    public URL getRefreshConsentTokenUrl(String offers, String refreshToken, 
                                         String ru) {
        if (isVoid(offers)) {
            throw new WLLException("Error: getRefreshConsentTokenUrl: Invalid offers list.");
        }

        if (isVoid(refreshToken)) {
            throw new WLLException("Error: getRefreshConsentTokenUrl: Invalid refresh token.");
        }

        String url = getConsentBaseUrl() + "RefreshToken.aspx";
        url += "?ps=" + escape(offers);
        url += "&reft=" + refreshToken;

        if (isVoid(ru)) {
            ru = getReturnUrl();
        }

        if (!isVoid(ru)) {
            url += "&ru=" + escape(ru);
        }
        
        if (!forceDelAuthNonProvisioned) {
            url += "&app=" + getAppVerifier();
        }

        try {
            return new URL(url);
        } catch (Exception e) {
            throw new WLLException("Error: getRefreshConsentTokenUrl: Unable to create refresh consent token URL: " + url + ": " + e);
        }
    }

    /*
     * Returns the URL for the consent-management user interface.
     */
    public URL getManageConsentUrl() {
        return getManageConsentUrl(null);
    }

    /*
     * Returns the URL for the consent-management user interface.
     * <p>
     * You can change the language in which the consent page is displayed
     * by specifying a culture ID (For example, 'fr-fr' or 'en-us') in the
     * 'market' parameter.
     */
    public URL getManageConsentUrl(String market) {
        String url = getConsentBaseUrl() + "ManageConsent.aspx";

        if (!isVoid(market)) {
            url += "?mkt=" + escape(market);
        }

        try {
            return new URL(url);
        } catch (Exception e) {
            throw new WLLException("Error: getManageConsentUrl: Unable to create manage consent URL: " + url + ": " + e);
        }
    }

    /**
     * Holds the Consent Token object corresponding to consent granted. 
     */
    public static class ConsentToken {
        private WindowsLiveLogin wll;

        /**
         * Initialize the ConsentToken module with the WindowsLiveLogin, 
         * delegation token, refresh token, session key, expiry, offers, 
         * location ID, context, decoded token, and raw token.
         */   
        public ConsentToken(WindowsLiveLogin wll, String delegationToken, String refreshToken, String sessionKey, String expiry, String offers, String locationID, String context, String decodedToken, String token) {
            this.wll = wll;
            setDelegationToken(delegationToken);
            setRefreshToken(refreshToken);
            setSessionKey(sessionKey);
            setExpiry(expiry);
            setOffers(offers);
            setLocationID(locationID);
            setContext(context);
            setDecodedToken(decodedToken);
            setToken(token);
        }

        private String delegationToken;

        /**
         * Gets the Delegation token.
         */
        public String getDelegationToken() {
            return delegationToken;
        }

        /**
         * Sets the Delegation token.
         */
        private void setDelegationToken(String delegationToken) {
            if (isVoid(delegationToken)) {
                throw new WLLException("Error: ConsentToken: Null delegation token.");
            }

            this.delegationToken = delegationToken;
        }

        /**
         * Gets the refresh token.
         */
        private String refreshToken;
        
        public String getRefreshToken() {
            return refreshToken;
        }

        /**
         * Sets the refresh token.
         */
        private void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
        
        private byte[] sessionKey;

        /**
         * Gets the session key.
         */
        public byte[] getSessionKey() {
            return sessionKey;
        }

        /**
         * Sets the session key.
         */
        private void setSessionKey(String sessionKey) {
            if (isVoid(sessionKey)) {
                throw new WLLException("Error: ConsentToken: Null session key.");
            }

            this.sessionKey = WindowsLiveLogin.u64(sessionKey);
        }

        private Date expiry;

        /**
         * Gets the expiry time of delegation token.
         */
        public Date getExpiry() {
            return expiry;
        }

        /**
         * Sets the expiry time of delegation token.
         */
        public void setExpiry(String expiry) {
            if (isVoid(expiry)) {
                throw new WLLException("Error: ConsentToken: Null expiry time.");
            }

            long expiryLong;

            try {
                expiryLong = Long.parseLong(expiry);
            } catch (Exception e) {
                throw new WLLException("Error: ConsentToken: Invalid expiry time: " + expiry);
            }

            this.expiry = new Date(expiryLong * 1000);
        }

        private List<String> offers;

        /**
         * Gets the list of offers/actions for which the user granted consent.
         */
        public List<String> getOffers() {
            return offers;
        }

        private String offersString;

        /**
         * Gets the string representation of all the offers/actions for which 
         * the user granted consent.
         */
        public String getOffersString() {
            return offersString;
        }

        /**
         * Sets the offers/actions for which user granted consent.
         */
        private void setOffers(String offers) {
            if (isVoid(offers)) {
                throw new WLLException("Error: ConsentToken: Null offers.");
            }

            offers = unescape(offers);

            this.offersString = "";
            this.offers = new ArrayList<String>();
            
            String[] offersList = offers.split(";");

            for (String offer : offersList) {
                if (!isVoid(this.offersString)) {
                    this.offersString += ",";
                }
                
                int separator = offer.indexOf(":");
                if (separator == -1) {
                    this.offers.add(offer);
                    this.offersString += offer;
                } else {
                    offer = offer.substring(0, separator);
                    this.offers.add(offer);
                    this.offersString += offer;
                }
                    
            }
        }

        private String locationID;

        /**
         * Gets the location ID.
         */       
        public String getLocationID() {
            return locationID; 
        }

        /**
        * Sets the location ID.
        */
        private void setLocationID(String locationID)
        {
            if (isVoid(locationID)) {
                throw new WLLException("Error: ConsentToken: Null Location ID.");
            }        
            this.locationID = locationID;
        }

        private String context;

        /**
         * Returns the application context that was originally passed
         * to the sign-in request, if any.
         */
        public String getContext() {
            return context;
        }

        /**
         * Sets the application context.
         */
        private void setContext(String context) {
            this.context = context;
        }

        String decodedToken;

        /**
         * Gets the decoded token.
         */
        public String getDecodedToken() {
            return decodedToken;
        }

        /**
         * Sets the decoded token.
         */
        private void setDecodedToken(String decodedToken) {
            this.decodedToken = decodedToken;
        }

        String token;

        /**
         * Gets the raw token.
         */
        public String getToken() {
            return token;
        }

        /**
         * Sets the raw token.
         */
        private void setToken(String token) {
            this.token = token;
        }

        /**
         * Indicates whether the delegation token is set and has not expired.
         */
        public boolean isValid() {
            if (isVoid(getDelegationToken())) {
                return false;
            }
            
            long now = (new Date()).getTime();
            long expiry = getExpiry().getTime();

            if ((now-300) > expiry) {
                return false;
            }

            return true;
        }

        /**
         * Refreshes the current token and replace it. If operation succeeds 
         * true is returned to signify success.
         */
        public boolean refresh() {
            ConsentToken ct = wll.refreshConsentToken(this);
            
            if (ct == null) {
                return false;
            }

            copy(ct);

            return true;
        }

        /**
         * Makes a copy of the ConsentToken object.
         */
        private void copy(ConsentToken consentToken) {
            this.delegationToken = consentToken.delegationToken;
            this.refreshToken = consentToken.refreshToken;
            this.sessionKey = consentToken.sessionKey;
            this.expiry = consentToken.expiry;
            this.offers = consentToken.offers;
            this.offersString = consentToken.offersString;
            this.locationID = consentToken.locationID;
            this.decodedToken = consentToken.decodedToken;
            this.token = consentToken.token;
        }
    }

    /*
     * Processes the POST response from the Delegated Authentication 
     * service after a user has granted consent. The processConsent
     * function extracts the consent token string and returns the result 
     * of invoking the processConsentToken method. 
     */
    public ConsentToken processConsent(Map<String, String[]> query) {
        if (query == null) {
            debug("Error: processConsent: Invalid query map.");
            return null;
        }

        String[] values = query.get("action");
        if ((values == null) || (values.length != 1)) {
            debug("Warning: processConsent: Single action parameter not found.");
            return null;
        }
        String action = values[0];

        if (!"delauth".equals(action)) {
            debug("Warning: processConsent: query action ignored: " + action);
            return null;
        }

        values = query.get("ResponseCode");
        if ((values == null) || (values.length != 1)) {
            debug("Warning: processConsent: Single ResponseCode parameter not found.");
            return null;            
        }
        String responseCode = values[0];

        if (!"RequestApproved".equals(responseCode)) {
            debug("Error: processConsent: Consent was not successfully granted: " 
                  + responseCode);
            return null;
        }

        values = query.get("ConsentToken");
        if ((values == null) || (values.length != 1)) {
            debug("Warning: processConsent: Single ConsentToken parameter not found.");
            return null;            
        }
        String token = values[0];

        String context = null;
        values = query.get("appctx");
        if ((values != null) && (values.length == 1)) {
            context = values[0];
            context = escape(context);
        }

        return processConsentToken(token, context);
    }

    /*
     * Processes the consent token string that is returned in the POST 
     * response by the Delegated Authentication service after a 
     * user has granted consent.
     */
    public ConsentToken processConsentToken(String token) {
        return processConsentToken(token, null);
    }

    /*
     * Processes the consent token string that is returned in the POST 
     * response by the Delegated Authentication service after a 
     * user has granted consent.
     * <p>
     * If you specify context, 'context' will be returned as-is in the 
     * ConsentToken.       
     */
    public ConsentToken processConsentToken(String token, String context) {
        String decodedToken = token;

        if (isVoid(token)) {
            debug("Error: processConsentToken: Null token.");
            return null;
        }
            
        Map<String, String> parsedToken = parse(unescape(token));

        if (parsedToken == null) {
            debug("Error: processConsentToken: Failed to parse token: " 
                  + token);
            return null;
        }
            
        if (!isVoid(parsedToken.get("eact"))) {
            decodedToken = decodeAndValidateToken(parsedToken.get("eact"));
            if (isVoid(decodedToken)) {
                debug("Error: processConsentToken: Failed to decode/validate token: " + token);
                return null;
            }
            
            parsedToken = parse(decodedToken);
            decodedToken = escape(decodedToken);
        }
            
        ConsentToken consentToken = null;
        
        try {
            consentToken = new ConsentToken(this, 
                                            parsedToken.get("delt"), 
                                            parsedToken.get("reft"),
                                            parsedToken.get("skey"),
                                            parsedToken.get("exp"),
                                            parsedToken.get("offer"), 
                                            parsedToken.get("lid"),
                                            context, decodedToken,
                                            token);
        } catch (WLLException e) {
            debug("Error: processConsentToken: Contents of token considered invalid: " + e);
        }

            return consentToken;
    }

    /*
     * Attempts to obtain a new, refreshed token and return it. The 
     * original token is not modified.
     */
    public ConsentToken refreshConsentToken(ConsentToken token) {
        return refreshConsentToken(token, null);
    }

    /*
     * Attempts to obtain a new, refreshed token and return it. The 
     * original token is not modified.
     * <p>
     * The registered/configured return URL can also be overridden by 
     * specifying 'ru' here. 
     */
    public ConsentToken refreshConsentToken(ConsentToken token, String ru) {
        if (token == null) {
            debug("Error: refreshConsentToken: Null consent token.");
            return null;
        }
        
        return refreshConsentToken(token.getOffersString(), 
                                   token.getRefreshToken(), ru);
    }

    /*
     * Attempts to obtain a new, refreshed token and return it using 
     * the offers and refresh token. The original token is not modified.
     */
    public ConsentToken refreshConsentToken(String offers, 
                                            String refreshToken) {
        return refreshConsentToken(offers, refreshToken, null);
    }

    /*
     * Attempts to obtain a new, refreshed token and return it using 
     * the offers and refresh token. The original token is not modified.
     * <p>
     * The registered/configured return URL can also be overridden by 
     * specifying 'ru' here. 
     */
    public ConsentToken refreshConsentToken(String offers, 
                                            String refreshToken, String ru) {
        URL url = null;
        
        try {
            url = getRefreshConsentTokenUrl(offers, refreshToken, ru);
        } catch (Exception e) {
            debug("Error: Failed to construct refresh consent token URL: " + e);
            return null;
        }
        
        if (url == null) {
            debug("Error: Failed to construct refresh consent token URL.");
            return null;
        }
            
        String body = fetch(url);
        
        if (isVoid(body)) {
            debug("Error: refreshConsentToken: Failed to download token.");
            return null;
        }   
        
        String re = "\\{\"ConsentToken\":\"(.*)\"\\}";
        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(body);

        if (!m.find()) {
            debug("Error: refreshConsentToken: Failed to extract token: " + body);
            return null;
        }
            
        return processConsentToken(m.group(1));
    }

    /* Common methods. */

    /*
     * Decodes and validates the raw token.
     */
    public String decodeAndValidateToken(String token) {
        boolean haveOldSecret = false;

        long now = (new Date()).getTime();
        long expiry = (oldSecretExpiry == null) ? 0 : oldSecretExpiry.getTime();

        if (now < expiry) {
            if ((oldCryptKey != null) && (oldSignKey != null)) {
                haveOldSecret = true;
            }
        }

        String stoken = decodeAndValidateToken(token, cryptKey, signKey);

        if (isVoid(stoken) && haveOldSecret) {
            debug("Warning: Failed to validate token with current secret, attempting old secret.");
            return decodeAndValidateToken(token, oldCryptKey, oldSignKey);
        }

        return stoken;
    }

    /*
     * Decodes and validates the raw token with appropriate crypt Key
     * and sign Key.
     */
    public String decodeAndValidateToken(String token, byte[] cryptKey, 
                                         byte[] signKey) {
        String stoken = decodeToken(token, cryptKey);

        if (!isVoid(stoken)) {
            stoken = validateToken(stoken, signKey);
        }

        return stoken;
    }


    /**
     * Decodes the given token. Returns null on failure.
     * <p>
     * <ul>
     * <li>First, the string is URL unescaped and base64 decoded.</li>
     * <li>Second, the IV is extracted from the first 16 bytes the
     * string.</li>
     * <li>Finally, the string is decrypted by using the encryption
     * key.</li>
     * </ul>
     */
    public String decodeToken(String token) {
        return decodeToken(token, cryptKey);
    }


    /**
     * Decodes the given token provided the crypt key. Returns null on failure.
     * <p>
     * <ul>
     * <li>First, the string is URL unescaped and base64 decoded.</li>
     * <li>Second, the IV is extracted from the first 16 bytes the
     * string.</li>
     * <li>Finally, the string is decrypted by using the encryption
     * key.</li>
     * </ul>
     */
    public String decodeToken(String token, byte[] cryptKey) {
        if ((cryptKey == null) || (cryptKey.length == 0)) {
            fatal("Error: decodeToken: Secret key was not set. Aborting.");
        }

        if (isVoid(token)) {
            debug("Error: decodeToken: Null token input.");
            return null;
        }

        try {
            int ivLen = 16;
            byte[] tokenBytes = u64(token);

            if ((tokenBytes == null) || (tokenBytes.length <= ivLen) 
                   || (tokenBytes.length % ivLen != 0)) {
                debug("Error: decodeToken: Attempted to decode invalid token.");
                return null;
            }

            byte[] iv        = Arrays.copyOf(tokenBytes, ivLen);
            byte[] crypted   = Arrays.copyOfRange(tokenBytes, ivLen, 
                                                  tokenBytes.length);
            
            SecretKeySpec  keySpec = new SecretKeySpec(cryptKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            String decrypted = new String(cipher.doFinal(crypted));
            return decrypted;
        } catch (Exception e) {
            debug("Error: decodeToken: Decryption failed: " + token + ", " 
                  + e);
        }

        return null;
    }

    /**
     * Creates a signature for the given string.
     */
    public byte[] signToken(String token) {
        return signToken(token, signKey);
    }


    /**
     * Creates a signature for the given string by using the signature
     * key.
     */
    public byte[] signToken(String token, byte[] signKey) {
        if ((signKey == null) || (signKey.length == 0)) {
            fatal("Error: signToken: Secret key was not set. Aborting.");
        }
        
        if (isVoid(token)) {
            debug("Attempted to sign null token.");
            return null;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(signKey, "AES");
            mac.init(keySpec);
            return mac.doFinal(token.getBytes());
        } catch (Exception e) {
            debug("Error: signToken: Signing failed: " + token + ", " + e);
        }

        return null;
    }

    /**
     * Extracts the signature from the token and validates it.
     */
    public String validateToken(String token) {
        return validateToken(token, signKey);
    }

    /**
     * Extracts the signature from the token and validates it by using the 
     * signature key.
     */
    public String validateToken(String token, byte[] signKey) {
        if (isVoid(token)) {
            debug("Error: validateToken: Null token.");
            return null;
        }
            
        String[] split = token.split("&sig=");

        if (split.length != 2)  {
            debug("Error: validateToken: Invalid token: " + token);
            return null;
        }

        byte[] sig  = u64(split[1]);
        if (sig == null) {
            debug("Error: validateToken: Could not extract the signature from the token.");
            return null;
        }

        byte[] sig2 = signToken(split[0], signKey);
        if (sig2 == null) {
            debug("Error: validateToken: Could not generate a signature for the token.");
            return null;
        }

        if (Arrays.equals(sig, sig2)) {
            return token;
        }    
        
        debug("Error: validateToken: Signature did not match.");
        return null;
    }

    /* Implementation of the methods needed to perform Windows Live
       application verification as well as trusted sign-in. */

    /**
     * Generates an Application Verifier token.
     */
    public String getAppVerifier() {
        return getAppVerifier(null);
    }

    /**
     * Generates an Application Verifier token. An IP address can be
     * included in the token.
     */
    public String getAppVerifier(String ip)
    {
        String token = "appid=" + getAppId() + "&ts=" + getTimestamp();

        if (!isVoid(ip)) {
            token += "&ip=" + ip;
        }

        token += "&sig=" + e64(signToken(token));
        return escape(token);
    }

    /**
     * Returns the URL needed to retrieve the application security
     * token. The application security token will be generated for
     * the Windows Live site.
     * <p>
     * JavaScript Output Notation (JSON) output is returned: 
     * <p>
     * {"token":"&lt;value&gt;"}
     */
    public URL getAppLoginUrl() {
        return getAppLoginUrl(null, null, false);
    }

    /**
     * Returns the URL needed to retrieve the application security
     * token.
     * <p>
     * By default, the application security token will be generated
     * for the Windows Live site; a specific Site ID can optionally be
     * specified in 'siteId'.
     * <p>
     * JSON output is returned: 
     * <p>
     * {"token":"&lt;value&gt;"}
     */
    public URL getAppLoginUrl(String siteId){
        return getAppLoginUrl(siteId, null, false);
    }

    /**
     * Returns the URL needed to retrieve the application security
     * token.
     * <p>
     * By default, the application security token will be generated
     * for the Windows Live site; a specific Site ID can optionally be
     * specified in 'siteId'. The IP address can also optionally be
     * included in 'ip'.
     * <p>
     * JSON output is returned: 
     * <p>
     * {"token":"&lt;value&gt;"}
     */
    public URL getAppLoginUrl(String siteId, String ip) {
        return getAppLoginUrl(siteId, ip, false);
    }

    /**
     * Returns the URL needed to retrieve the application security
     * token.
     * <p>
     * By default, the application security token will be generated
     * for the Windows Live site; a specific Site ID can optionally be
     * specified in 'siteId'. The IP address can also optionally be
     * included in 'ip'.
     * <p>
     * If 'js' is false, JSON output is returned: 
     * <p>
     * {"token":"&lt;value&gt;"}
     * <p>
     * Otherwise, a JavaScript response is returned. It is assumed that
     * WLIDResultCallback is a custom function implemented to handle
     * the token value:
     * <p>
     * WLIDResultCallback("&lt;tokenvalue&gt;");
     */
    public URL getAppLoginUrl(String siteId, String ip, boolean js) {
        String url = getSecureUrl();
        url += "wapplogin.srf?app=" + getAppVerifier(ip);
        url += "&alg=" + getSecurityAlgorithm();

        if (!isVoid(siteId)) {
            url += "&id=" + siteId;
        }

        if (js) {
            url += "&js=1";
        }

        try {
            return new URL(url);
        } catch (Exception e) {
            debug("Error: getAppLoginUrl: Could not create application login URL: " 
                  + url + ", " + e);
        }

        return null;
    }

    /**
     * Retrieves the application security token for application
     * verification from the application sign-in URL. The application
     * security token will be generated for the Windows Live site.
     */
    public String getAppSecurityToken() {
        return getAppSecurityToken(null, null);
    }

    /**
     * Retrieves the application security token for application
     * verification from the application sign-in URL.
     * <p>
     * By default, the application security token will be generated
     * for the Windows Live site; a specific Site ID can optionally be
     * specified in 'siteId'.
     */
    public String getAppSecurityToken(String siteId) {
        return getAppSecurityToken(siteId, null);
    }

    /**
     * Retrieves the application security token for application
     * verification from the application sign-in URL.
     * <p>
     * By default, the application security token will be generated
     * for the Windows Live site; a specific Site ID can optionally be
     * specified in 'siteId'. The IP address can also optionally be
     * included in 'ip'.
     * <p>
     * Implementation note: The application security token is
     * downloaded from the application sign-in URL in JSON format
     * {"token":"&lt;value&gt;"}, so we need to extract &lt;value&gt;
     * from the string and return it as seen here.
     */
    public String getAppSecurityToken(String siteId, String ip) {
        URL url = getAppLoginUrl(siteId, ip);

        if (url == null) {
            debug("Error: getAppSecurityToken: Could not get application sign-in URL to fetch security token");
            return null;
        }

        String body = fetch(url);
        if (isVoid(body)) {
            debug("Error: getAppSecurityToken: Could not fetch security token from URL: " + url);
            return null;
        }

        String regex = "\\{\"token\":\"(.*)\"\\}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(body);

        if (!m.find()) {
            debug("error: getAppSecurityToken: Failed to extract token: " + body);
            return null;
        }

        return m.group(1);
    }

    /**
     * Returns a string that can be passed to the getTrustedParams
     * function as the 'retcode' parameter. If this is specified as the
     * 'retcode', the application will be used as return URL after it
     * finishes trusted sign-in.
     */
    public String getAppRetCode() {
        return "appid=" + getAppId();
    }

    /**
     * Returns a table of key-value pairs that must be posted to the
     * sign-in URL for trusted sign-in. Use HTTP POST to do this. Be
     * aware that the values in the table are neither URL nor HTML
     * escaped and may have to be escaped if you are inserting them in
     * code such as an HTML form.
     * <p> 
     * User to be trusted on the local site is passed in as string
     * 'user'.
     **/
    public Map<String, String> getTrustedParams(String user) {
        return getTrustedParams(user, null);
    }

    /**
     * Returns a table of key-value pairs that must be posted to the
     * sign-in URL for trusted sign-in. Use HTTP POST to do this. Be
     * aware that the values in the table are neither URL nor HTML
     * escaped and may have to be escaped if you are inserting them in
     * code such as an HTML form.
     * <p>  
     * User to be trusted on the local site is passed in as string
     * 'user'.
     * <p>
     * Optionally, 'retcode' specifies the resource to which
     * successful sign-in is redirected, such as Windows Live Mail, and
     * is typically a string in the format 'id=2000'. If you pass in
     * the value from GetAppRetCode instead, sign-in will be redirected
     * to the application. Otherwise, an HTTP 200 response is
     * returned.
     */
    public Map<String, String> getTrustedParams(String user, String retcode) {
        String token = getTrustedToken(user);
        if (isVoid(token)) {
            return null;
        }
        token = "<wst:RequestSecurityTokenResponse xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"><wst:RequestedSecurityToken><wsse:BinarySecurityToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" + token + "</wsse:BinarySecurityToken></wst:RequestedSecurityToken><wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"><wsa:EndpointReference xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"><wsa:Address>uri:WindowsLiveID</wsa:Address></wsa:EndpointReference></wsp:AppliesTo></wst:RequestSecurityTokenResponse>";

        Map<String, String> params = new HashMap<String, String>();
        params.put("wa", getSecurityAlgorithm());
        params.put("wresult", token);
        if (!isVoid(retcode))
            params.put("wctx", retcode);
        return params;
    }

    /**
     * Returns the trusted sign-in token in the format needed by the
     * trusted sign-in gadget.
     * <p>
     * The user to be trusted on the local site is passed in as string
     * 'user'.
     */
    public String getTrustedToken(String user) {
        if (isVoid(user)) {
            debug("Error: getTrustedToken: Null user specified.");
            return null;
        }
            
        String token = "appid=" + getAppId() + "&uid=" + escape(user) 
          + "&ts=" + getTimestamp();        
        token += "&sig=" + e64(signToken(token));
        return escape(token);
    }

    /**
     * Returns the trusted sign-in URL to use for the Windows Live Login server.
     */
    public URL getTrustedLoginUrl() {           
        String url = getSecureUrl();
        url += "wlogin.srf";

        try {
            return new URL(url);
        } catch (Exception e) {
            debug("Error: getTrustedLoginUrl: Unable to create trusted sign-in URL: " 
                  + url + ": " + e);
        }

        return null;
    }

    /**
     * Returns the trusted sign-out URL to use for the Windows Live Login server.
     */
    public URL getTrustedLogoutUrl() {           
        String url = getSecureUrl();
        url += "logout.srf?appid=" + getAppId();

        try {
            return new URL(url);
        } catch (Exception e) {
            debug("Error: getTrustedLogoutUrl: Unable to create trusted sign-in URL: " 
                  + url + ": " + e);

        }

        return null;
    }
    
    /* Helper methods */

    /**
     * Function to parse the settings file.
     */
    private Map<String,String> parseSettings(String settingsFile) {
        try {
            InputStream settingsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(settingsFile);

            if (settingsStream == null) {
                fatal("Error: parseSettings: Could not load the settings file: " 
                      + settingsFile);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(settingsStream);

            NodeList nl = document.getElementsByTagName("windowslivelogin");

            if (nl.getLength() != 1) {
                fatal("Error: parseSettings: Failed to parse settings file: "
                      + settingsFile);
            }

            Node topNode = nl.item(0);
            nl = topNode.getChildNodes();
            Map<String,String> settings = new HashMap<String,String>();
            
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    settings.put(n.getNodeName(), 
                                 n.getFirstChild().getNodeValue());
                }
            }
            
            return settings;
        } catch (Exception e) {
            fatal("Error: parseSettings: Unable to load settings from: " 
                  + settingsFile + ": " + e);
        }

        return null;
   }

   /**
    * Derives the key, given the secret key and prefix as described in the
    * Web Authentication SDK documentation.
    */
    private byte[] derive(String secret, String prefix) {
        if(isVoid(secret) || isVoid(prefix)) {
            fatal("Error: derive: secret or prefix is null.");
        }        
            
        try {
            int keyLen = 16;
            String key = prefix + secret;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes());
            byte rv[] = Arrays.copyOf(digest, keyLen);
            return rv;
        } catch (Exception e) {
            fatal("Error: derive: Unable to derive key: " + e);
        }

        return null;
    }

    /**
     * Parses query string and return a table representation of the 
     * key and value pairs.
     */
    private static Map<String,String> parse(String input) {
        if (isVoid(input)) {
            debug("Error: parse: Null input.");
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        String[] pairs = input.split("&");

        for (String pair : pairs) {
            String[] kv = pair.split("=");

            if (kv.length != 2) {
                debug("Error: parse: Bad input passed to parse: " + input);
                return null;
            }

            map.put(kv[0], kv[1]);
        }

        return map;
    }

    /**
     * Generates a time stamp suitable for the application verifier 
     * token.
     */
    private static String getTimestamp() {
        Date now = new Date();
        return String.valueOf(now.getTime()/1000);
    }

    /**
     * Base64-encodes and URL-escapes a string.
     */
    private static String e64(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return escape(Base64.encodeBytes(bytes));
     }

     /**
      * URL-unescapes and Base64-decodes a string.
      */
    private static byte[] u64(String s) {
        if (s == null) {
            return null;
        }

        return Base64.decode(unescape(s));
    }

    /**
     * Fetches the contents given a URL.
     */
    private String fetch(URL url) {
        StringBuilder body = new StringBuilder();

        try {
            BufferedReader in = 
              new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                body.append(inputLine);
            }

            in.close();
            return body.toString();
        } catch (Exception e) {
            debug("Error: fetch: Exception reading URL: " + e);
        }

        return null;
    }

    /**
     * Verifies if string is null or empty.
     */
    private static boolean isVoid(String string) {
        if ((string == null) || (string.length() == 0)) {
            return true;
        }
        
        return false;
    }
      
    /**
     * URL-encodes a string.
     */
    public static String escape(String s) {
        if (s == null) {
            return null;
        }

        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
              debug("Error: escape: Unable to URL-encode string: " + e);
        }

        return null;
    }

    /**
     * URL-decodes a string.
     */
    public static String unescape(String s) {
        if (s == null) {
            return null;
        }

        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            debug("Error: unescape: Unable to URL-decode string: " + e);
        }

        return null;
    }
}
