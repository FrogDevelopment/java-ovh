package com.ovh.api;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.ovh.api.OvhApiException.OvhApiExceptionCause.API_ERROR;
import static com.ovh.api.OvhApiException.OvhApiExceptionCause.AUTH_ERROR;
import static com.ovh.api.OvhApiException.OvhApiExceptionCause.BAD_PARAMETERS_ERROR;
import static com.ovh.api.OvhApiException.OvhApiExceptionCause.INTERNAL_ERROR;
import static com.ovh.api.OvhApiException.OvhApiExceptionCause.RESOURCE_CONFLICT_ERROR;
import static com.ovh.api.OvhApiException.OvhApiExceptionCause.RESOURCE_NOT_FOUND;
import static java.net.HttpURLConnection.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * Simple low level wrapper over the OVH REST API.
 *
 * @author mbsk
 */
@RequiredArgsConstructor
public class OvhApi {

    private final static Map<String, String> ENDPOINTS_BY_NAME = Map.of(
            "ovh-eu", "https://eu.api.ovh.com/1.0",
            "ovh-ca", "https://ca.api.ovh.com/1.0",
            "kimsufi-eu", "https://eu.api.kimsufi.com/1.0",
            "kimsufi-ca", "https://ca.api.kimsufi.com/1.0",
            "soyoustart-eu", "https://eu.api.soyoustart.com/1.0",
            "soyoustart-ca", "https://ca.api.soyoustart.com/1.0",
            "runabove", "https://api.runabove.com/1.0",
            "runabove-ca", "https://api.runabove.com/1.0"
    );

    private final OvhProperties ovhProperties;

    public String get(String path) throws OvhApiException {
        return get(path, "", true);
    }

    public String get(String path, boolean needAuth) throws OvhApiException {
        return get(path, "", needAuth);
    }

    public String get(String path, String body, boolean needAuth) throws OvhApiException {
        return call("GET", body, path, needAuth);
    }

    public String put(String path, String body, boolean needAuth) throws OvhApiException {
        return call("PUT", body, path, needAuth);
    }

    public String post(String path, String body, boolean needAuth) throws OvhApiException {
        return call("POST", body, path, needAuth);
    }

    public String delete(String path, String body, boolean needAuth) throws OvhApiException {
        return call("DELETE", body, path, needAuth);
    }

    private String call(String method, String body, String path, boolean needAuth) throws OvhApiException {
        try {
            var endpoint = ENDPOINTS_BY_NAME.getOrDefault(ovhProperties.getEndpoint(), ovhProperties.getEndpoint());

            var url = new URL(new StringBuilder(endpoint).append(path).toString());

            // prepare
            var request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod(method);
            request.setReadTimeout(ovhProperties.getReadTimeOut());
            request.setConnectTimeout(ovhProperties.getConnectTimeOut());
            request.setRequestProperty("Content-Type", "application/json");
            request.setRequestProperty("X-Ovh-Application", ovhProperties.getApplicationKey());

            // handle authentication
            if (needAuth) {
                addAuthenticationHeaders(request, method, url, body);
            }

            if (body != null && !body.isEmpty()) {
                request.setDoOutput(true);
                try (var out = new DataOutputStream(request.getOutputStream())) {
                    out.writeBytes(body);
                    out.flush();
                }
            }

            var responseCode = request.getResponseCode();
            BufferedReader in;
            if (responseCode == HTTP_OK) {
                in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
            }

            // build response
            var response = new StringBuilder();
            try (in) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            // return the raw JSON result
            return switch (responseCode) {
                case HTTP_OK -> response.toString();
                case HTTP_BAD_REQUEST -> throw new OvhApiException(response.toString(), BAD_PARAMETERS_ERROR);
                case HTTP_FORBIDDEN -> throw new OvhApiException(response.toString(), AUTH_ERROR);
                case HTTP_NOT_FOUND -> throw new OvhApiException(response.toString(), RESOURCE_NOT_FOUND);
                case HTTP_CONFLICT -> throw new OvhApiException(response.toString(), RESOURCE_CONFLICT_ERROR);
                default -> throw new OvhApiException(response.toString(), API_ERROR);
            };

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new OvhApiException(e.getMessage(), INTERNAL_ERROR);
        }
    }

    private void addAuthenticationHeaders(HttpURLConnection request, String method, URL url, String body) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // get timestamp from local system
        var timestamp = System.currentTimeMillis() / 1000;

        // set HTTP headers for authentication
        request.setRequestProperty("X-Ovh-Consumer", ovhProperties.getConsumerKey());
        request.setRequestProperty("X-Ovh-Timestamp", Long.toString(timestamp));

        // build signature
        var toSign = new StringBuilder(ovhProperties.getApplicationSecret())
                .append("+")
                .append(ovhProperties.getConsumerKey())
                .append("+")
                .append(method)
                .append("+")
                .append(url)
                .append("+")
                .append(body)
                .append("+")
                .append(timestamp)
                .toString();
        var signature = new StringBuilder("$1$").append(hashSHA1(toSign)).toString();
        request.setRequestProperty("X-Ovh-Signature", signature);
    }

    private static String hashSHA1(String text) throws NoSuchAlgorithmException {
        var md = MessageDigest.getInstance("SHA-1");
        var sha1hash = new byte[40];
        md.update(text.getBytes(ISO_8859_1), 0, text.length());
        sha1hash = md.digest();
        var sb = new StringBuilder();
        for (var hash : sha1hash) {
            sb.append(Integer.toString((hash & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}
