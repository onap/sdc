package org.openecomp.sdc.common.http.client.api;

import org.apache.http.conn.HttpClientConnectionManager;
import org.junit.Test;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.openecomp.sdc.security.SecurityUtil;

import static org.junit.Assert.assertNotNull;

public class HttpConnectionMngFactoryTest {

    @Test
    public void validate() {
        final String testKeyStore = "testKeyStore ";
        final String testKeyStorePassword = SecurityUtil.INSTANCE.encrypt("testKeyStorePassword").left().value();

        ClientCertificate clientCertificate = new ClientCertificate();
        clientCertificate.setKeyStore(testKeyStore);
        clientCertificate.setKeyStorePassword(testKeyStorePassword);
        HttpClientConnectionManager httpClientConnectionManager = new HttpConnectionMngFactory().getOrCreate(clientCertificate);

        assertNotNull(httpClientConnectionManager);

        httpClientConnectionManager.shutdown();
    }

}
