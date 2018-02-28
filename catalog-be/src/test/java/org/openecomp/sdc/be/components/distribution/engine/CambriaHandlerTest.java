package org.openecomp.sdc.be.components.distribution.engine;

import com.att.nsa.apiClient.credentials.ApiCredential;
import com.att.nsa.apiClient.http.HttpException;
import com.att.nsa.cambria.client.CambriaClient.CambriaApiException;
import com.att.nsa.cambria.client.CambriaIdentityManager;
import fj.data.Either;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class CambriaHandlerTest {
    @Spy
    private CambriaHandler handler = new CambriaHandler();

    @Mock
    private CambriaIdentityManager createIdentityManager;

    private ApiCredential apiCredential = new ApiCredential("apiKey", "apiSecret");

    @BeforeClass
    public static void beforeClass() {
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        new ConfigurationManager(configurationSource);
    }


    @Before
    public void startUp() throws MalformedURLException, GeneralSecurityException {
        doReturn(createIdentityManager).when(handler).buildCambriaClient(any());
    }

    @Test
    public void testCreateUebKeys() throws HttpException, CambriaApiException, IOException {
        Mockito.when(createIdentityManager.createApiKey(Mockito.anyString(), Mockito.anyString())).thenReturn(apiCredential);
        Either<ApiCredential, CambriaErrorResponse> eitherCreateUebKeys = handler.createUebKeys(Arrays.asList("Myhost:1234") );

        Mockito.verify(createIdentityManager).setApiCredentials(Mockito.anyString(), Mockito.anyString());

        assertTrue("Unexpected Operational Status", eitherCreateUebKeys.isLeft());

    }

    @Test
    public void testCreateUebKeys_FAIL() throws HttpException, CambriaApiException, IOException {
        Mockito.when(createIdentityManager.createApiKey(Mockito.anyString(), Mockito.anyString())).thenThrow(new CambriaApiException("Error Message"));
        Either<ApiCredential, CambriaErrorResponse> eitherCreateUebKeys = handler.createUebKeys(Arrays.asList("Myhost:1234") );
        Mockito.verify(createIdentityManager, Mockito.never()).setApiCredentials(Mockito.anyString(), Mockito.anyString());
        assertTrue("Unexpected Operational Status", eitherCreateUebKeys.isRight());
        CambriaErrorResponse response = eitherCreateUebKeys.right().value();
        assertEquals("Unexpected Operational Status", CambriaOperationStatus.CONNNECTION_ERROR, response.getOperationStatus());
        assertEquals("Unexpected HTTP Code", 500, response.getHttpCode().intValue());
    }

}
