package com.yeepay.yop.sdk.client;

import com.google.common.base.Joiner;
import com.yeepay.yop.sdk.Region;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.http.Protocol;
import com.yeepay.yop.sdk.http.RetryPolicy;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.yeepay.yop.sdk.YopConstants.YOP_HTTP_CLIENT_IMPL_DEFAULT;

/**
 * Basic client configurations for YOP clients.
 */
public class ClientConfiguration {

    /**
     * The default timeout for creating new connections.
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS = 10 * 1000;

    /**
     * The default timeout for request new connections form pool.
     */
    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS = 10 * 1000;

    /**
     * The default timeout for reading from a connected socket.
     */
    public static final int DEFAULT_SOCKET_TIMEOUT_IN_MILLIS = 50 * 1000;

    /**
     * The default max connection pool size.
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 200;

    /**
     * The default max connections per route
     */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 0;

    /**
     * The default User-Agent header value when sending requests to the target service. Initialized in the static
     * initializer.
     */
    public static final String DEFAULT_USER_AGENT;

    /**
     * The default region.
     */
    public static Region DEFAULT_REGION = Region.CN_N1;

    /**
     * The default protocol.
     */
    public static Protocol DEFAULT_PROTOCOL = Protocol.HTTP;

    /**
     * The User-Agent header value to use when sending requests to YOP services.
     */
    private String userAgent = ClientConfiguration.DEFAULT_USER_AGENT;

    /**
     * The retry policy for failed requests.
     */
    private RetryPolicy retryPolicy = RetryPolicy.DEFAULT_RETRY_POLICY;

    /**
     * The optional local address to bind when connecting to YOP services.
     */
    private InetAddress localAddress;

    /**
     * The protocol (HTTP/HTTPS) to use when connecting to YOP services.
     */
    private Protocol protocol = Protocol.HTTPS;

    /**
     * The optional proxy host the client will connect through.
     */
    private String proxyHost = null;

    /**
     * The optional proxy port the client will connect through.
     */
    private int proxyPort = -1;

    /**
     * The optional proxy scheme the client will connect through.
     */
    private String proxyScheme;

    /**
     * The optional user name to use when connecting through a proxy.
     */
    private String proxyUsername = null;

    /**
     * The optional password to use when connecting through a proxy.
     */
    private String proxyPassword = null;

    /**
     * The optional Windows domain to use when connecting through a NTLM proxy.
     */
    private String proxyDomain = null;

    /**
     * The optional Windows workstation to use when connecting through a NTLM proxy.
     */
    private String proxyWorkstation = null;

    /**
     * Whether to enable proxy preemptive authentication. If it is true, the client will send the basic authentication
     * response even before the proxy server gives an unauthorized response in certain situations, thus reducing the
     * overhead of making the connection.
     */
    private boolean proxyPreemptiveAuthenticationEnabled;

    /**
     * The maximum number of open HTTP connections.
     */
    private int maxConnections = ClientConfiguration.DEFAULT_MAX_CONNECTIONS;

    /**
     * The maximum number of per route.
     */
    private int maxConnectionsPerRoute = ClientConfiguration.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;

    /**
     * The socket timeout (SO_TIMEOUT) in milliseconds, which is a maximum period inactivity between two consecutive
     * data packets. A value of 0 means infinity, and is not recommended.
     */
    private int socketTimeoutInMillis = ClientConfiguration.DEFAULT_SOCKET_TIMEOUT_IN_MILLIS;

    /**
     * The connection timeout in milliseconds. A value of 0 means infinity, and is not recommended.
     */
    private int connectionTimeoutInMillis = ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS;

    /**
     * 从连接池获取到连接的超时时间
     */
    private int connectionRequestTimeoutInMillis = ClientConfiguration.DEFAULT_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS;

    /**
     * The optional size (in bytes) for the low level TCP socket buffer. This is an advanced option for advanced users
     * who want to tune low level TCP parameters to try and squeeze out more performance. Ignored if not positive.
     */
    private int socketBufferSizeInBytes = 0;

    /**
     * The service endpoint URL to which the client will connect.
     */
    private String endpoint = null;

    /**
     * The region of service. This value is used by the client to construct the endpoint URL automatically, and is
     * ignored if endpoint is not null.
     */
    private Region region = DEFAULT_REGION;

    /**
     * The YOP credentials used by the client to sign HTTP requests.
     */
    private YopCredentials<?> credentials = null;

    /**
     * The http client impl
     */
    private String clientImpl = YOP_HTTP_CLIENT_IMPL_DEFAULT;

    // Initialize DEFAULT_USER_AGENT
    static {
        String language = System.getProperty("user.language");
        if (language == null) {
            language = "";
        }
        String region = System.getProperty("user.region");
        if (region == null) {
            region = "";
        }
        DEFAULT_USER_AGENT = Joiner.on('/').join("java", YopConstants.VERSION, System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.version"), language, region)
                .replace(' ', '_');
    }

    /**
     * Constructs a new ClientConfiguration instance with default settings.
     */
    public ClientConfiguration() {
    }

    /**
     * Constructs a new ClientConfiguration instance with the same settings as the specified configuration.
     *
     * @param other the configuration to copy settings from.
     */
    public ClientConfiguration(ClientConfiguration other) {
        this.connectionTimeoutInMillis = other.connectionTimeoutInMillis;
        this.maxConnections = other.maxConnections;
        this.maxConnectionsPerRoute = other.maxConnectionsPerRoute;
        this.retryPolicy = other.retryPolicy;
        this.localAddress = other.localAddress;
        this.protocol = other.protocol;
        this.proxyDomain = other.proxyDomain;
        this.proxyHost = other.proxyHost;
        this.proxyPassword = other.proxyPassword;
        this.proxyPort = other.proxyPort;
        this.proxyScheme = other.proxyScheme;
        this.proxyUsername = other.proxyUsername;
        this.proxyWorkstation = other.proxyWorkstation;
        this.proxyPreemptiveAuthenticationEnabled = other.proxyPreemptiveAuthenticationEnabled;
        this.socketTimeoutInMillis = other.socketTimeoutInMillis;
        this.userAgent = other.userAgent;
        this.socketBufferSizeInBytes = other.socketBufferSizeInBytes;
        this.endpoint = other.endpoint;
        this.region = other.region;
        this.credentials = other.credentials;
        this.clientImpl = other.clientImpl;
    }

    /**
     * Constructs a new ClientConfiguration instance with the same settings as the specified configuration.
     * This constructor is used to create a client configuration from one SDK to another SDK. e.g. from VOD to YOS.
     * In this case endpoint should be changed while other attributes keep same.
     *
     * @param other    the configuration to copy settings from.
     * @param endpoint the endpoint
     */
    public ClientConfiguration(ClientConfiguration other, String endpoint) {
        this.endpoint = endpoint;
        this.connectionTimeoutInMillis = other.connectionTimeoutInMillis;
        this.maxConnections = other.maxConnections;
        this.retryPolicy = other.retryPolicy;
        this.localAddress = other.localAddress;
        this.protocol = other.protocol;
        this.proxyDomain = other.proxyDomain;
        this.proxyHost = other.proxyHost;
        this.proxyPassword = other.proxyPassword;
        this.proxyPort = other.proxyPort;
        this.proxyUsername = other.proxyUsername;
        this.proxyWorkstation = other.proxyWorkstation;
        this.proxyPreemptiveAuthenticationEnabled = other.proxyPreemptiveAuthenticationEnabled;
        this.socketTimeoutInMillis = other.socketTimeoutInMillis;
        this.userAgent = other.userAgent;
        this.socketBufferSizeInBytes = other.socketBufferSizeInBytes;
        this.region = other.region;
        this.credentials = other.credentials;
        this.clientImpl = other.clientImpl;
    }

    /**
     * Returns the protocol (HTTP/HTTPS) to use when connecting to YOP services.
     *
     * @return the protocol (HTTP/HTTPS) to use when connecting to YOP services.
     */
    public Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * Sets the protocol (HTTP/HTTPS) to use when connecting to YOP services.
     *
     * @param protocol the protocol (HTTP/HTTPS) to use when connecting to YOP services.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol == null ? ClientConfiguration.DEFAULT_PROTOCOL : protocol;
    }

    /**
     * Sets the protocol (HTTP/HTTPS) to use when connecting to YOP services, and returns the updated configuration
     * instance.
     *
     * @param protocol the protocol (HTTP/HTTPS) to use when connecting to YOP services.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProtocol(Protocol protocol) {
        this.setProtocol(protocol);
        return this;
    }

    /**
     * Returns the maximum number of open HTTP connections.
     *
     * @return the maximum number of open HTTP connections.
     */
    public int getMaxConnections() {
        return this.maxConnections;
    }

    /**
     * Sets the maximum number of open HTTP connections.
     *
     * @param maxConnections the maximum number of open HTTP connections.
     * @throws IllegalArgumentException if maxConnections is negative.
     */
    public void setMaxConnections(int maxConnections) {
        checkArgument(maxConnections >= 0, "maxConnections should not be negative.");
        this.maxConnections = maxConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        checkArgument(maxConnectionsPerRoute >= 0, "maxConnectionsPerRoute should not be negative.");
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    /**
     * Sets the maximum number of open HTTP connections, and returns the updated configuration instance.
     *
     * @param maxConnections the maximum number of open HTTP connections.
     * @return the updated configuration instance.
     * @throws IllegalArgumentException if maxConnections is negative.
     */
    public ClientConfiguration withMaxConnections(int maxConnections) {
        this.setMaxConnections(maxConnections);
        return this;
    }

    /**
     * Sets the maximum number of per route, and returns the updated configuration instance.
     *
     * @param maxConnections the maximum number of per route.
     * @return the updated configuration instance.
     * @throws IllegalArgumentException if maxConnections is negative.
     */
    public ClientConfiguration withMaxConnectionsPerRoute(int maxConnections) {
        this.setMaxConnectionsPerRoute(maxConnections);
        return this;
    }

    /**
     * Returns the User-Agent header value to use when sending requests to YOP services.
     *
     * @return the User-Agent header value to use when sending requests to YOP services.
     */
    public String getUserAgent() {
        return this.userAgent;
    }

    /**
     * Sets the User-Agent header value to use when sending requests to YOP services.
     * <p>
     * If the specified value is null, DEFAULT_USER_AGENT is used. If the specified value does not end with
     * DEFAULT_USER_AGENT, DEFAULT_USER_AGENT is appended.
     *
     * @param userAgent the User-Agent header value to use when sending requests to YOP services.
     */
    public void setUserAgent(String userAgent) {
        if (userAgent == null) {
            this.userAgent = DEFAULT_USER_AGENT;
        } else if (userAgent.endsWith(DEFAULT_USER_AGENT)) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = userAgent + ", " + DEFAULT_USER_AGENT;
        }
    }

    /**
     * Sets the User-Agent header value to use when sending requests to YOP services, and returns the updated
     * configuration instance.
     * <p>
     * If the specified value is null, DEFAULT_USER_AGENT is used. If the specified value does not end with
     * DEFAULT_USER_AGENT, DEFAULT_USER_AGENT is appended.
     *
     * @param userAgent the User-Agent header value to use when sending requests to YOP services.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withUserAgent(String userAgent) {
        this.setUserAgent(userAgent);
        return this;
    }

    /**
     * Returns the optional local address to bind when connecting to YOP services.
     *
     * @return the optional local address to bind when connecting to YOP services.
     */
    public InetAddress getLocalAddress() {
        return this.localAddress;
    }

    /**
     * Sets the optional local address to bind when connecting to YOP services.
     *
     * @param localAddress the optional local address to bind when connecting to YOP services.
     */
    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Sets the optional local address to bind when connecting to YOP services, and returns the updated configuration
     * instance.
     *
     * @param localAddress the optional local address to bind when connecting to YOP services.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withLocalAddress(InetAddress localAddress) {
        this.setLocalAddress(localAddress);
        return this;
    }

    /**
     * Returns the optional proxy host the client will connect through.
     *
     * @return the optional proxy host the client will connect through.
     */
    public String getProxyHost() {
        return this.proxyHost;
    }

    /**
     * Sets the optional proxy host the client will connect through.
     * <p>
     * The client will connect through the proxy only if the host is not null and the port is positive.
     *
     * @param proxyHost the optional proxy host the client will connect through.
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Sets the optional proxy host the client will connect through, and returns the updated configuration instance.
     * <p>
     * The client will connect through the proxy only if the host is not null and the port is positive.
     *
     * @param proxyHost the optional proxy host the client will connect through.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyHost(String proxyHost) {
        this.setProxyHost(proxyHost);
        return this;
    }

    /**
     * Returns the optional proxy port the client will connect through.
     *
     * @return the optional proxy port the client will connect through.
     */
    public int getProxyPort() {
        return this.proxyPort;
    }

    /**
     * Sets the optional proxy port the client will connect through.
     * <p>
     * The client will connect through the proxy only if the host is not null and the port is positive.
     *
     * @param proxyPort the optional proxy port the client will connect through.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Sets the optional proxy port the client will connect through, and returns the updated configuration instance.
     * <p>
     * The client will connect through the proxy only if the host is not null and the port is positive.
     *
     * @param proxyPort the optional proxy port the client will connect through.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyPort(int proxyPort) {
        this.setProxyPort(proxyPort);
        return this;
    }

    /**
     * Returns the optional proxy scheme the client will connect through.
     *
     * @return the optional proxy scheme the client will connect through.
     */
    public String getProxyScheme() {
        return this.proxyScheme;
    }

    /**
     * Sets the optional proxy scheme the client will connect through.
     * <p>
     * The client will connect through the proxy only if the host is not null and the port is positive.
     *
     * @param proxyScheme the optional proxy scheme the client will connect through.
     */
    public void setProxyScheme(String proxyScheme) {
        this.proxyScheme = proxyScheme;
    }

    /**
     * Sets the optional proxy scheme the client will connect through, and returns the updated configuration instance.
     * <p>
     * The client will connect through the proxy only if the host is not null and the port is positive.
     *
     * @param proxyScheme the optional proxy port the client will connect through.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyScheme(String proxyScheme) {
        this.setProxyScheme(proxyScheme);
        return this;
    }


    /**
     * Returns the optional user name to use when connecting through a proxy.
     *
     * @return the optional user name to use when connecting through a proxy.
     */
    public String getProxyUsername() {
        return this.proxyUsername;
    }

    /**
     * Sets the optional user name to use when connecting through a proxy.
     *
     * @param proxyUsername the optional user name to use when connecting through a proxy.
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * Sets the optional user name to use when connecting through a proxy, and returns the updated configuration
     * instance.
     *
     * @param proxyUsername the optional user name to use when connecting through a proxy.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyUsername(String proxyUsername) {
        this.setProxyUsername(proxyUsername);
        return this;
    }

    /**
     * Returns the optional password to use when connecting through a proxy.
     *
     * @return the optional password to use when connecting through a proxy.
     */
    public String getProxyPassword() {
        return this.proxyPassword;
    }

    /**
     * Sets the optional password to use when connecting through a proxy.
     *
     * @param proxyPassword the optional password to use when connecting through a proxy.
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * Sets the optional password to use when connecting through a proxy, and returns the updated configuration
     * instance.
     *
     * @param proxyPassword the optional password to use when connecting through a proxy.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyPassword(String proxyPassword) {
        this.setProxyPassword(proxyPassword);
        return this;
    }

    /**
     * Returns the optional Windows domain to use when connecting through a Windows NTLM proxy.
     *
     * @return the optional Windows domain to use when connecting through a Windows NTLM proxy.
     */
    public String getProxyDomain() {
        return this.proxyDomain;
    }

    /**
     * Sets the optional Windows domain to use when connecting through a Windows NTLM proxy.
     *
     * @param proxyDomain the optional Windows domain to use when connecting through a Windows NTLM proxy.
     */
    public void setProxyDomain(String proxyDomain) {
        this.proxyDomain = proxyDomain;
    }

    /**
     * Sets the optional Windows domain to use when connecting through a Windows NTLM proxy, and returns the updated
     * configuration instance.
     *
     * @param proxyDomain the optional Windows domain to use when connecting through a Windows NTLM proxy.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyDomain(String proxyDomain) {
        this.setProxyDomain(proxyDomain);
        return this;
    }

    /**
     * Returns the optional Windows workstation to use when connecting through a Windows NTLM proxy.
     *
     * @return the optional Windows workstation to use when connecting through a Windows NTLM proxy.
     */
    public String getProxyWorkstation() {
        return this.proxyWorkstation;
    }

    /**
     * Sets the optional Windows workstation to use when connecting through a Windows NTLM proxy.
     *
     * @param proxyWorkstation the optional Windows workstation to use when connecting through a Windows NTLM proxy.
     */
    public void setProxyWorkstation(String proxyWorkstation) {
        this.proxyWorkstation = proxyWorkstation;
    }

    /**
     * Sets the optional Windows workstation to use when connecting through a Windows NTLM proxy, and returns the
     * updated configuration instance.
     *
     * @param proxyWorkstation the optional Windows workstation to use when connecting through a Windows NTLM proxy.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyWorkstation(String proxyWorkstation) {
        this.setProxyWorkstation(proxyWorkstation);
        return this;
    }

    /**
     * Returns whether to enable proxy preemptive authentication. If it is true, the client will send the basic
     * authentication response even before the proxy server gives an unauthorized response in certain situations, thus
     * reducing the overhead of making the connection.
     *
     * @return whether to enable proxy preemptive authentication.
     */
    public boolean isProxyPreemptiveAuthenticationEnabled() {
        return this.proxyPreemptiveAuthenticationEnabled;
    }

    /**
     * Sets whether to enable proxy preemptive authentication. If it is true, the client will send the basic
     * authentication response even before the proxy server gives an unauthorized response in certain situations, thus
     * reducing the overhead of making the connection.
     *
     * @param proxyPreemptiveAuthenticationEnabled whether to enable proxy preemptive authentication.
     */
    public void setProxyPreemptiveAuthenticationEnabled(boolean proxyPreemptiveAuthenticationEnabled) {
        this.proxyPreemptiveAuthenticationEnabled = proxyPreemptiveAuthenticationEnabled;
    }

    /**
     * Sets whether to enable proxy preemptive authentication, and returns the updated configuration instance. If it is
     * true, the client will send the basic authentication response even before the proxy server gives an unauthorized
     * response in certain situations, thus reducing the overhead of making the connection.
     *
     * @param proxyPreemptiveAuthenticationEnabled whether to enable proxy preemptive authentication.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withProxyPreemptiveAuthenticationEnabled(
            boolean proxyPreemptiveAuthenticationEnabled) {
        this.setProxyPreemptiveAuthenticationEnabled(proxyPreemptiveAuthenticationEnabled);
        return this;
    }

    /**
     * Returns the retry policy for failed requests.
     *
     * @return the retry policy for failed requests.
     */
    public RetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }

    /**
     * Sets the retry policy for failed requests.
     *
     * @param retryPolicy the retry policy for failed requests.
     */
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy == null ? RetryPolicy.DEFAULT_RETRY_POLICY : retryPolicy;
    }

    /**
     * Sets the retry policy for failed requests, and returns the updated configuration instance.
     *
     * @param retryPolicy the retry policy for failed requests.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withRetryPolicy(RetryPolicy retryPolicy) {
        this.setRetryPolicy(retryPolicy);
        return this;
    }

    /**
     * Returns the socket timeout (SO_TIMEOUT) in milliseconds, which is a maximum period inactivity between two
     * consecutive data packets.
     *
     * @return the socket timeout (SO_TIMEOUT) in milliseconds.
     */
    public int getSocketTimeoutInMillis() {
        return this.socketTimeoutInMillis;
    }

    /**
     * Sets the socket timeout (SO_TIMEOUT) in milliseconds, which is a maximum period inactivity between two
     * consecutive data packets. A value of 0 means infinity, and is not recommended.
     *
     * @param socketTimeoutInMillis the socket timeout (SO_TIMEOUT) in milliseconds.
     * @throws IllegalArgumentException if socketTimeoutInMillis is negative.
     */
    public void setSocketTimeoutInMillis(int socketTimeoutInMillis) {
        checkArgument(socketTimeoutInMillis >= 0, "socketTimeoutInMillis should not be negative.");
        if (0 == socketTimeoutInMillis) {
            return;
        }
        this.socketTimeoutInMillis = socketTimeoutInMillis;
    }

    /**
     * Sets the socket timeout (SO_TIMEOUT) in milliseconds, which is a maximum period inactivity between two
     * consecutive data packets, and returns the updated configuration instance. A value of 0 means infinity, and is not
     * recommended.
     *
     * @param socketTimeoutInMillis the socket timeout (SO_TIMEOUT) in milliseconds.
     * @return the updated configuration instance.
     * @throws IllegalArgumentException if socketTimeoutInMillis is negative.
     */
    public ClientConfiguration withSocketTimeoutInMillis(int socketTimeoutInMillis) {
        this.setSocketTimeoutInMillis(socketTimeoutInMillis);
        return this;
    }

    /**
     * Returns the connection timeout in milliseconds.
     *
     * @return the connection timeout in milliseconds.
     */
    public int getConnectionTimeoutInMillis() {
        return this.connectionTimeoutInMillis;
    }

    /**
     * Sets the connection timeout in milliseconds. A value of 0 means infinity, and is not recommended.
     *
     * @param connectionTimeoutInMillis the connection timeout in milliseconds.
     * @throws IllegalArgumentException if connectionTimeoutInMillis is negative.
     */
    public void setConnectionTimeoutInMillis(int connectionTimeoutInMillis) {
        checkArgument(connectionTimeoutInMillis >= 0, "connectionTimeoutInMillis should not be negative.");
        if (0 == connectionTimeoutInMillis) {
            return;
        }
        this.connectionTimeoutInMillis = connectionTimeoutInMillis;
    }

    /**
     * Sets the connection timeout in milliseconds, and returns the updated configuration instance. A value of 0 means
     * infinity, and is not recommended.
     *
     * @param connectionTimeoutInMillis the connection timeout in milliseconds.
     * @return the updated configuration instance.
     * @throws IllegalArgumentException if connectionTimeoutInMillis is negative.
     */
    public ClientConfiguration withConnectionTimeoutInMillis(int connectionTimeoutInMillis) {
        this.setConnectionTimeoutInMillis(connectionTimeoutInMillis);
        return this;
    }

    /**
     * Returns the connection request timeout in milliseconds.
     *
     * @return the connection request timeout in milliseconds.
     */
    public int getConnectionRequestTimeoutInMillis() {
        return this.connectionRequestTimeoutInMillis;
    }

    /**
     * Sets the connection request timeout in milliseconds. A value of 0 means infinity, and is not recommended.
     *
     * @param connectionRequestTimeoutInMillis the connection request timeout in milliseconds.
     * @throws IllegalArgumentException if connectionRequestTimeoutInMillis is negative.
     */
    public void setConnectionRequestTimeoutInMillis(int connectionRequestTimeoutInMillis) {
        checkArgument(connectionRequestTimeoutInMillis >= 0, "connectionRequestTimeoutInMillis should not be negative.");
        if (0 == connectionRequestTimeoutInMillis) {
            return;
        }
        this.connectionRequestTimeoutInMillis = connectionRequestTimeoutInMillis;
    }

    /**
     * Sets the connection request timeout in milliseconds, and returns the updated configuration instance. A value of 0 means
     * infinity, and is not recommended.
     *
     * @param connectionRequestTimeoutInMillis the connection request timeout in milliseconds.
     * @return the updated configuration instance.
     * @throws IllegalArgumentException if connectionRequestTimeoutInMillis is negative.
     */
    public ClientConfiguration withConnectionRequestTimeoutInMillis(int connectionRequestTimeoutInMillis) {
        this.setConnectionRequestTimeoutInMillis(connectionRequestTimeoutInMillis);
        return this;
    }

    /**
     * Returns the optional size (in bytes) for the low level TCP socket buffer. This is an advanced option for advanced
     * users who want to tune low level TCP parameters to try and squeeze out more performance. Ignored if not positive.
     *
     * @return the optional size (in bytes) for the low level TCP socket buffer.
     */
    public int getSocketBufferSizeInBytes() {
        return this.socketBufferSizeInBytes;
    }

    /**
     * Sets the optional size (in bytes) for the low level TCP socket buffer. This is an advanced option for advanced
     * users who want to tune low level TCP parameters to try and squeeze out more performance. Ignored if not positive.
     *
     * @param socketBufferSizeInBytes the optional size (in bytes) for the low level TCP socket buffer.
     */
    public void setSocketBufferSizeInBytes(int socketBufferSizeInBytes) {
        this.socketBufferSizeInBytes = socketBufferSizeInBytes;
    }

    /**
     * Sets the optional size (in bytes) for the low level TCP socket buffer, and returns the updated configuration
     * instance. This is an advanced option for advanced users who want to tune low level TCP parameters to try and
     * squeeze out more performance. Ignored if not positive.
     *
     * @param socketBufferSizeInBytes the optional size (in bytes) for the low level TCP socket buffer.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withSocketBufferSizeInBytes(int socketBufferSizeInBytes) {
        this.setSocketBufferSizeInBytes(socketBufferSizeInBytes);
        return this;
    }

    /**
     * Returns the service endpoint URL to which the client will connect.
     *
     * @return the service endpoint URL to which the client will connect.
     */
    public String getEndpoint() {
        String url = this.endpoint;
        // if the set endpoint does not contain a protocol, append protocol to head of it
        if (this.endpoint != null && this.endpoint.length() > 0
                && endpoint.indexOf("://") < 0) {
            url = protocol.toString().toLowerCase() + "://" + endpoint;
        }
        return url;
    }

    /**
     * Sets the service endpoint URL to which the client will connect.
     *
     * @param endpoint the service endpoint URL to which the client will connect.
     * @throws IllegalArgumentException if endpoint is not a valid URL.
     * @throws NullPointerException     if endpoint is null.
     */
    public void setEndpoint(String endpoint) {
        checkNotNull(endpoint, "endpoint should not be null.");

        this.endpoint = endpoint;
    }

    /**
     * Sets the service endpoint URL to which the client will connect, and returns the updated configuration instance.
     *
     * @param endpoint the service endpoint URL to which the client will connect.
     * @return the updated configuration instance.
     * @throws IllegalArgumentException if endpoint is not a valid URL.
     * @throws NullPointerException     if endpoint is null.
     */
    public ClientConfiguration withEndpoint(String endpoint) {
        this.setEndpoint(endpoint);
        return this;
    }

    /**
     * Returns the region of service. This value is used by the client to construct the endpoint URL automatically, and
     * is ignored if endpoint is not null.
     *
     * @return the region of service.
     */
    public Region getRegion() {
        return this.region;
    }

    /**
     * Sets the region of service. This value is used by the client to construct the endpoint URL automatically, and is
     * ignored if endpoint is not null.
     * <p>
     * If the specified region is null, sets to DEFAULT_REGION.
     *
     * @param region the region of service.
     */
    public void setRegion(Region region) {
        this.region = region == null ? DEFAULT_REGION : region;
    }

    /**
     * Sets the region of service, and returns the updated configuration instance. This value is used by the client to
     * construct the endpoint URL automatically, and is ignored if endpoint is not null.
     * <p>
     * If the specified region is null, sets to DEFAULT_REGION.
     *
     * @param region the region of service.
     * @return the updated configuration instance.
     */
    public ClientConfiguration withRegion(Region region) {
        this.setRegion(region);
        return this;
    }

    /**
     * Returns the YOP credentials used by the client to sign HTTP requests.
     *
     * @return the YOP credentials used by the client to sign HTTP requests.
     */
    public YopCredentials<?> getCredentials() {
        return this.credentials;
    }

    /**
     * Sets the YOP credentials used by the client to sign HTTP requests.
     *
     * @param credentials the YOP credentials used by the client to sign HTTP requests.
     * @throws NullPointerException if credentials is null.
     */
    public void setCredentials(YopCredentials<?> credentials) {
        checkNotNull(credentials, "credentials should not be null.");
        this.credentials = credentials;
    }

    /**
     * Sets the YOP credentials used by the client to sign HTTP requests, and returns the updated configuration
     * instance.
     *
     * @param credentials the YOP credentials used by the client to sign HTTP requests.
     * @return the updated configuration instance.
     * @throws NullPointerException if credentials is null.
     */
    public ClientConfiguration withCredentials(YopCredentials<?> credentials) {
        this.setCredentials(credentials);
        return this;
    }

    /**
     * Returns the Http Client Impl
     *
     * @return the the Http Client Impl.
     */
    public String getClientImpl() {
        return this.clientImpl;
    }

    /**
     * Sets the Http Client Impl
     *
     * @param clientImpl the Http Client Impl
     * @throws NullPointerException if clientImpl is null.
     */
    public void setClientImpl(String clientImpl) {
        if (StringUtils.isNotBlank(clientImpl)) {
            this.clientImpl = clientImpl;
        }
    }

    /**
     * Sets the Http Client Impl.
     *
     * @param clientImpl the Http Client Impl.
     * @return the updated configuration instance.
     * @throws NullPointerException if clientImpl is null.
     */
    public ClientConfiguration withClientImpl(String clientImpl) {
        this.setClientImpl(clientImpl);
        return this;
    }

    @Override
    public String toString() {
        return "ClientConfiguration [ \n  userAgent=" + userAgent
                + ", \n  retryPolicy=" + retryPolicy + ", \n  localAddress="
                + localAddress + ", \n  protocol=" + protocol + ", \n  proxyHost="
                + proxyHost + ", \n  proxyPort=" + proxyPort + ", \n  proxyScheme="
                + proxyScheme + ", \n  proxyUsername="
                + proxyUsername + ", \n  proxyPassword=" + proxyPassword
                + ", \n  proxyDomain=" + proxyDomain + ", \n  proxyWorkstation="
                + proxyWorkstation + ", \n  proxyPreemptiveAuthenticationEnabled="
                + proxyPreemptiveAuthenticationEnabled + ", \n  maxConnections="
                + maxConnections + ", \n  socketTimeoutInMillis="
                + socketTimeoutInMillis + ", \n  connectionTimeoutInMillis="
                + connectionTimeoutInMillis + ", \n  socketBufferSizeInBytes="
                + socketBufferSizeInBytes + ", \n  endpoint=" + endpoint
                + ", \n  region=" + region + ", \n  credentials=" + credentials
                + ", \n  clientImpl=" + clientImpl + "]\n";
    }

}
