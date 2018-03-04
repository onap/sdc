package org.openecomp.sdc.common.http.client.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class RestUtils {

    public static void addBasicAuthHeader(Properties headers, String username, String password) {
        byte[] credentials = Base64.encodeBase64((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        headers.setProperty(HttpHeaders.AUTHORIZATION, "Basic " + new String(credentials, StandardCharsets.UTF_8));
    }

}
