package org.openecomp.sdc.common.http.client.api;

import org.apache.http.HttpHeaders;
import org.junit.Test;

import static org.junit.Assert.*;

import org.openecomp.sdc.common.http.client.api.*;

import java.util.Properties;

public class RestUtilsTest {

    @Test
    public void addBasicAuthHeader() {
        Properties headers = new Properties();
        RestUtils restutil = new RestUtils();
        restutil.addBasicAuthHeader(headers,"uname","passwd");
        assertEquals(headers.getProperty(HttpHeaders.AUTHORIZATION),"Basic dW5hbWU6cGFzc3dk");
    }
}