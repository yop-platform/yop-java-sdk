package com.yeepay.yop.sdk.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.HttpMethodName;
import com.yeepay.yop.sdk.http.Protocol;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class HttpUtils {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final BitSet URI_UNRESERVED_CHARACTERS = new BitSet();
    private static final String[] PERCENT_ENCODED_STRINGS = new String[256];

    private static final Joiner queryStringJoiner = Joiner.on('&');
    private static final boolean HTTP_VERBOSE = Boolean.parseBoolean(System.getProperty("yop.sdk.http", "false"));
//    private static boolean HTTP_VERBOSE = true;

    /**
     * Regex which matches any of the sequences that we need to fix up after URLEncoder.encode().
     */
    // private static final Pattern ENCODED_CHARACTERS_PATTERN;
    static {
        /*
         * StringBuilder pattern = new StringBuilder();
         *
         * pattern .append(Pattern.quote("+")) .append("|") .append(Pattern.quote("*")) .append("|")
         * .append(Pattern.quote("%7E")) .append("|") .append(Pattern.quote("%2F"));
         *
         * ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
         */
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
     * Yop object key can contain arbitrary characters, which may result double slash in the url path. Apache http
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
     * Normalize a string for use in YOP web service APIs. The normalization algorithm is:
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

    /**
     * Encode a string for use in the path of a URL; uses URLEncoder.encode, (which encodes a string for use in the
     * query portion of a URL), then applies some postfilters to fix things up per the RFC. Can optionally handle
     * strings which are meant to encode a path (ie include '/'es which should NOT be escaped).
     *
     * @param value the value to encode
     * @param path true if the value is intended to represent a path
     * @return the encoded value
     */
    /*
     * public static String urlEncode(String value) { if (value == null) { return ""; }
     *
     * try { String encoded = URLEncoder.encode(value, DEFAULT_ENCODING);
     *
     * Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded); StringBuffer buffer = new
     * StringBuffer(encoded.length());
     *
     * while (matcher.find()) { String replacement = matcher.group(0);
     *
     * if ("+".equals(replacement)) { replacement = "%20"; } else if ("*".equals(replacement)) { replacement = "%2A"; }
     * else if ("%7E".equals(replacement)) { replacement = "~"; } else if (path && "%2F".equals(replacement)) {
     * replacement = "/"; }
     *
     * matcher.appendReplacement(buffer, replacement); }
     *
     * matcher.appendTail(buffer); return buffer.toString();
     *
     * } catch (UnsupportedEncodingException ex) { throw new RuntimeException(ex); } }
     */

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

    public static String getCanonicalQueryString(Map<String, List<String>> parameters, boolean forSignature) {
        if (parameters.isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (forSignature && Headers.AUTHORIZATION.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");
            List<String> value = entry.getValue();
            if (value == null) {
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
     * Append the given path to the given baseUri.
     * <p>
     * <p>
     * This method will encode the given path but not the given baseUri.
     *
     * @param baseUri
     * @param pathComponents
     */
    public static URI appendUri(URI baseUri, String... pathComponents) {
        StringBuilder builder = new StringBuilder(baseUri.toASCIIString());
        for (String path : pathComponents) {
            if (path != null && path.length() > 0) {
                path = normalizePath(path);
                if (path.startsWith("/")) {
                    if (builder.charAt(builder.length() - 1) == '/') {
                        builder.setLength(builder.length() - 1);
                    }
                } else {
                    if (builder.charAt(builder.length() - 1) != '/') {
                        builder.append('/');
                    }
                }
                builder.append(path);
            }
        }
        try {
            return new URI(builder.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }

    public static void printRequest(HttpRequestBase request) {
        if (!HTTP_VERBOSE) {
            return;
        }
        System.out.println("\n-------------> ");
        System.out.println(request.getRequestLine());
        for (Header h : request.getAllHeaders()) {
            System.out.println(h.getName() + " : " + h.getValue());
        }
        RequestConfig config = request.getConfig();
        if (config != null) {
            System.out.println("getConnectionRequestTimeout: "
                    + config.getConnectionRequestTimeout());
            System.out.println("getConnectTimeout: "
                    + config.getConnectTimeout());
            System.out.println("getCookieSpec: " + config.getCookieSpec());
            System.out.println("getLocalAddress: " + config.getLocalAddress());

        }
    }

    public static void printResponse(CloseableHttpResponse response) {
        if (!HTTP_VERBOSE) {
            return;
        }
        System.out.println("\n<------------- ");
        StatusLine status = response.getStatusLine();
        System.out.println(status.getStatusCode() + " - "
                + status.getReasonPhrase());
        Header[] heads = response.getAllHeaders();
        for (Header h : heads) {
            System.out.println(h.getName() + " : " + h.getValue());
        }
    }

    public static boolean usePayloadForQueryParameters(Request<? extends BaseRequest> request) {
        boolean requestIsPOST = HttpMethodName.POST.equals(request.getHttpMethod());
        boolean requestHasNoPayload = (request.getContent() == null);

        return requestIsPOST && requestHasNoPayload;
    }

    /**
     * Creates an encoded query string from all the parameters in the specified
     * request.
     *
     * @param request      The request containing the parameters to encode.
     * @param forSignature forSignature.
     * @return Null if no parameters were present, otherwise the encoded query
     * string for the parameters present in the specified request.
     */
    public static String encodeParameters(Request<? extends BaseRequest> request, Boolean forSignature) {
        Map<String, List<String>> requestParams = BooleanUtils.isTrue(forSignature) ?
                new TreeMap<String, List<String>>(request.getParameters()) : Collections.unmodifiableMap(request.getParameters());

        if (requestParams.isEmpty()) {
            return null;
        }
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        for (Map.Entry<String, List<String>> entry : requestParams.entrySet()) {
            String parameterName = entry.getKey();
            for (String value : entry.getValue()) {
                try {
                    nameValuePairs.add(new BasicNameValuePair(parameterName, BooleanUtils.isTrue(forSignature) ? value : URLEncoder.encode(value, YopConstants.DEFAULT_ENCODING)));
                } catch (UnsupportedEncodingException e) {
                    throw new YopClientException("unsupported charset.", e);
                }
            }
        }
        return URLEncodedUtils.format(nameValuePairs, DEFAULT_ENCODING);
    }
}
