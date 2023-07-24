package com.yeepay.g3.sdk.yop.http;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/1/10 上午11:23
 */
public final class HttpUtils {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static BitSet URI_UNRESERVED_CHARACTERS = new BitSet();
    private static String[] PERCENT_ENCODED_STRINGS = new String[256];

    private static final Joiner queryStringJoiner = Joiner.on('&');

    private HttpUtils() {
        // do nothing
    }

    /**
     * Regex which matches any of the sequences that we need to fix up after URLEncoder.encode().
     */
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        URI_UNRESERVED_CHARACTERS.set('-');
        URI_UNRESERVED_CHARACTERS.set('.');
        URI_UNRESERVED_CHARACTERS.set('_');
        URI_UNRESERVED_CHARACTERS.set('~');

        for (int i = 0; i < PERCENT_ENCODED_STRINGS.length; ++i) {
            PERCENT_ENCODED_STRINGS[i] = String.format("%%%02X", i);
        }
    }

    /**
     * Normalize a string for use in url path. The algorithm is:
     * <p>
     * <p>
     * <ol>
     * <li>Normalize the string</li>
     * <li>replace all "%2F" with "/"</li>
     * <li>replace all "//" with "/%2F"</li>
     * </ol>
     * <p>
     * <p>
     * object key can contain arbitrary characters, which may result double slash in the url path. Apache http
     * client will replace "//" in the path with a single '/', which makes the object key incorrect. Thus we replace
     * "//" with "/%2F" here.
     *
     * @param path the path string to normalize.
     * @return the normalized path string.
     * @see #normalize(String)
     */
    public static String normalizePath(String path) {
        return normalize(path).replace("%2F", "/");
    }

    /**
     * Normalize a string for use in web service APIs. The normalization algorithm is:
     * <p>
     * <ol>
     * <li>Convert the string into a UTF-8 byte array.</li>
     * <li>Encode all octets into percent-encoding, except all URI unreserved characters per the RFC 3986.</li>
     * </ol>
     * <p>
     * <p>
     * All letters used in the percent-encoding are in uppercase.
     *
     * @param value the string to normalize.
     * @return the normalized string.
     * @throws UnsupportedEncodingException
     */
    public static String normalize(String value) {
        try {
            StringBuilder builder = new StringBuilder();
            for (byte b : value.getBytes(DEFAULT_ENCODING)) {
                if (URI_UNRESERVED_CHARACTERS.get(b & 0xFF)) {
                    builder.append((char) b);
                } else {
                    builder.append(PERCENT_ENCODED_STRINGS[b & 0xFF]);
                }
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCanonicalURIPath(String path) {
        if (path == null) {
            return "/";
        } else if (path.startsWith("/")) {
            return normalizePath(path);
        } else {
            return "/" + normalizePath(path);
        }
    }

    /**
     * 适用于 RSA 签名方式
     *
     * @param parameters
     * @param forSignature
     * @return
     */
    public static String getCanonicalQueryString(Multimap<String, String> parameters, boolean forSignature) {
        if (parameters.isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, Collection<String>> entry : parameters.asMap().entrySet()) {
            if (forSignature && Headers.AUTHORIZATION.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");
            Collection<String> value = entry.getValue();
            if (null == value || 0 == value.size()) {
                if (forSignature) {
                    parameterStrings.add(normalize(key) + '=');
                } else {
                    parameterStrings.add(normalize(key));
                }
            } else {
                for (String item : value) {
                    parameterStrings.add(normalize(key) + '=' + normalize(item));
                }
            }
        }
        Collections.sort(parameterStrings);

        return queryStringJoiner.join(parameterStrings);
    }

    /**
     * 适用于 RSA 签名方式
     *
     * @param parameters
     * @param forSignature
     * @return
     */
    public static String getCanonicalQueryString(Map<String, String[]> parameters, boolean forSignature) {
        if (parameters.isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            if (forSignature && Headers.AUTHORIZATION.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");
            String[] value = entry.getValue();
            if (null == value || 0 == value.length) {
                if (forSignature) {
                    parameterStrings.add(normalize(key) + '=');
                } else {
                    parameterStrings.add(normalize(key));
                }
            } else {
                for (String item : value) {
                    parameterStrings.add(normalize(key) + '=' + normalize(item));
                }
            }
        }
        Collections.sort(parameterStrings);

        return queryStringJoiner.join(parameterStrings);
    }

    public static String formatServerRoot(String serverRoot) {
        if (StringUtils.isBlank(serverRoot)) {
            return "";
        }
        serverRoot = serverRoot.trim();
        if (StringUtils.endsWith(serverRoot, "/")) {
            return StringUtils.substring(serverRoot, 0, -1);
        }
        return serverRoot;
    }

    /**
     * Returns a host header according to the specified URI. The host header is generated with the same logic used by
     * apache http client, that is, append the port to hostname only if it is not the default port.
     *
     * @param uri the URI
     * @return a host header according to the specified URI.
     */
    public static String generateHostHeader(URI uri) {
        String host = uri.getHost();
        if (isUsingNonDefaultPort(uri)) {
            host += ":" + uri.getPort();
        }
        return host;
    }

    /**
     * Returns true if the specified URI is using a non-standard port (i.e. any port other than 80 for HTTP URIs or any
     * port other than 443 for HTTPS URIs).
     *
     * @param uri the URI
     * @return True if the specified URI is using a non-standard port, otherwise false.
     */
    public static boolean isUsingNonDefaultPort(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        int port = uri.getPort();
        if (port <= 0) {
            return false;
        }
        if (scheme.equals(Protocol.HTTP.toString())) {
            return port != Protocol.HTTP.getDefaultPort();
        }
        if (scheme.equals(Protocol.HTTPS.toString())) {
            return port != Protocol.HTTPS.getDefaultPort();
        }
        return false;
    }

}
